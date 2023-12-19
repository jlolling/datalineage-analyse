package de.jlo.talend.model;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class TestTalendJob {
	
	@Test
	public void testCompare1() {
		Model model = new Model();
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
	public void testCompare2() {
		Model model = new Model();
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
	public void testCompare3() {
		Model model = new Model();
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

}
