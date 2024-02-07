package de.jlo.analyse.talend;

import java.util.ArrayList;
import java.util.List;

public class AnalyseEmbeddedJobs {
	
	private Job job = null;
	private List<String> embeddedJobs = new ArrayList<>();
	
	public AnalyseEmbeddedJobs(Job job) {
		if (job == null) {
			throw new IllegalArgumentException("job cannot be null");
		}
		this.job = job;
	}
	
	public void analyse() throws Exception {
		List<TRunJob> jobs = job.getTRunJobs();
		for (TRunJob tr : jobs) {
			if (embeddedJobs.contains(tr.getReferencedJobName()) == false) {
				embeddedJobs.add(tr.getReferencedJobName());
			}
		}
	}
	
	public List<String> getEmbeddedJobNames() {
		return embeddedJobs;
	}

	public Job getJob() {
		return job;
	}

}
