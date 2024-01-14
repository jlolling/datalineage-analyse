package de.jlo.analyse.talend;

public class ContextParameter {
	
	private String id = null;
	private String name = null;
	private String value = null;
	private String talendType = null;
	private String comment = null;
	
	public ContextParameter(String id, String name) {
		if (id == null || id.trim().isEmpty()) {
			throw new IllegalArgumentException("id cannot be null or empty");
		}
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("name cannot be null or empty");
		}
		this.id = id;
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
	public void setValue(String value) {
		this.value = value;
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
	}	

}
