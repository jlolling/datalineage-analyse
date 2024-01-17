package de.jlo.analyse;

public class DatabaseTable {
	
	private String tableName = null;
	private String databaseHost = null;
	
	public DatabaseTable(String host, String table) {
		this.databaseHost = host;
		this.tableName = table;
	}
	
	@Override
	public String toString() {
		return databaseHost + ":" + tableName;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof DatabaseTable) {
			return ((DatabaseTable) o).toString().equals(toString());
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	public String getTableName() {
		return tableName;
	}

	public String getDatabaseHost() {
		return databaseHost;
	}

}
