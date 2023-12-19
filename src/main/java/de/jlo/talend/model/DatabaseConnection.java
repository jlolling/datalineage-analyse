package de.jlo.talend.model;

import org.dom4j.Element;
import org.dom4j.QName;

public class DatabaseConnection {
	
	private String id = null;
	private String name = null;
	private boolean contextMode = false;
	private String contextId = null;
	private String url = null;
	private String port = null;
	private String userName = null;
	private String password = null;
	private String serverName = null;
	private String database = null;
	private String schema = null;
	
	public DatabaseConnection(Element databaseConnectionNode) {
		if (databaseConnectionNode == null) {
			throw new IllegalArgumentException("Element databaseConnectionNode is null!");
		}
    	QName nameId = new QName("id", null);
		id = databaseConnectionNode.attributeValue(nameId);
		name = databaseConnectionNode.attributeValue("name");
		contextMode = "true".equals(databaseConnectionNode.attributeValue("ContextMode"));
		contextId = databaseConnectionNode.attributeValue("ContextId");
		url = databaseConnectionNode.attributeValue("URL");
		port = databaseConnectionNode.attributeValue("Port");
		userName = databaseConnectionNode.attributeValue("Username");
		password = databaseConnectionNode.attributeValue("Password");
		serverName = databaseConnectionNode.attributeValue("ServerName");
		database = databaseConnectionNode.attributeValue("SID");
		schema = databaseConnectionNode.attributeValue("UiSchema");
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public boolean isContextMode() {
		return contextMode;
	}

	public String getContextId() {
		return contextId;
	}

	public String getUrl() {
		return url;
	}

	public String getPort() {
		return port;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return password;
	}

	public String getServerName() {
		return serverName;
	}

	public String getDatabase() {
		return database;
	}

	public String getSchema() {
		return schema;
	}
	
}
