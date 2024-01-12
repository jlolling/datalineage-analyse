package de.jlo.analyse.tableau;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import de.jlo.analyse.sql.StrictSQLParser;

public class TableauWorkbook {
	
	private String twbFilePath = null;
	private String name = null;
	private Document doc = null;
	private List<DatabaseTable> tables = new ArrayList<>();
	
	public TableauWorkbook(String twbFilePath) {
		if (twbFilePath == null || twbFilePath.trim().isEmpty()) {
			throw new IllegalArgumentException("twbFilePath cannot be null or empty");
		}
		this.twbFilePath = twbFilePath;
		name = Utils.getFileNameWithoutExt(this.twbFilePath);
	}
	
	public Document getDocument() throws Exception {
		if (doc == null) {
			doc = Utils.readDocument(twbFilePath);
		}
		return doc;
	}
	
	public void parseWorkbook() throws Exception {
		List<Node> connectionNodes = getConnections();
		for (Node cn : connectionNodes) {
			analyseConnection((Element) cn);
		}
	}
	
	private List<Node> getConnections() throws Exception {
		Element root = getDocument().getRootElement();
		List<Node> connectionNodes = root.selectNodes("/workbook/datasources/datasource/connection");
		return connectionNodes;
	}
	
	private void analyseConnection(Element connectionNode) throws Exception {
		List<Node> objectNodes = connectionNode.selectNodes("ObjectModelEncapsulateLegacy-relation");
		if (objectNodes.size() > 0) {
			List<Node> serverNodes = connectionNode.selectNodes("named-connections/named-connection/connection");
			if (serverNodes.size() > 0) {
				Element serverNode = (Element) serverNodes.get(0);
				String host = serverNode.attributeValue("server");
				for (Node objectNode : objectNodes) {
					String sql = objectNode.getText();
					if (sql != null) {
						extractTables(sql, host);
					}
				}
			}
		}
	}
	
	private void extractTables(String sql, String host) throws Exception {
		sql = sql.replace("<<", "<").replace(">>", ">");
		StrictSQLParser p = new StrictSQLParser();
		try {
			p.parseScriptFromCode(sql);
		} catch (Throwable t) {
			throw new Exception("Parse SQL failed. SQL: \n" + sql + "\nerror: " + t.getMessage(), t);
		}
		List<String> listTables = p.getTablesRead();
		for (String name : listTables) {
			DatabaseTable table = new DatabaseTable(host, name);
			addTable(table);
		}
	}
	
	private void addTable(DatabaseTable table) {
		if (tables.contains(table) == false) {
			tables.add(table);
		}
	}

	public List<DatabaseTable> getTableNames() {
		return tables;
	}

	public String getName() {
		return name;
	}

}
