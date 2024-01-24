package de.jlo.analyse.talend;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.jlo.analyse.DatabaseTable;
import de.jlo.analyse.TestUtil;

public class TestAnalyseTables {
	
	private String projectRoot = TestUtil.getWorkDir() + "src/test/resources/TEST"; // set this to your Talend project root dir 

	@Test
	public void testAnalyseTables() throws Exception {
		Project project = new Project();
		project.readProject(projectRoot);
		String testJobName = "test_child_job1";
		Job testJob = project.getLatestJob(testJobName);
		AnalyseTables a = new AnalyseTables(testJob);
		a.analyseTables();
		List<DatabaseTable> listInputTables = a.getListInputTables();
		for (DatabaseTable t : listInputTables) {
			System.out.println(t);
		}
		int expected = 1;
		int actual = listInputTables.size();
		assertEquals("Number read tables wrong", expected, actual);
		System.out.println("--------------------------");
		List<DatabaseTable> listOutputTables = a.getListOutputTables();
		for (DatabaseTable t : listOutputTables) {
			System.out.println(t);
		}
		expected = 1;
		actual = listOutputTables.size();
		assertEquals("Number written tables wrong", expected, actual);
	}

	@Test
	public void testAnalyseTableTransfer() throws Exception {
		Project project = new Project();
		project.readProject(projectRoot);
		String testJobName = "test_table_transfer";
		Job testJob = project.getLatestJob(testJobName);
		AnalyseTables a = new AnalyseTables(testJob);
		a.analyseTables();
		List<DatabaseTable> listInputTables = a.getListInputTables();
		for (DatabaseTable t : listInputTables) {
			System.out.println(t);
		}
		int expected = 1;
		int actual = listInputTables.size();
		assertEquals("Number read tables wrong", expected, actual);
		System.out.println("--------------------------");
		List<DatabaseTable> listOutputTables = a.getListOutputTables();
		for (DatabaseTable t : listOutputTables) {
			System.out.println(t);
		}
		expected = 1;
		actual = listOutputTables.size();
		assertEquals("Number written tables wrong", expected, actual);
	}
	
}
