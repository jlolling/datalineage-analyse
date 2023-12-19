package de.jlo.talend.model;

import static org.junit.Assert.assertEquals;

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
	
	public void testReadJobContext() throws Exception {
		Model model = new Model(projectRoot);
		String jobName = "";
		Job testJob = model.getLatestJob()
	}

}
