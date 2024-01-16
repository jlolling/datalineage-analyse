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
		System.out.println("referencedJobId: " + referencedJobId);
		if (referencedJobId == null) {
			String jobName = getComponentValueByName("PROCESS");
			String version = getComponentValueByName("PROCESS:PROCESS_TYPE_VERSION");
			referencedJob = getJob().getProject().getJobByVersion(jobName, version);
		} else {
			int pos = referencedJobId.indexOf(':');
			if (pos > 0) {
				referencedJobId = referencedJobId.substring(pos + 1);
			}
			referencedJob = getJob().getProject().getJobById(referencedJobId);
		}
	}
		
	public Job getReferencedTalendjob() {
		return referencedJob;
	}
	
}
