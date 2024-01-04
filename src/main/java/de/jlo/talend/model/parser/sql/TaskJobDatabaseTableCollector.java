package de.jlo.talend.model.parser.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Node;

import de.jlo.talend.model.Model;
import de.jlo.talend.model.ContextParameter;
import de.jlo.talend.model.Job;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

public class TaskJobDatabaseTableCollector {
	
	private static final Logger LOG = LogManager.getLogger(TaskJobDatabaseTableCollector.class);
	private Map<Job, List<String>> inputTables = new HashMap<>();
	private Map<String, Map<String, List<String>>> inputTablesPerComponent = new HashMap<>();
	private Map<Job, List<String>> functions = new HashMap<>();
	private Map<Job, List<String>> outputTables = new HashMap<>();
	private boolean preferSQLParser = true;
	private boolean preferPatternParser = true;
	private Model model = null;
	
	public TaskJobDatabaseTableCollector(Model model) {
		this.model = model;
	}
	
	public List<Job> getAllJobs() {
		return model.getAllJobs();
	}

	public void search(String jobNamePattern, boolean onlyLatestVersion) throws Exception {
		// read context for all jabs
		List<Job> list = model.getJobs(jobNamePattern, onlyLatestVersion);
		for (Job job : list) {
			LOG.debug("Find tables in " + job);
			try {
				findTables(job);
			} catch (Exception e) {
				LOG.error("Find tables in job: " + job.getJobName() + " failed.", e);
			}
		}
	}
	
