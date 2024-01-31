package de.jlo.analyse.talend;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.jlo.analyse.TestUtil;

public class TestAnalyseEmbedJobs {
	
	private String projectRoot = TestUtil.getWorkDir() + "src/test/resources/TEST"; // set this to your Talend project root dir 

	@Test
	public void testAnalyseAllJobs() throws Exception {
		Project project = new Project();
		project.readProject(projectRoot);
		List<Job> allJobs = project.getAllLatestJobs();
		int count = 0;
		for (Job job : allJobs) {
			AnalyseEmbeddedJobs a = new AnalyseEmbeddedJobs(job);
			a.analyse();
			List<String> listEmbeddedJobs = a.getEmbeddedJobNames();
			System.out.println("##### Embedded Jobs for: " + job.getJobName());
			for (String name : listEmbeddedJobs) {
				System.out.println(name);
				count++;
			}
		}
		System.out.println("Count: " + count);
		assertEquals("wrong number child jobs", 3, count);
	}

}
