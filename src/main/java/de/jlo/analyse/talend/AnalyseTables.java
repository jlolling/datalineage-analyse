package de.jlo.analyse.talend;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.jlo.analyse.DatabaseTable;
import de.jlo.analyse.sql.SQLCodeUtil;
import de.jlo.analyse.sql.StrictSQLParser;

public class AnalyseTables {
	
	private Job job = null;
	private List<DatabaseTable> listInputTables = new ArrayList<>();
	private List<DatabaseTable> listOutputTables = new ArrayList<>();
	private List<DatabaseTable> listCreateTables = new ArrayList<>();
	private static Properties properties = new Properties();
	private static boolean loadPropertiesDone = false;
	private static final String propertiesFileName = "table-components.properties";
	private ContextVarResolver contextVarResolver = null;
	
	public AnalyseTables(Job job) throws Exception {
		if (job == null) {
			throw new IllegalArgumentException("job cannot be null");
		}
		this.job = job;
		if (loadPropertiesDone == false) {
			loadProperties();
			loadPropertiesDone = true;
		}
		contextVarResolver = new ContextVarResolver();
		contextVarResolver.setContext(job.getContext());
	}
	
	private void loadProperties() throws Exception {
		try (InputStream is = this.getClass().getResourceAsStream("/" + propertiesFileName)) {
			properties.load(is);
		} catch (Exception e) {
			throw new Exception("Load properties from " + propertiesFileName + " failed: " + e.getMessage(), e);
		}
	}
	
	public void analyseTables() throws Exception {
		List<Component> listComponents = job.getComponents();
		String key = null;
		String attrName = null;
		for (Component c : listComponents) {
			key = c.getComponentName() + ".QUERY";
			attrName = properties.getProperty(key);
			if (attrName != null) {
				String dbSchema = getDatabaseSchemaForComponentInput(c);
				// analyse query component
				String queryValue = c.getComponentValueByName(attrName);
				String query = contextVarResolver.replaceContextVars(queryValue);
				query = SQLCodeUtil.convertJavaToSqlCode(query);
				// get the host and database
				StrictSQLParser parser = new StrictSQLParser();
				parser.setDefaultSchema(dbSchema);
				parser.parseScriptFromCode(query);
				String host = DatabaseTable.getHost(dbSchema);
				for (String tableName : parser.getTablesRead()) {
					if (host != null) {
						tableName = host + ":" + tableName; 
					}
					addInputTable(new DatabaseTable(tableName));
				}
				for (String tableName : parser.getTablesWritten()) {
					if (host != null) {
						tableName = host + ":" + tableName; 
					}
					addOutputTable(new DatabaseTable(tableName));
				}
				for (String tableName : parser.getTablesCreated()) {
					if (host != null) {
						tableName = host + ":" + tableName; 
					}
					addCreateTable(new DatabaseTable(tableName));
				}
			}
			key = c.getComponentName() + ".SOURCE_TABLE";
			attrName = properties.getProperty(key);
			if (attrName != null) {
				// analyse input component table name
				String tableName = c.getComponentValueByName(attrName);
				if (tableName != null && tableName.replace("\"", "").trim().isEmpty() == false) {
					String cleanName = contextVarResolver.replaceContextVars(tableName);
					if (cleanName.contains(".") == false) {
						String dbSchema = getDatabaseSchemaForComponentInput(c);
						if (dbSchema != null) {
							cleanName = dbSchema + "." + cleanName;
						}
					}
					addInputTable(new DatabaseTable(cleanName.replace("\"", "")));
				}
			}
			key = c.getComponentName() + ".TARGET_TABLE";
			attrName = properties.getProperty(key);
			if (attrName != null) {
				// analyse output component
				String tableName = c.getComponentValueByName(attrName);
				String cleanName = contextVarResolver.replaceContextVars(tableName);
				if (cleanName.contains(".") == false) {
					String dbSchema = getDatabaseSchemaForComponentOutput(c);
					if (dbSchema != null) {
						cleanName = dbSchema + "." + cleanName;
					}
				}
				addOutputTable(new DatabaseTable(cleanName.replace("\"", "")));
			}
			if (c.getComponentName().equals("tCreateTable")) {
				String tableName = c.getComponentValueByName("TABLE");
				String cleanName = contextVarResolver.replaceContextVars(tableName);
				String dbSchema = getDatabaseSchemaForComponentOutput(c);
				if (dbSchema != null) {
					cleanName = dbSchema + "." + cleanName;
				}
				addCreateTable(new DatabaseTable(cleanName.replace("\"", "")));
			}
		}
	}
	
