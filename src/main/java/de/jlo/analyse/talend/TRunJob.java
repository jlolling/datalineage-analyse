package de.jlo.analyse.talend;

import org.dom4j.Element;

public class TRunJob extends Component {
	
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
			referencedJob = getJob().getProject().getJobByVersion(jobName, version);
		} else {
			referencedJobId = referencedJobId.replace("TALEND:", "");
			referencedJob = getJob().getProject().getJobById(referencedJobId);
		}
	}
		
	public Job getReferencedTalendjob() {
		return referencedJob;
	}
	
}
