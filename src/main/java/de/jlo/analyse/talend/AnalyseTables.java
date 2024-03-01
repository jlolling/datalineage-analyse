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
	private String tableSeparator = ",";
	
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
		contextVarResolver.setJobName(job.getJobName());
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
				String combinedHostAndSchema = getDatabaseSchemaForComponentInput(c);
				String host = DatabaseTable.getHost(combinedHostAndSchema);
				String schema = DatabaseTable.getSchema(combinedHostAndSchema);
				// analyse query component
				String queryValue = c.getComponentValueByName(attrName);
//				System.out.println("\n\n############ " + c.getUniqueId() + " #########\n" + queryValue);
				String query = contextVarResolver.replace(queryValue);
				query = SQLCodeUtil.convertJavaToSqlCode(query);
				// get the host and database
				StrictSQLParser parser = new StrictSQLParser();
				parser.setDefaultSchema(schema);
				parser.parseScriptFromCode(query);
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
					String cleanName = contextVarResolver.replace(tableName);
					String dbSchema = getDatabaseSchemaForComponentInput(c);
					addInputTables(getDatabaseTables(dbSchema, cleanName));
				}
			}
			key = c.getComponentName() + ".TARGET_TABLE";
			attrName = properties.getProperty(key);
			if (attrName != null) {
				// analyse output component
				String tableName = c.getComponentValueByName(attrName);
				String cleanName = contextVarResolver.replace(tableName);
				String dbSchema = getDatabaseSchemaForComponentOutput(c);
				addOutputTables(getDatabaseTables(dbSchema, cleanName));
			}
			if (c.getComponentName().equals("tCreateTable")) {
				String tableName = c.getComponentValueByName("TABLE");
				String cleanName = contextVarResolver.replace(tableName);
				String dbSchema = getDatabaseSchemaForComponentOutput(c);
				addCreateTables(getDatabaseTables(dbSchema, cleanName));
			}
		}
	}
	
	private List<DatabaseTable> getDatabaseTables(String dbschema, String listString) {
		listString = listString.replace("\"", "");
		List<DatabaseTable> list = new ArrayList<>();
		if (tableSeparator != null) {
			String[] names = listString.split(tableSeparator);
			for (String name : names) {
				if (name != null && name.trim().isEmpty() == false) {
					if (dbschema != null && name.contains(".") == false) {
						if (dbschema.endsWith(":")) {
							// dbschema contains only the host
							name = dbschema + name;
						} else {
							// dbschema contains a real schema and not only the host
							name = dbschema + "." + name;
						}
					}
					DatabaseTable t = new DatabaseTable(name);
					list.add(t);
				}
			}
		}
		return list;
	}
	
	private void addInputTable(DatabaseTable t) {
		if (listInputTables.contains(t) == false) {
			listInputTables.add(t);
		}
	}
	
	private void addInputTables(List<DatabaseTable> list) {
		for (DatabaseTable t : list) {
			if (listInputTables.contains(t) == false) {
				listInputTables.add(t);
			}
		}
	}
	
	private void addOutputTable(DatabaseTable t) {
		if (listOutputTables.contains(t) == false) {
			listOutputTables.add(t);
		}
	}
	
	private void addOutputTables(List<DatabaseTable> list) {
		for (DatabaseTable t : list) {
			if (listOutputTables.contains(t) == false) {
				listOutputTables.add(t);
			}
		}
	}
	
	private void addCreateTable(DatabaseTable t) {
		if (listCreateTables.contains(t) == false) {
			listCreateTables.add(t);
		}
	}
	
	private void addCreateTables(List<DatabaseTable> list) {
		for (DatabaseTable t : list) {
			if (listCreateTables.contains(t) == false) {
				listCreateTables.add(t);
			}
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
			return contextVarResolver.replace(dbComponent.getComponentValueByName(attr)).replace("\"", "");
		}
		return null;
	}
	
	private String getDatabaseName(Component dbComponent) throws Exception {
		String attr = properties.getProperty(dbComponent.getComponentName() + ".DBNAME");
		if (attr != null) {
			String name = contextVarResolver.replace(dbComponent.getComponentValueByName(attr)).replace("\"", "");
			if (name != null && name.trim().isEmpty() == false) {
				return name;
			}
		}
		return null;
	}

	private String getSchemaName(Component dbComponent) throws Exception {
		String attr = properties.getProperty(dbComponent.getComponentName() + ".SCHEMA");
		if (attr != null) {
			String schema = contextVarResolver.replace(dbComponent.getComponentValueByName(attr)).replace("\"", "");
			if (schema != null && schema.trim().isEmpty() == false) {
				return schema;
			}
		}
		return null;
	}
	
	private String getDatabaseSchemaForComponentOutput(Component c) throws Exception {
		if (c == null) {
			throw new Exception("component cannot be null");
		}
		boolean useAlwaysConnection = "true".equals(properties.get(c.getComponentName() + ".USE_ALWAYS_CONN"));
		String attr = (String) properties.get(c.getComponentName() + ".USE_EXISTING_CONNECTION");
		if (attr == null) {
			attr = "USE_EXISTING_CONNECTION";
		}
		boolean useExternalConnection = useAlwaysConnection || "true".equals(c.getComponentValueByName(attr));
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
			dbSchema = host + ":" + (dbSchema != null ? dbSchema : "");
		}
		return dbSchema;
	}

	private String getDatabaseSchemaForComponentInput(Component c) throws Exception {
		if (c == null) {
			throw new Exception("component cannot be null");
		}
		boolean useAlwaysConnection = "true".equals(properties.get(c.getComponentName() + ".USE_ALWAYS_CONN"));
		boolean useExternalConnection = useAlwaysConnection || "true".equals(c.getComponentValueByName("USE_EXISTING_CONNECTION"));
		if (useExternalConnection) {
			Component cc = getReferencedConnectionComponentForInput(c);
			return getDatabaseSchemaForComponentInput(cc);
		}
		String database = getDatabaseName(c);
		String schema = getSchemaName(c);
		String dbSchema = null;
		if (database != null) {
			dbSchema = database;
		}
		if (schema != null && schema.trim().isEmpty() == false) {
			if (dbSchema != null) {
				dbSchema = dbSchema + "." + schema;
			} else {
				dbSchema = schema;
			}
		}
		String host = getHost(c);
		if (host != null) {
			dbSchema = host + ":" + (dbSchema != null ? dbSchema : "");
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
