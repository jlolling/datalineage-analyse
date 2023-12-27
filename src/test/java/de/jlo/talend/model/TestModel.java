package de.jlo.talend.model;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class TestModel {
	
	private String projectRoot = "/Users/jan/development/tos-workspace/XOM_COMMON"; // set this to your Talend project root dir 
	
	@Test
	public void testReadModel() throws Exception {
		Model model = new Model();
		int countJobs = model.readProject(projectRoot);
		System.out.println("Count jobs: " + countJobs);
		int expected = 42;
		assertEquals(expected, countJobs);
		int countConn = model.getDatabaseConnections().size();
		System.out.println("Count db connections: " + countConn);
		expected = 1;
		assertEquals(expected, countConn);
	}
	
	@Test
	public void testReadJobContext() throws Exception {
		Model model = new Model(projectRoot);
		String jobName = "manage_batchjobs_check_failed___one_task";
		Job testJob = model.getLatestJob(jobName);
		assertEquals("0.3", testJob.getVersion());
		List<ContextParameter> context = testJob.getContext();
		for (ContextParameter p : context) {
			System.out.println(p.getName() + "=" + p.getValue());
		}
		assertEquals(21, context.size());
	}

}
