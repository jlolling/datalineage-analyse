package de.jlo.analyse.talend;

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
	private List<TRunJob> embeddedJobs = null;
	private List<Component> listComponents = null;
	private Project project = null;
	
	public Job(Project project) {
		if (project == null) {
			throw new IllegalArgumentException("model cannot be null");
		}
		this.project = project;
	}
	
	public Project getProject() {
		return project;
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
	
	public Document getItemDoc() throws Exception {
		if (itemDoc == null) {
			itemDoc = project.readItem(this);
		}
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
	
	private void retrieveContext() throws Exception {
		context = new ArrayList<>();
		Element root = getItemDoc().getRootElement();
		List<Node> contextNodes = new ArrayList<Node>();
		// search context with filter
		if (project.getDefaultContext() != null) {
			String[] defaultContextValues = project.getDefaultContext().split(",");
			for (String contextName : defaultContextValues) {
				if (contextName != null && contextName.trim().isEmpty() == false) {
					String contextXPath = "context[@name='" + contextName + "']/contextParameter";
					contextNodes = root.selectNodes(contextXPath);
					if (contextNodes.size() > 0) {
						// we found a context with expected name
						break; // do not search for more
					}
				}
			}
		}
		if (contextNodes.size() == 0) {
			contextNodes = root.selectNodes("context/contextParameter"); // search context without filter
		}
		for (Node cn : contextNodes) {
			String value = ((Element) cn).attributeValue("value");
			if (value != null && value.replace("\"", "").trim().isEmpty() == false) {
				// we should ignore empty context variables
				String name = ((Element) cn).attributeValue("name");
				String type = ((Element) cn).attributeValue("type");
				String comment = ((Element) cn).attributeValue("comment");
				String contextId = ((Element) cn).attributeValue("internalId");
				if (contextId == null) {
					contextId = ((Element) cn).attributeValue("repositoryContextId");
				}
				ContextParameter p = new ContextParameter(contextId, name);
				p.setValue(value);
				p.setTalendType(type);
				p.setComment(comment);
				if (context.contains(p) == false) {
					context.add(p);
				}
			}
		}
	}
	
	public void addReplaceContext(List<ContextParameter> otherContext) {
		for (ContextParameter p : otherContext) {
			if (p.getValue() != null) {
				if (context.contains(p)) {
					context.remove(p);
				}
				context.add(p);
			}
		}
	}
	
	public void addReplaceContextVariable(String key, String value) {
		ContextParameter p = new ContextParameter(key);
		p.setValue(value);
		if (p.getValue() != null) {
			if (context.contains(p)) {
				context.remove(p);
			}
			context.add(p);
		}
	}
	
	private void retrieveTRunJobs() throws Exception {
		embeddedJobs = new ArrayList<>();
		Element root = getItemDoc().getRootElement();
		List<Node> tRunJobNodes = root.selectNodes("node[@componentName='tRunJob']");
		for (Node cn : tRunJobNodes) {
			TRunJob tRunJob = new TRunJob(this, (Element) cn);
			embeddedJobs.add(tRunJob);
		}
	}
	
	private void retrieveComponents() throws Exception {
		listComponents = new ArrayList<>();
		Element root = getItemDoc().getRootElement();
		List<Node> components = root.selectNodes("node[not(@componentName='tRunJob')]");
		for (Node cn : components) {
			Component c = new Component(this, (Element) cn);
			listComponents.add(c);
		}
	}

	public List<TRunJob> getEmbeddedJobs() throws Exception {
		if (embeddedJobs == null) {
			retrieveTRunJobs();
		}
		return embeddedJobs;
	}
	
	public List<Job> getAllEmbeddedJobs() throws Exception {
		if (embeddedJobs == null) {
			retrieveTRunJobs();
		}
		List<Job> list = new ArrayList<>();
		collectEmbeddedJobs(list, this);
		return list;
	}
	
	private void collectEmbeddedJobs(List<Job> list, Job parentJob) throws Exception {
		List<TRunJob> listTrun = parentJob.getEmbeddedJobs();
		for (TRunJob tr : listTrun) {
			Job child = tr.getReferencedTalendjob();
			if (list.contains(child) == false) {
				list.add(child);
			}
			collectEmbeddedJobs(list, child);
		}
	}
	
	public List<ContextParameter> getContext() throws Exception {
		if (context == null) {
			retrieveContext();
		}
		return context;
	}

	public List<Component> getComponents() throws Exception {
		if (listComponents == null) {
			retrieveComponents();
		}
		return listComponents;
	}
	
	public List<Component> getComponents(String componentName) throws Exception {
		if (listComponents == null) {
			retrieveComponents();
		}
		List<Component> list = new ArrayList<>();
		for (Component c : listComponents) {
			if (c.getComponentName().equalsIgnoreCase(componentName)) {
				list.add(c);
			}
		}
		return list;
	}
	
	public Component getComponent(String uniqueId) throws Exception {
		if (listComponents == null) {
			retrieveComponents();
		}
		for (Component c : listComponents) {
			if (c.getUniqueId().equals(uniqueId)) {
				return c;
			}
		}
		return null;
	}
	
}
