package de.jlo.analyse.talend;

import org.dom4j.Element;

public class TRunJob extends TalendComponent {
	
	private Job referencedJob = null;
	
	public TRunJob(Job job, Element element) {
		super(job, element);
		retrieveReferencedJob();
	}
	
	public void retrieveReferencedJob() {
		String referencedJobId = getComponentValueByName("PROCESS:PROCESS_TYPE_PROCESS");
		if (referencedJobId == null) {
			String jobName = getComponentAttributeByName("PROCESS").getValue();
			String version = getComponentAttributeByName("PROCESS:PROCESS_TYPE_VERSION").getValue();
			referencedJob = getJob().getModel().getJobByVersion(jobName, version);
		} else {
			referencedJobId = referencedJobId.replace("TALEND:", "");
			referencedJob = getJob().getModel().getJobById(referencedJobId);
		}
	}
		
	public Job getReferencedTalendjob() {
		return referencedJob;
	}
	
}
