package de.jlo.analyse.tableau;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import de.jlo.analyse.DatabaseTable;
import de.jlo.analyse.sql.StrictSQLParser;

public class TableauDatasource {
	
	private String tdsFilePath = null;
	private String name = null;
	private Document doc = null;
	private List<DatabaseTable> tables = new ArrayList<>();
	
	public TableauDatasource(String tdsFilePath) {
		if (tdsFilePath == null || tdsFilePath.trim().isEmpty()) {
			throw new IllegalArgumentException("tdsFilePath cannot be null or empty");
		}
		this.tdsFilePath = tdsFilePath;
		name = Utils.getFileNameWithoutExt(this.tdsFilePath);
	}
	
	public Document getDocument() throws Exception {
		if (doc == null) {
			doc = Utils.readDocument(tdsFilePath);
		}
		return doc;
	}
	
	public void parseDatasource() throws Exception {
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
			// we have table data, now find out on which database
			List<Node> serverNodes = connectionNode.selectNodes("named-connections/named-connection/connection");
			if (serverNodes.size() > 0) {
				Element serverNode = (Element) serverNodes.get(0);
				String host = serverNode.attributeValue("server");
				if (host != null && host.trim().isEmpty() == false) {
					for (Node objectNode : objectNodes) {
						String sql = objectNode.getText();
						String type = ((Element) objectNode).attributeValue("type");
						if ("text".equals(type)) {
							if (sql != null) {
								extractTables(sql, host);
							}
						} else if ("table".equals(type)) {
							// extract the dbname
							String dbname = serverNode.attributeValue("dbname");
							// extract table
							String tableName = ((Element) objectNode).attributeValue("table");
							// Tableau added square brackets to the table name
							tableName = tableName.replace("[", "").replace("]", "");
							DatabaseTable table = new DatabaseTable(host, dbname + "." + tableName);
							addTable(table);
						}
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
