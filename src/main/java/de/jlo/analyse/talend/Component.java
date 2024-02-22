package de.jlo.analyse.talend;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Element;

public class Component {
	
	private Element element = null;
	private Job job = null;
	private String uniqueId = null;
	private String componentName = null;
	private List<ComponentAttribute> listAttributes = new ArrayList<>();
	
	public Component(Job job, Element element) throws Exception {
		if (element == null) {
			throw new IllegalArgumentException("element cannot be null");
		}
		if (job == null) {
			throw new IllegalArgumentException("job cannot be null");
		}
		this.element = element;
		this.job = job;
		retrieveComponentAttributes();
		componentName = this.element.attributeValue("componentName");
		uniqueId = getComponentValueByName("UNIQUE_NAME");
	}
	
	public Job getJob() {
		return job;
	}

	public String getUniqueId() {
		return uniqueId;
	}
	
	public String getComponentName() {
		return componentName;
	}
	
	private void retrieveComponentAttributes() throws Exception {
		List<Element> params = element.elements();
		for (Element param : params) {
			ComponentAttribute a = new ComponentAttribute();
			a.setName(param.attributeValue("name"));
			a.setField(param.attributeValue("field"));
			a.setValue(param.attributeValue("value"));
			listAttributes.add(a);
		}
	}
	
	public String getComponentValueByName(String attributeName) throws Exception {
		ComponentAttribute a = getComponentAttributeByName(attributeName);
		if (a != null) {
			return a.getValue();
		} else {
			return null;
		}
	}
	
	public ComponentAttribute getComponentAttributeByName(String attributeName) throws Exception {
		String[] attributeNames = attributeName.split(",");
		for (String name : attributeNames) {
			String actualName = name;
			String jsonPath = null;
			// we get for new components the attribute values inside a json object
			int pos = name.indexOf('$');
			if (pos > 0) {
				actualName = name.substring(0, pos);
				jsonPath = name.substring(pos);
			}
			for (ComponentAttribute a : listAttributes) {
				a.setJsonPath(jsonPath); // if not null the the value will be extracted by json path
				if (a.getName().equals(actualName) && a.getValue() != null) {
					return a;
				}
			}
		}
		return null;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Component) {
			return ((Component) o).uniqueId.equals(uniqueId);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return uniqueId.hashCode();
	}
	
	@Override
	public String toString() {
		return uniqueId + " (" + componentName + ")";
	}
	
}
