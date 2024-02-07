package de.jlo.analyse.talend;

public class ComponentAttribute {
	
	private String name;
	private String field;
	private String value;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getField() {
		return field;
	}
	
	public void setField(String field) {
		this.field = field;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		if (value != null && value.trim().isEmpty() == false) {
			this.value = value;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ComponentAttribute) {
			if (((ComponentAttribute) o).name.equals(name)) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}

}
