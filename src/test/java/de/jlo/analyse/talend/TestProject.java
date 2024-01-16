package de.jlo.analyse.talend;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.jlo.analyse.TestUtil;

public class TestProject {
	
	private String projectRoot = TestUtil.getWorkDir() + "src/test/resources/TEST"; // set this to your Talend project root dir 
	
	@Test
	public void testReadProject() throws Exception {
		Project project = new Project();
		int countJobs = project.readProject(projectRoot);
		System.out.println("Count jobs: " + countJobs);
		int expected = 5;
		assertEquals("wrong number of jobs", expected, countJobs);
		int countConn = project.getDatabaseConnections().size();
		System.out.println("Count db connections: " + countConn);
		expected = 1;
		assertEquals("wrong number of meta database connections", expected, countConn);
	}
	
	@Test
	public void testReadJobContext() throws Exception {
		Project project = new Project(projectRoot);
		String jobName = "test_job_chain";
		Job testJob = project.getLatestJob(jobName);
		assertEquals("wrong job version", "0.1", testJob.getVersion());
		List<ContextParameter> context = testJob.getContext();
		for (ContextParameter p : context) {
			System.out.println(p.getName() + "=" + p.getValue());
		}
		assertEquals("wrong number context variables", 2, context.size());
	}

}
