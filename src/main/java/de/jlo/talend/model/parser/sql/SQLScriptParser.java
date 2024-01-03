package de.jlo.talend.model.parser.sql;

import java.util.List;

public class SQLScriptParser {
	
	private String sqlScriptCode = null;
	private TableAndProcedureNameFinder finder = new TableAndProcedureNameFinder();
	
	public SQLScriptParser(String code) {
		if (code == null || code.trim().isEmpty()) {
			throw new IllegalArgumentException("SQL code cannot be null or empty");
		}
		sqlScriptCode = code;
	}
	
	public List<String> getTablesRead() {
		return finder.getListTableNamesInput();
	}
	
	public List<String> getTablesWritten() {
		return finder.getListTableNamesOutput();
	}
	
	public List<String> getTablesCreated() {
		return finder.getListTableNamesCreate();
	}

}
