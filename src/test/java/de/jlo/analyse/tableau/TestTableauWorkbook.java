package de.jlo.analyse.tableau;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import de.jlo.analyse.DatabaseTable;
import de.jlo.analyse.TestUtil;

public class TestTableauWorkbook {
	
	@Test
	public void testRetrieveTableFromQueries() throws Exception {
		String resource = "/Merchant_Report.twb";
		String path = "/temp" + resource;
		TestUtil.saveResourceAsFile(resource, path);
		TableauWorkbook tb = new TableauWorkbook(path);
		tb.parseWorkbook();
		List<DatabaseTable> tables = tb.getTableNames();
		for (DatabaseTable t : tables) {
			System.out.println(t);
		}
		assertTrue("No tables", tables.size() > 0);
	}

	@Test
	public void testRetrieveTableDirect() throws Exception {
		String resource = "/KCD-MPETest.twb";
		String path = "/temp" + resource;
		TestUtil.saveResourceAsFile(resource, path);
		TableauWorkbook tb = new TableauWorkbook(path);
		tb.parseWorkbook();
		List<DatabaseTable> tables = tb.getTableNames();
		for (DatabaseTable t : tables) {
			System.out.println(t);
		}
		assertTrue("No tables", tables.size() > 0);
	}

	@Test
	public void testRetrieveTableDirect2() throws Exception {
		String resource = "/Report_with_table.twb";
		String path = "/temp" + resource;
		TestUtil.saveResourceAsFile(resource, path);
		TableauWorkbook tb = new TableauWorkbook(path);
		tb.parseWorkbook();
		List<DatabaseTable> tables = tb.getTableNames();
		for (DatabaseTable t : tables) {
			System.out.println(t);
		}
		assertTrue("No tables", tables.size() > 0);
	}
	
	@Test
	public void testRetrieveDatasources() throws Exception {
		String resource = "/test_workbook_with_datasource.twb";
		String path = "/temp" + resource;
		TestUtil.saveResourceAsFile(resource, path);
		TableauWorkbook tb = new TableauWorkbook(path);
		tb.parseWorkbook();
		List<String> tables = tb.getDatasourceNames();
		for (String t : tables) {
			System.out.println(t);
		}
		assertTrue("No datasources", tables.size() > 0);
	}


}
