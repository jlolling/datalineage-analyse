package de.jlo.talend.model;

import org.dom4j.Element;
import org.dom4j.Node;

public class TRunJob {
	
	private Job referencedJob = null;
	private String uniqueName = null;
	private Node node = null;
	private Model model = null;
	
	public TRunJob(Node node, Model model) {
		if (model == null) {
			throw new IllegalArgumentException("model cannot be null");
		}
		if (node == null) {
			throw new IllegalArgumentException("node cannot be null");
		}
		this.node = node;
		uniqueName = Model.getComponentId((Element) node);
		String referencedJobId = Model.getComponentAttribute((Element) node, "PROCESS:PROCESS_TYPE_PROCESS");
		if (referencedJobId == null) {
			// potential tRunJob bug found
			// try to get the job by name and version
			String jobName = Model.getComponentAttribute((Element) node, "PROCESS");
			String version = Model.getComponentAttribute((Element) node, "PROCESS:PROCESS_TYPE_VERSION");
			referencedJob = model.getJobByVersion(jobName, version);
		} else {
			referencedJobId = referencedJobId.replace("TALEND:", "");
			referencedJob = model.getJobById(referencedJobId);
		}
	}
	
	public String getUniqueId() {
		return uniqueName;
	}
	
	public Job getReferencedTalendjob() {
		return referencedJob;
	}

	public Model getModel() {
		return model;
	}

	public Node getNode() {
		return node;
	}
	
}
