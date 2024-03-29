package de.jlo.analyse.talend;

import org.dom4j.Element;

public class TRunJob extends Component {
	
	private Job referencedJob = null;
	private String referencedJobName = null;
	
	public TRunJob(Job job, Element element) throws Exception {
		super(job, element);
		retrieveReferencedJob();
	}
	
	public void retrieveReferencedJob() throws Exception {
		String referencedJobId = getComponentValueByName("PROCESS:PROCESS_TYPE_PROCESS");
		referencedJobName = getComponentValueByName("PROCESS");
		if (referencedJobId == null) {
			String version = getComponentValueByName("PROCESS:PROCESS_TYPE_VERSION");
			referencedJob = getJob().getProject().getJobByVersion(referencedJobName, version);
		} else {
			int pos = referencedJobId.indexOf(':');
			if (pos > 0) {
				referencedJobId = referencedJobId.substring(pos + 1);
			}
			referencedJob = getJob().getProject().getJobById(referencedJobId);
			if (isTransmitContext()) {
				try {
					referencedJob.addReplaceContext(getJob().getContext());
				} catch (Exception e) {
					// ignore
				}
			}
		}
	}

	public Job getReferencedTalendjob() {
		return referencedJob;
	}

	public String getReferencedJobName() {
		return referencedJobName;
	}

	public boolean isTransmitContext() throws Exception {
		return "true".equals(getComponentValueByName("TRANSMIT_WHOLE_CONTEXT"));
	}

}