	private void addInputTable(DatabaseTable t) {
		if (listInputTables.contains(t) == false) {
			listInputTables.add(t);
		}
	}
	
	private void addOutputTable(DatabaseTable t) {
		if (listOutputTables.contains(t) == false) {
			listOutputTables.add(t);
		}
	}
	
	private void addCreateTable(DatabaseTable t) {
		if (listCreateTables.contains(t) == false) {
			listCreateTables.add(t);
		}
	}
	
	private Component getReferencedConnectionComponentForInput(Component c) throws Exception {
		String attr = properties.getProperty(c.getComponentName() + ".SOURCE_CONN");
		if (attr != null) {
			String uniqueIdConn = c.getComponentValueByName(attr);
			if (uniqueIdConn != null) {
				Component cc = job.getComponent(uniqueIdConn);
				return cc;
			}
		}
		return null;
	}
	
	private Component getReferencedConnectionComponentForOutput(Component c) throws Exception {
		String attr = properties.getProperty(c.getComponentName() + ".TARGET_CONN");
		if (attr != null) {
			String uniqueIdConn = c.getComponentValueByName(attr);
			if (uniqueIdConn != null) {
				Component cc = job.getComponent(uniqueIdConn);
				if (cc == null) {
					throw new Exception("Component " + uniqueIdConn + " does not exist in current job: " + c.getJob());
				}
				return cc;
			}
		}
		return null; 
	}

	private String getHost(Component dbComponent) throws Exception {
		String attr = properties.getProperty(dbComponent.getComponentName() + ".HOST");
		if (attr != null) {
			return contextVarResolver.replaceContextVars(dbComponent.getComponentValueByName(attr)).replace("\"", "");
		}
		return null;
	}
	
	private String getDatabaseName(Component dbComponent) throws Exception {
		String attr = properties.getProperty(dbComponent.getComponentName() + ".DBNAME");
		if (attr != null) {
			return contextVarResolver.replaceContextVars(dbComponent.getComponentValueByName(attr)).replace("\"", "");
		}
		return null;
	}

	private String getSchemaName(Component dbComponent) throws Exception {
		String attr = properties.getProperty(dbComponent.getComponentName() + ".SCHEMA");
		if (attr != null) {
			return contextVarResolver.replaceContextVars(dbComponent.getComponentValueByName(attr)).replace("\"", "");
		}
		return null;
	}
	
	private String getDatabaseSchemaForComponentOutput(Component c) throws Exception {
		if (c == null) {
			throw new IllegalArgumentException("component cannot be null");
		}
		boolean useAlwaysConnection = "true".equals(properties.get(c.getComponentName() + ".USE_ALWAYS_CONN"));
		boolean useExternalConnection = useAlwaysConnection || "true".equals(c.getComponentValueByName("USE_EXISTING_CONNECTION"));
		if (useExternalConnection) {
			Component cc = getReferencedConnectionComponentForOutput(c);
			return getDatabaseSchemaForComponentOutput(cc);
		}
		String database = getDatabaseName(c);
		String schema = getSchemaName(c);
		String dbSchema = null;
		if (database != null) {
			dbSchema = database;
		}
		if (schema != null) {
			if (dbSchema != null) {
				dbSchema = dbSchema + "." + schema;
			} else {
				dbSchema = schema;
			}
		}
		String host = getHost(c);
		if (host != null) {
			dbSchema = host + ":" + dbSchema;
		}
		return dbSchema;
	}

	private String getDatabaseSchemaForComponentInput(Component c) throws Exception {
		boolean useAlwaysConnection = "true".equals(properties.get(c.getComponentName() + ".USE_ALWAYS_CONN"));
		boolean useExternalConnection = useAlwaysConnection || "true".equals(c.getComponentValueByName("USE_EXISTING_CONNECTION"));
		if (useExternalConnection) {
			Component cc = getReferencedConnectionComponentForInput(c);
			return getDatabaseSchemaForComponentOutput(cc);
		}
		String database = getDatabaseName(c);
		String schema = getSchemaName(c);
		String dbSchema = null;
		if (database != null) {
			dbSchema = database;
		}
		if (schema != null) {
			if (dbSchema != null) {
				dbSchema = dbSchema + "." + schema;
			} else {
				dbSchema = schema;
			}
		}
		return dbSchema;
	}

	public List<DatabaseTable> getListInputTables() {
		return listInputTables;
	}

	public List<DatabaseTable> getListOutputTables() {
		return listOutputTables;
	}

	public List<DatabaseTable> getListCreateTables() {
		return listCreateTables;
	}

}
