package de.jlo.talend.model;

import java.util.ArrayList;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

public class Job implements Comparable<Job> {
	
	private String id = null;
	private String projectName = null;
	private String jobFolder = null;
	private String jobName = null;
	private String version = null;
	private int majorVersion = 0;
	private int minorVersion = 0;
	private String pathWithoutExtension = null;
	private Document itemDoc = null;
	private List<ContextParameter> context = null;
	private List<TRunJob> embeddedJobs = new ArrayList<>();
	private Model model = null;
	
	public Job(Model model) {
		if (model == null) {
			throw new IllegalArgumentException("model cannot be null");
		}
		this.model = model;
	}
	
	public Model getModel() {
		return model;
	}
	
	public void addTRunjob(TRunJob tRunjob) {
		embeddedJobs.add(tRunjob);
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
		int pos = version.indexOf('.');
		if (pos == -1) {
			throw new IllegalArgumentException("Talendjob has an invalid version: " + version);
		} else {
			majorVersion = Integer.parseInt(version.substring(0, pos));
			minorVersion = Integer.parseInt(version.substring(pos + 1));
		}
	}
	
	public String getPathWithoutExtension() {
		return pathWithoutExtension;
	}
	
	public void setPath(String path) {
		if (path.endsWith(".properties")) {
			this.pathWithoutExtension = path.replace(".properties", "");
		} else if (path.endsWith(".item")) {
 			this.pathWithoutExtension = path.replace(".item", "");
		} else if (path.endsWith(".screenshot")) {
 			this.pathWithoutExtension = path.replace(".screenshot", "");
		} else {
			this.pathWithoutExtension = path;
		}
	}

	@Override
	public int hashCode() {
		return (jobName + ":" + version).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof Job) {
			return jobName.equals(((Job) o).jobName) && version.equals(((Job) o).version);
		}
		return false;
	}

	@Override
	public int compareTo(Job job) {
		int pos = job.getVersion().indexOf('.');
		if (pos == -1) {
			throw new IllegalArgumentException("Talendjob: " + job.jobName + " has an invalid version: " + job.version);
		} else {
			int major = Integer.parseInt(job.getVersion().substring(0, pos));
			int minor = Integer.parseInt(job.getVersion().substring(pos + 1));
			if (jobName.equals(job.jobName)) {
				if (majorVersion > major) {
					return -1;
				} else if (majorVersion < major) {
					return 1;
				} else {
					if (minorVersion > minor) {
						return -1;
					} else if (minorVersion < minor) {
						return 1;
					} else {
						return 0;
					}
				}
			} else {
				String ownPath = jobFolder + jobName + version;
				String otherPath = job.jobFolder + job.jobName + job.version;
				return ownPath.compareTo(otherPath);
			}
		}
	}
	
	@Override
	public String toString() {
		return jobFolder + "/" + jobName + ":" + version;
	}
	
	public Document getItemDoc() {
		return itemDoc;
	}
	
	public void setItemDoc(Document itemDoc) {
		this.itemDoc = itemDoc;
	}
	
	public String getJobFolder() {
		return jobFolder;
	}
	
	public void setJobFolder(String jobFolder) {
		this.jobFolder = jobFolder;
	}
	
	public void retrieveContext() throws Exception {
		if (itemDoc == null) {
			throw new IllegalStateException("Item document not set!");
		}
		context = new ArrayList<>();
		Element root = itemDoc.getRootElement();
		List<Node> contextNodes = root.selectNodes("context/contextParameter");
		for (Node cn : contextNodes) {
			String id = ((Element) cn).attributeValue("internalId");
			if (id == null) {
				id = ((Element) cn).attributeValue("repositoryContextId");
			}
			String name = ((Element) cn).attributeValue("name");
			String value = ((Element) cn).attributeValue("value");
			String type = ((Element) cn).attributeValue("type");
			String comment = ((Element) cn).attributeValue("comment");
			ContextParameter p = new ContextParameter(id, name);
			p.setValue(value);
			p.setTalendType(type);
			p.setComment(comment);
			context.add(p);
		}
	}
	
	public void retrieveTRunJobs() throws Exception {
		if (itemDoc == null) {
			throw new IllegalStateException("Item document not set!");
		}
		embeddedJobs.clear();
		Element root = itemDoc.getRootElement();
		List<Node> tRunJobNodes = root.selectNodes("node[@componentName='tRunJob']");
		for (Node cn : tRunJobNodes) {
			TRunJob tRunJob = new TRunJob(cn, model);
			embeddedJobs.add(tRunJob);
		}
	}
	
	public List<TRunJob> getEmbeddedJobs() {
		return embeddedJobs;
	}
	
	public List<ContextParameter> getContext() throws Exception {
		if (context == null) {
			retrieveContext();
		}
		return context;
	}
	
	public static List<ComponentAttribute> getComponentAttributes(Element component) {
		List<Element> params = component.elements();
		List<ComponentAttribute> attributes = new ArrayList<>();
		for (Element param : params) {
			ComponentAttribute a = new ComponentAttribute();
			a.setName(param.attributeValue("name"));
			a.setField(param.attributeValue("field"));
			a.setValue(param.attributeValue("value"));
			attributes.add(a);
		}
		return attributes;
	}
	
	public static ComponentAttribute getComponentAttributeByName(Element component, String nameToSearchFor) {
		List<Element> params = component.elements();
		for (Element param : params) {
			String name = param.attributeValue("name");
			if (name.equalsIgnoreCase(nameToSearchFor)) {
				ComponentAttribute a = new ComponentAttribute();
				a.setName(param.attributeValue("name"));
				a.setField(param.attributeValue("field"));
				a.setValue(param.attributeValue("value"));
				return a;
			}
		}
		return null;
	}

}
