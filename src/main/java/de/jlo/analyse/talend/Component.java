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
	
	public Component(Job job, Element element) {
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
	
	private void retrieveComponentAttributes() {
		List<Element> params = element.elements();
		for (Element param : params) {
			ComponentAttribute a = new ComponentAttribute();
			a.setName(param.attributeValue("name"));
			a.setField(param.attributeValue("field"));
			a.setValue(param.attributeValue("value"));
			listAttributes.add(a);
		}
	}
	
	public String getComponentValueByName(String attributeName) {
		ComponentAttribute a = getComponentAttributeByName(attributeName);
		if (a != null) {
			return a.getValue();
		} else {
			return null;
		}
	}
	
	public ComponentAttribute getComponentAttributeByName(String attributeName) {
		for (ComponentAttribute a : listAttributes) {
			if (a.getName().equals(attributeName)) {
				return a;
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
