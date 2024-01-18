package de.jlo.analyse.sql;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import net.sf.jsqlparser.expression.Function;
import net.sf.jsqlparser.parser.CCJSqlParser;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.StringProvider;
import net.sf.jsqlparser.parser.feature.Feature;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.create.view.CreateView;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class StrictSQLParser {
	
	private List<String> listInputTables = new ArrayList<>();
	private List<String> listOutputTables = new ArrayList<>();
	private List<String> listCreateTables = new ArrayList<>();
	private List<String> listCreateTempTables = new ArrayList<>();
	private List<String> listFunctions = new ArrayList<>();
	private String defaultSchema = null;
	private StringBuilder errorText = new StringBuilder();
	private long timeout = 10000l;
	
	public StrictSQLParser() {}
	
	private String getFullQualifiedName(String name) {
		if (defaultSchema != null) {
			int pos = name.indexOf(".");
			if (pos == -1) {
				return SQLCodeUtil.cleanupEnclosures(defaultSchema + "." + name);
			} else {
				return SQLCodeUtil.cleanupEnclosures(name);
			}
		} else {
			return SQLCodeUtil.cleanupEnclosures(name);
		}
	}
	
	public void reset() {
		listInputTables = new ArrayList<>();
		listOutputTables = new ArrayList<>();
		listCreateTables = new ArrayList<>();
		listCreateTempTables = new ArrayList<>();
		listFunctions = new ArrayList<>();
	}
	
	public String getParserErrorLog() {
		if (errorText.length() > 0) {
			return errorText.toString();
		} else {
			return null;
		}
	}
	
	public void parseScriptFromFile(String scriptPath) throws Exception {
		String code = SQLCodeUtil.readContentfromFile(scriptPath, null);
		SimpleSQLParser sp = new SimpleSQLParser();
		sp.parseScript(code);
		List<SQLStatement> list = sp.getStatements();
		int index = 0;
		for (SQLStatement stat : list) {
			try {
				index++;
				parseStatementFromCode(stat.getSQL());
			} catch (Exception e) {
				String message = "Statement #" + index + " starting at line: " + stat.getStartLineNumber() + " SQL:\n" + stat.getSQL() + "\nfails: " + e.getMessage();
				if (errorText.length() > 0) {
					errorText.append("\n##############################\n");
				}
				errorText.append(message);
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				errorText.append("\nStacktrace:\n");
				errorText.append(sw.toString());
			}
		}
	}
	
	public void parseStatementFromCode(String sql) throws Exception {
		if (sql == null || sql.trim().isEmpty()) {
			throw new IllegalArgumentException("SQL statement code cannot be null or empty");
		}
		String cleanedCode = SQLCodeUtil.removeIntoFromSelect(SQLCodeUtil.replaceHashCommentsAndAssignments(SQLCodeUtil.cleanupEmptyLines(sql)));
		try {
			CCJSqlParser parser = new CCJSqlParser(new StringProvider(cleanedCode))
					.withTimeOut(timeout)
					.withAllowComplexParsing(true)
					.withSquareBracketQuotation(true);
//			parser.getConfiguration().setValue(Feature.timeOut, timeout);
//			parser.getConfiguration().setValue(Feature.allowComplexParsing, true);
//			parser.getConfiguration().setValue(Feature.allowSquareBracketQuotation, true);
			Statement stmt = CCJSqlParserUtil.parseStatements(parser, Executors.newSingleThreadExecutor()).get(0);			
			analyseStatement(stmt);
		} catch (Exception e) {
			if (e.getMessage().contains("unexpected token: \"@\"")) {
				cleanedCode = cleanedCode.replace("@", ":");
				Statement stmt = CCJSqlParserUtil.parseStatements(cleanedCode).get(0);
				analyseStatement(stmt);
			} else {
				throw e;
			}
		}
	}
	
	private void analyseStatement(Statement stmt) throws Exception {
		if (stmt instanceof Insert || stmt instanceof Update || stmt instanceof Delete || stmt instanceof Truncate) {
			TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
			tablesNamesFinder.analyse(stmt);
			List<String> tableList = tablesNamesFinder.getListTableNamesOutput();
			for (String tableName : tableList) {
				String name = getFullQualifiedName(tableName);
				if (listOutputTables.contains(name) == false) {
					listOutputTables.add(name);
				}
			}
			tableList = tablesNamesFinder.getListTableNamesInput();
			for (String tableName : tableList) {
				String name = getFullQualifiedName(tableName);
				if (listInputTables.contains(name) == false && listCreateTempTables.contains(name) == false) {
					listInputTables.add(name);
				}
			}
		} else if (stmt instanceof Select) {
			TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
			tablesNamesFinder.analyse(stmt);
			List<String> tableList = tablesNamesFinder.getListTableNamesInput();
			for (String tableName : tableList) {
				String name = getFullQualifiedName(tableName);
				if (listInputTables.contains(name) == false && listCreateTempTables.contains(name) == false) {
					listInputTables.add(name);
				}
			}
			List<String> functionList = tablesNamesFinder.getListFunctionSignatures();
			for (String name : functionList) {
				if (listFunctions.contains(name) == false) {
					listFunctions.add(name);
				}
			}
		} else if (stmt instanceof Function) {
			TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
			tablesNamesFinder.analyse(stmt);
			List<String> functionList = tablesNamesFinder.getListFunctionSignatures();
			for (String name : functionList) {
				if (listFunctions.contains(name) == false) {
					listFunctions.add(name);
				}
			}
			List<String> tableList = tablesNamesFinder.getListTableNamesInput();
			for (String tableName : tableList) {
				String name = getFullQualifiedName(tableName);
				if (listInputTables.contains(name) == false && listCreateTempTables.contains(name) == false) {
					listInputTables.add(name);
				}
			}
		} else if (stmt instanceof CreateTable) {
			TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
			tablesNamesFinder.analyse(stmt);
			List<String> tableList = tablesNamesFinder.getListTableNamesCreate();
			for (String tableName : tableList) {
				String name = getFullQualifiedName(tableName);
				if (listCreateTables.contains(name) == false) {
					listCreateTables.add(name);
				}
			}
			tableList = tablesNamesFinder.getListTableNamesTemp();
			for (String tableName : tableList) {
				String name = getFullQualifiedName(tableName);
				if (listCreateTempTables.contains(name) == false) {
					listCreateTempTables.add(name);
				}
			}
			tableList = tablesNamesFinder.getListTableNamesInput();
			for (String tableName : tableList) {
				String name = getFullQualifiedName(tableName);
				// prevent we list temporary tables here
				if (listInputTables.contains(name) == false && listCreateTempTables.contains(name) == false) {
					listInputTables.add(name);
				}
			}
		} else if (stmt instanceof CreateView) {
			TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
			tablesNamesFinder.analyse(stmt);
			List<String> tableList = tablesNamesFinder.getListTableNamesCreate();
			for (String tableName : tableList) {
				String name = getFullQualifiedName(tableName);
				if (listCreateTables.contains(name) == false) {
					listCreateTables.add(name);
				}
			}
			tableList = tablesNamesFinder.getListTableNamesInput();
			for (String tableName : tableList) {
				String name = getFullQualifiedName(tableName);
				if (listInputTables.contains(name) == false) {
					listInputTables.add(name);
				}
			}
		}
	}
	
	public void parseScriptFromCode(String sqlScriptCode) throws Exception {
		SimpleSQLParser sp = new SimpleSQLParser();
		sp.parseScript(sqlScriptCode);
		List<SQLStatement> list = sp.getStatements();
		int index = 0;
		for (SQLStatement stat : list) {
			try {
				index++;
				parseStatementFromCode(stat.getSQL());
			} catch (Exception e) {
				String message = "Statement #" + index + " starting at line: " + stat.getStartLineNumber() + " SQL:\n" + stat.getSQL() + "\nfails: " + e.getMessage();
				if (errorText.length() > 0) {
					errorText.append("\n##############################\n");
				}
				errorText.append(message);
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);
				errorText.append("\nStacktrace:\n");
				errorText.append(sw.toString());
			}
		}
	}
	
	public List<String> getTablesRead() {
		return listInputTables;
	}
	
	public List<String> getTablesWritten() {
		return listOutputTables;
	}
	
	public List<String> getTablesCreated() {
		return listCreateTables;
	}

	public String getDefaultSchema() {
		return defaultSchema;
	}

	public void setDefaultSchema(String defaultSchema) {
		if (defaultSchema != null && defaultSchema.trim().isEmpty() == false) {
			this.defaultSchema = defaultSchema;
		}
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(Long timeout) {
		if (timeout != null) {
			this.timeout = timeout;
		}
	}
	
}
