package de.jlo.analyse.talend;

import de.jlo.analyse.JsonUtil;

public class ComponentAttribute {
	
	private String name;
	private String field;
	private String value;
	private String jsonPath = null;
	private static JsonUtil ju = new JsonUtil();
	
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
	
	public String getValue() throws Exception {
		if (jsonPath != null && value != null) {
			try {
				ju.parse(value);
				return ju.getNodeValue(jsonPath);
			} catch (Exception e) {
				throw new Exception("Fail to parse value for component-attribute: " + name, e);
			}
		}
		return value;
	}
	
	public void setValue(String value) throws Exception {
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

	public String getJsonPath() {
		return jsonPath;
	}

	public void setJsonPath(String jsonPath) {
		if (jsonPath != null && jsonPath.trim().isEmpty() == false) {
			this.jsonPath = jsonPath;
		}
	}

}
