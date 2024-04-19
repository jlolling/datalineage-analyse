package de.jlo.analyse.talend;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ContextParameter {
	
	private String id = null;
	private String name = null;
	private String value = null;
	private String talendType = null;
	private String comment = null;
	private String delimiter = null;
	private List<String> listValues = null; 
	private final String listKey = "list-delimiter=";
	
	public ContextParameter(String id, String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("name cannot be null or empty");
		}
		this.id = id;
		this.name = name;
	}
	
	public ContextParameter(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("name cannot be null or empty");
		}
		this.name = name;
	}

	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public boolean isValueList() {
		return delimiter != null; 
	}
	
	public List<String> getValues() {
		if (listValues == null) {
			listValues = new ArrayList<>();
			if (value != null && delimiter != null) {
				StringTokenizer st = new StringTokenizer(value, delimiter);
				while (st.hasMoreTokens()) {
					String v = st.nextToken();
					if (v != null && v.trim().isEmpty() == false) {
						listValues.add(v);
					}
				}
			}
		}
		return listValues;
	}
	
	public void setValue(String value) {
		if (value != null && value.replace("\"", "").trim().isEmpty() == false) {
			this.value = value.trim();
		}
	}

	public String getTalendType() {
		return talendType;
	}
	
	public void setTalendType(String talendType) {
		this.talendType = talendType;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
		if (comment != null) {
			int pos = comment.indexOf(listKey);
			if (pos != -1) {
				pos = pos + listKey.length();
				if (pos < comment.length()) {
					delimiter = comment.substring(pos, pos+1);
					if (delimiter != null && delimiter.isEmpty()) {
						delimiter = null; // we do not want empty Strings
					}
				}
			} else if (comment.contains("list")) {
				delimiter = ","; // this is the simplest way to declare a parameter as list
			}
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ContextParameter) {
			return name.equals(((ContextParameter) o).getName());
		} else if (o instanceof String) {
			return name.equals((String) o);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	public String getDelimiter() {
		return delimiter;
	}

	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}
	
}