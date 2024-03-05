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
	private boolean throwExeptionInsteadOfErrorText = false;
	private String parsedSQLCode = null;
	
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
				String message = "Statement #" + index + " starting at line: " + stat.getStartLineNumber() + " SQL:\n" + getParsedSQLCode() + "\nfails: " + e.getMessage();
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
	
	public void parseStatementFromCodeSimple(String sql) {
		List<String> tables = SimpleSQLParser.findFromTables(parsedSQLCode);
		for (String name : tables) {
			if (listInputTables.contains(name) == false && listCreateTempTables.contains(name) == false) {
				listInputTables.add(name);
			}
		}
		String tableName = SimpleSQLParser.findCreateViewName(parsedSQLCode);
		if (tableName != null) {
			if (listCreateTables.contains(tableName) == false) {
				listCreateTables.add(tableName);
			}
		}
		tableName = SimpleSQLParser.findCreateTableName(parsedSQLCode);
		if (tableName != null) {
			if (listCreateTables.contains(tableName) == false) {
				listCreateTables.add(tableName);
			}
		}
	}
	
	public void parseStatementFromCode(String sql) throws Exception {
		if (sql == null || sql.trim().isEmpty()) {
			throw new IllegalArgumentException("SQL statement code cannot be null or empty");
		}
		parsedSQLCode = SQLCodeUtil.removeDisturbingTerms(
								SQLCodeUtil.removeIntoFromSelect(
								SQLCodeUtil.replaceHashCommentsAndAssignments(
								SQLCodeUtil.removeBraketsAroundNumbers(
								SQLCodeUtil.cleanupEmptyLines(sql)))));
		try {
			CCJSqlParser parser = new CCJSqlParser(new StringProvider(parsedSQLCode))
					.withTimeOut(timeout)
					.withAllowComplexParsing(true)
					.withSquareBracketQuotation(true);
			Statement stmt = CCJSqlParserUtil.parseStatements(parser, Executors.newSingleThreadExecutor()).get(0);			
			analyseStatement(stmt);
		} catch (Exception e) {
			if (e.getMessage().contains("unexpected token: \"@\"")) {
				parsedSQLCode = parsedSQLCode.replace("@", ":");
				Statement stmt = CCJSqlParserUtil.parseStatements(parsedSQLCode).get(0);
				analyseStatement(stmt);
			} else {
				parseStatementFromCodeSimple(parsedSQLCode);
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
				if (throwExeptionInsteadOfErrorText) {
					throw new Exception(message, e);
				} else {
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

	public boolean isThrowExeptionInsteadOfErrorText() {
		return throwExeptionInsteadOfErrorText;
	}

	public void setThrowExeptionInsteadOfErrorText(boolean throwExeptionInsteadOfErrorText) {
		this.throwExeptionInsteadOfErrorText = throwExeptionInsteadOfErrorText;
	}

	public String getParsedSQLCode() {
		return parsedSQLCode;
	}

	public void setParsedSQLCode(String parsedSQLCode) {
		this.parsedSQLCode = parsedSQLCode;
	}
	
}
