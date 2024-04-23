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
		project.setDefaultContext("production,Default");
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
	public void testAnalyseTables2() throws Exception {
		Project project = new Project();
		project.readProject(projectRoot);
		project.setDefaultContext("production,Default");
		String testJobName = "L_S_206_DKH_PRC_Nettopreise";
		Job testJob = project.getLatestJob(testJobName);
		AnalyseTables a = new AnalyseTables(testJob);
		a.analyseTables();
		List<DatabaseTable> listInputTables = a.getListInputTables();
		for (DatabaseTable t : listInputTables) {
			System.out.println(t);
		}
		int expected = 10;
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
		System.out.println("--------------------------");
		List<DatabaseTable> listCreateTables = a.getListCreateTables();
		for (DatabaseTable t : listCreateTables) {
			System.out.println(t);
		}
		expected = 1;
		actual = listCreateTables.size();
		assertEquals("Number create tables wrong", expected, actual);
	}

	@Test
	public void testAnalyseTables3() throws Exception {
		Project project = new Project();
		project.readProject(projectRoot);
		project.setDefaultContext("production,Default");
		String testJobName = "L_S_218_DKG_SAP_KLOE_AVEKONV2_one_KSCHL";
		Job testJob = project.getLatestJob(testJobName);
		testJob.addReplaceContextVariable("kschl", "Z000");
		AnalyseTables a = new AnalyseTables(testJob);
		a.analyseTables();
		List<DatabaseTable> listInputTables = a.getListInputTables();
		for (DatabaseTable t : listInputTables) {
			System.out.println(t);
		}
		int expected = 3;
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
		System.out.println("--------------------------");
		List<DatabaseTable> listCreateTables = a.getListCreateTables();
		for (DatabaseTable t : listCreateTables) {
			System.out.println(t);
		}
		expected = 1;
		actual = listCreateTables.size();
		assertEquals("Number create tables wrong", expected, actual);
	}

	@Test
	public void testAnalyseTablesWithJDBC() throws Exception {
		Project project = new Project();
		project.readProject(projectRoot);
		project.setDefaultContext("production,Default");
		String testJobName = "L_D_020_LOAD_DIMENSION_MATERIAL";
		Job testJob = project.getLatestJob(testJobName);
		AnalyseTables a = new AnalyseTables(testJob);
		a.analyseTables();
		List<DatabaseTable> listInputTables = a.getListInputTables();
		for (DatabaseTable t : listInputTables) {
			System.out.println(t);
		}
		int expected = 12;
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
	
	@Test
	public void testAnalyseTableTransfer2() throws Exception {
		Project project = new Project();
		project.readProject(projectRoot);
		String testJobName = "L_L_013_VehiclePosition_PARCON_DTGPS_PS";
		Job testJob = project.getLatestJob(testJobName);
		AnalyseTables a = new AnalyseTables(testJob);
		a.analyseTables();
		List<DatabaseTable> listInputTables = a.getListInputTables();
		for (DatabaseTable t : listInputTables) {
			System.out.println(t);
		}
		int expected = 2;
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
	public void testAnalyseSAPRFCTableInputMessageServer() throws Exception {
		Project project = new Project();
		project.readProject(projectRoot);
		String testJobName = "test_sap_rfc_ms";
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
		String expectedName = "127.0.0.1:VBAK";
		String actualName = listInputTables.get(0).toString();
		assertEquals("Table name wrong", expectedName, actualName);
		System.out.println("--------------------------");
	}

	@Test
	public void testAnalyseSAPRFCTableInputAppServer() throws Exception {
		Project project = new Project();
		project.readProject(projectRoot);
		String testJobName = "test_sap_rfc_as";
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
		String expectedName = "127.0.0.1:VBAP";
		String actualName = listInputTables.get(0).toString();
		assertEquals("Table name wrong", expectedName, actualName);
		System.out.println("--------------------------");
	}

}