	public void findTables(Job job) throws Exception {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Find tables for job: " + job);
		}
		job.setItemDoc(model.readItem(job));
		// read context variables
		List<ContextParameter> context = job.getContext();
		ContextVarResolver cr = new ContextVarResolver();
		for (ContextParameter p : context) {
			cr.addContextVar(p.getName(), p.getValue());
		}
		List<Node> listInputComps = model.getComponents(job.getItemDoc(), "Output", "Input", "Row");
		for (Node component : listInputComps) {
			findTables(job, cr, (Element) component);
		}
	}
	
	private void addInputTable(Job job, String componentId, String tableName) {
		List<String> list = inputTables.get(job);
		if (list == null) {
			list = new ArrayList<String>();
			inputTables.put(job, list);
		}
		if (list.contains(tableName) == false) {
			list.add(tableName);
		}
		Map<String, List<String>> compTableMap = inputTablesPerComponent.get(job.getJobName());
		if (compTableMap == null) {
			compTableMap = new HashMap<>();
			inputTablesPerComponent.put(job.getJobName(), compTableMap);
		}
		list = compTableMap.get(componentId);
		if (list == null) {
			list = new ArrayList<String>();
			compTableMap.put(componentId, list);
		}
		if (list.contains(tableName) == false) {
			list.add(tableName);
		}
	}
	
	private void addOutputTable(Job job, String tableName) {
		List<String> list = outputTables.get(job);
		if (list == null) {
			list = new ArrayList<String>();
			outputTables.put(job, list);
		}
		if (list.contains(tableName) == false) {
			list.add(tableName);
		}
	}
	
	private void addFunction(Job job, String name) {
		List<String> list = functions.get(job);
		if (list == null) {
			list = new ArrayList<String>();
			functions.put(job, list);
		}
		if (list.contains(name) == false) {
			list.add(name);
		}
	}
	
	public List<String> findTablesInSelect(String sql) throws JSQLParserException {
		if (preferSQLParser) {
			Statement stmt = CCJSqlParserUtil.parse(sql);
			TableAndProcedureNameFinder finder = new TableAndProcedureNameFinder();
			finder.analyse(stmt);
			return finder.getListTableNamesInput();
		} else {
			return SimpleSQLParser.findFromTables(sql);
		}
	}

	public void findTables(Job job, ContextVarResolver cr, Element component) throws Exception {
		List<Element> params = component.elements();
		String compId = null;
		String componentName = component.attributeValue("componentName");
		if (componentName.contains("Output")) {
			String outputTableName = null;
			String outputTableSchema = null;
			for (Element param : params) {
				String pname = param.attributeValue("name");
				String field = param.attributeValue("field");
				String value = param.attributeValue("value");
				if ("UNIQUE_NAME".equals(pname)) {
					compId = value;
				}
				if (field.contains("TABLE")) {
					outputTableName = value;
				}
				if ("SCHEMA_DB".equals(pname)) {
					outputTableSchema = cr.getVariableValue(value);
				}
			}
			if (outputTableName != null) {
				if (outputTableSchema != null) {
					addOutputTable(job, outputTableSchema + "." + outputTableName);
				} else {
					addOutputTable(job, outputTableName);
				}
			}
		} else {
			String query = null;
			// Input and Row components
			for (Element param : params) {
				String pname = param.attributeValue("name");
				String field = param.attributeValue("field");
				String value = param.attributeValue("value");
				if ("UNIQUE_NAME".equals(pname)) {
					compId = value; 
				}
				if ("MEMO_SQL".equals(field)) {
					query = value;
				}
			}
			if (query != null && query.trim().isEmpty() == false) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Component: " + compId + " process query: " + query);
				}
				// now we have read a SQL query
				// replace the context variables
				try {
					query = cr.replaceContextVars(query);
				} catch (Exception e) {
					throw new Exception("Replace context vars failed for query in component: " + compId, e);
				}
				// replace globalMap variables
				query = SQLCodeUtil.replaceGlobalMapVars(query);
				// now convert to SQL
				String sql = SQLCodeUtil.convertJavaToSqlCode(query);
				// now parse the sql code
				if (preferSQLParser) {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Component: " + compId + " parse SQL: " + sql);
					}
					try {
						Statement stmt = CCJSqlParserUtil.parse(sql);
						TableAndProcedureNameFinder finder = new TableAndProcedureNameFinder();
						finder.analyse(stmt);
						List<String> listTables = finder.getListTableNamesInput();
						for (String t : listTables) {
							addInputTable(job, compId, t);
						}
						List<String> listFunctions = finder.getListFunctionSignatures();
						for (String f : listFunctions) {
							addFunction(job, f);
						}
					} catch (Exception pe) {
						// try to find the tables with patterns if a full parsing fails
						List<String> listTables = SimpleSQLParser.findFromTables(sql);
						for (String t : listTables) {
							addInputTable(job, compId, t);
						}
					}
				} else {
					if (LOG.isDebugEnabled()) {
						LOG.debug("Component: " + compId + " pattern matching SQL: " + sql);
					}
					// try to find the tables with patterns if a full parse fails
					SimpleSQLParser p = new SimpleSQLParser();
					p.setIncludeComments(false);
					p.parseScript(sql);
					List<SQLStatement> stats = p.getStatements();
					for (SQLStatement stat : stats) {
						List<String> listTables = SimpleSQLParser.findFromTables(stat.getSQL());
						for (String t : listTables) {
							addInputTable(job, compId, t);
						}
						String outTable = SimpleSQLParser.findInsertUpdateTables(stat.getSQL());
						if (outTable != null) {
							addOutputTable(job, outTable);
						}
					}
				}
			}
		}
	}

	/**
	 * Return detected input tables
	 * @return Map<job-name<List tables>>
	 */
	public Map<Job, List<String>> getInputTables() {
		return inputTables;
	}

	public Map<Job, List<String>> getFunctions() {
		return functions;
	}

	/**
	 * Return detected output tables
	 * @return Map<job-name<List tables>>
	 */
	public Map<Job, List<String>> getOutputTables() {
		return outputTables;
	}

	public boolean isPreferPatternParser() {
		return preferPatternParser;
	}

	public void setPreferPatternParser(boolean allowPatternParser) {
		this.preferPatternParser = allowPatternParser;
	}

	public boolean isPreferSQLParser() {
		return preferSQLParser;
	}

	public void setPreferSQLParser(boolean preferSQLParser) {
		this.preferSQLParser = preferSQLParser;
	}

	public Map<String, Map<String, List<String>>> getInputTablesPerComponent() {
		return inputTablesPerComponent;
	}
	
	public String getSummary() {
		StringBuilder sb = new StringBuilder();
		sb.append("Input tables #############\n");
		for (Map.Entry<Job, List<String>> entry : inputTables.entrySet()) {
			sb.append("Job: " + entry.getKey() + "\n");
			for (String t : entry.getValue()) {
				sb.append("\t" + t + "\n");
			}
		}
		sb.append("Output tables #############\n");
		for (Map.Entry<Job, List<String>> entry : outputTables.entrySet()) {
			sb.append("Job: " + entry.getKey() + "\n");
			for (String t : entry.getValue()) {
				sb.append("\t" + t + "\n");
			}
		}
		return sb.toString();
	}
	
}