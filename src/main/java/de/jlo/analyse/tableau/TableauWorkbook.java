package de.jlo.analyse.tableau;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

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
	}
	
	public Document getDocument() throws Exception {
		if (doc == null) {
			doc = Utils.readDocument(twbFilePath);
		}
		return doc;
	}
	
	private List<Node> getConnections() throws Exception {
		Element root = getDocument().getRootElement();
		List<Node> connectionNodes = root.selectNodes("/workbook/datasources/datasource/connection");
		return connectionNodes;
	}
	
	private void analyseConnection(Node connectionNode) throws Exception {
		
	}

	public List<DatabaseTable> getTableNames() {
		return tables;
	}

}
