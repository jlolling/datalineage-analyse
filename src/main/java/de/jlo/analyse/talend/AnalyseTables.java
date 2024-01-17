package de.jlo.analyse.talend;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import de.jlo.analyse.sql.StrictSQLParser;

public class AnalyseTables {
	
	private Job job = null;
	private List<String> listInputTables = new ArrayList<>();
	private List<String> listOutputTables = new ArrayList<>();
	private List<String> listCreateTables = new ArrayList<>();
	private static Properties properties = new Properties();
	private static boolean loadPropertiesDone = false;
	private static final String propertiesFileName = "table-components.properties";
	
	public AnalyseTables(Job job) throws Exception {
		if (job == null) {
			throw new IllegalArgumentException("job cannot be null");
		}
		this.job = job;
		if (loadPropertiesDone == false) {
			loadProperties();
			loadPropertiesDone = true;
		}
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
				// analyse query component
				String queryValue = c.getComponentValueByName(attrName);
				ContextVarResolver r = new ContextVarResolver();
				r.setContext(job.getContext());
				String query = r.replaceContextVars(queryValue);
				// get the host and database
				StrictSQLParser parser = new StrictSQLParser();
				
				parser.parseScriptFromCode(query);
			}
			key = c.getComponentName() + ".SOURCE_TABLE";
			attrName = properties.getProperty(key);
			if (attrName != null) {
				// analyse input component
				
			}
			key = c.getComponentName() + ".TARGET_TABLE";
			attrName = properties.getProperty(key);
			if (attrName != null) {
				// analyse output component
				
			}
				
			
		}
	}
	
	private void addInputTables(List<String> list) {
		for (String t : list) {
			addInputTable(t);
		}
	}
	
	private void addInputTable(String name) {
		if (listInputTables.contains(name) == false) {
			listInputTables.add(name);
		}
	}
	
	private void addOutputTables(List<String> list) {
		for (String t : list) {
			addOutputTable(t);
		}
	}
	
	private void addOutputTable(String name) {
		if (listOutputTables.contains(name) == false) {
			listOutputTables.add(name);
		}
	}
	
	private void addCreateTables(List<String> list) {
		for (String t : list) {
			addCreateTable(t);
		}
	}
	
	private void addCreateTable(String name) {
		if (listCreateTables.contains(name) == false) {
			listCreateTables.add(name);
		}
	}
	
	private Component getReferencedConnectionComponent(Component c) throws Exception {
		String use = c.getComponentValueByName("USE_EXISTING_CONNECTION");
		if ("true".equals(use)) {
			String attr = properties.getProperty(c.getComponentName() + "SOURCE_CONN");
			if (attr == null) {
				attr = properties.getProperty(c.getComponentName() + "TARGET_CONN");
			}
			if (attr != null) {
				String uniqueIdConn = c.getComponentValueByName(attr);
				if (uniqueIdConn != null) {
					Component cc = job.getComponent(uniqueIdConn);
					return cc;
				}
			}
		}
		return null;
	}
	
	private String getHost(Component dbComponent) {
		String attr = properties.getProperty(dbComponent.getComponentName() + ".HOST");
		if (attr != null) {
			return dbComponent.getComponentValueByName(attr);
		}
		return null;
	}
	
	private String getDatabaseName(Component dbComponent) {
		String attr = properties.getProperty(dbComponent.getComponentName() + ".DBNAME");
		if (attr != null) {
			return dbComponent.getComponentValueByName(attr);
		}
		return null;
	}

	private String getSchemaName(Component dbComponent) {
		String attr = properties.getProperty(dbComponent.getComponentName() + ".SCHEMA");
		if (attr != null) {
			return dbComponent.getComponentValueByName(attr);
		}
		return null;
	}

	private void analyseTableInputComponents(Component c) {
		
	}

}
