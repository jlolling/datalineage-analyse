package de.jlo.analyse;

public class DatabaseTable {
	
	private String tableName = null;
	private String databaseHost = null;
	
	public DatabaseTable(String host, String table) {
		this.databaseHost = host;
		this.tableName = table;
		if (table == null || table.trim().isEmpty()) {
			throw new IllegalArgumentException("table cannot be null or empty");
		}
		if (table.startsWith("null.")) {
			throw new IllegalArgumentException("table name contains null schema! table: " + table);
		}
	}
	
	public DatabaseTable(String combinedName) {
		int posHost = combinedName.indexOf(":");
		if (posHost > 0) {
			this.databaseHost = getHost(combinedName);
			this.tableName = combinedName.substring(posHost + 1);
		} else {
			this.tableName = combinedName;
		}
		if (tableName.startsWith("null.")) {
			throw new IllegalArgumentException("table name contains String null as schema! table: " + tableName);
		}
	}
	
	public static String getHost(String combinedName) {
		if (combinedName != null) {
			int posHost = combinedName.indexOf(":");
			if (posHost > 0) {
				return combinedName.substring(0, posHost);
			}
		}
		return null;
	}
	
	public static String getSchema(String combinedName) {
		if (combinedName != null) {
			int posHost = combinedName.indexOf(":");
			if (posHost > 0) {
				return combinedName.substring(posHost + 1);
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return (databaseHost != null ? databaseHost + ":" : "") + tableName;
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
