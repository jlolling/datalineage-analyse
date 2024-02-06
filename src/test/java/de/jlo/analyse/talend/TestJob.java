package de.jlo.analyse.talend;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.jlo.analyse.TestUtil;

public class TestJob {
	
	@Test
	public void testVersionCompare1() {
		Project model = new Project();
		Job job1 = new Job(model);
		job1.setId("12345");
		job1.setJobName("testjob");
		job1.setVersion("1.5");
		Job job2 = new Job(model);
		job2.setId("123456");
		job2.setJobName("testjob");
		job2.setVersion("1.3");
		System.out.println("compare=" + job1.compareTo(job2));
		assertTrue("compare failed", job1.compareTo(job2) < 0);
	}

	@Test
	public void testVersionCompare2() {
		Project model = new Project();
		Job job1 = new Job(model);
		job1.setId("12345");
		job1.setJobName("testjob");
		job1.setVersion("1.3");
		Job job2 = new Job(model);
		job2.setId("123456");
		job2.setJobName("testjob");
		job2.setVersion("1.3");
		System.out.println("compare=" + job1.compareTo(job2));
		assertTrue("compare failed", job1.compareTo(job2) == 0);
	}

	@Test
	public void testVersionCompare3() {
		Project model = new Project();
		Job job1 = new Job(model);
		job1.setId("12345");
		job1.setJobName("testjob");
		job1.setVersion("2.0");
		Job job2 = new Job(model);
		job2.setId("123456");
		job2.setJobName("testjob");
		job2.setVersion("3.1");
		System.out.println("compare=" + job1.compareTo(job2));
		assertTrue("compare failed", job1.compareTo(job2) > 0);
	}

	private Project project = null;

	@Before
	public void readProject() throws Exception {
		String projectRoot = TestUtil.getWorkDir() + "src/test/resources/TEST";
		project = new Project(projectRoot);
	}
	
	@Test
	public void testReadJobContext() throws Exception {
		String jobName = "test_job_chain";
		Job testJob = project.getLatestJob(jobName);
		assertEquals("wrong job version", "0.1", testJob.getVersion());
		List<ContextParameter> context = testJob.getContext();
		for (ContextParameter p : context) {
			System.out.println(p.getName() + "=" + p.getValue());
		}
		assertEquals("wrong number context variables", 2, context.size());
	}
	
	@Test
	public void testReadAllEmbeddedJobs() throws Exception {
		String jobName = "test_job_chain";
		Job testJob = project.getLatestJob(jobName);
		List<Job> list = testJob.getAllEmbeddedJobs();
		for (Job job : list) {
			System.out.println(job);
		}
		assertEquals("wong number child jobs", 3, list.size());
	}

	@Test
	public void testChildJobs() throws Exception {
		String jobName = "test_job_chain";
		Job testJob = project.getLatestJob(jobName);
		List<TRunJob> listChildJobs = testJob.getEmbeddedJobs();
		assertEquals("wrong number ob child jobs", 2, listChildJobs.size());
		for (int i = 0; i < listChildJobs.size(); i++) {
			TRunJob job = listChildJobs.get(i);
			if (i == 0) {
				assertEquals("wrong name of child job " + i, "test_child_job1", job.getReferencedTalendjob().getJobName());
			} else if (i == 1) {
				assertEquals("wrong name of child job " + i, "test_child_job2", job.getReferencedTalendjob().getJobName());
			}
		}
	}

	@Test
	public void testComponents() throws Exception {
		String jobName = "test_job_chain";
		Job testJob = project.getLatestJob(jobName);
		List<Component> listComponents = testJob.getComponents();
		for (Component c : listComponents) {
			System.out.println(c);
		}
		assertEquals("wrong number ob child jobs", 4, listComponents.size());
	}

	@Test
	public void testGetComponentByCoalesceNames() throws Exception {
		String jobName = "test_sap_rfc_ms";
		Job testJob = project.getLatestJob(jobName);
		Component c = testJob.getComponent("tSAPRFCConnection_1");
		String actual = c.getComponentValueByName("dummy,SYSTEMID");
		String expected = "\"KE4\"";
		assertEquals("Wrong value", expected, actual);
	}

}
