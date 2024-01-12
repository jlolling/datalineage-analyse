package de.jlo.analyse.tableau;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class TestTableauWorkbook {
	
	@Test
	public void testRetrieveTables() throws Exception {
		String path = "/Users/jan/development/eclipse-workspace-talendcomp/talend-datalineage/src/test/resources/Merchant_Report.twb";
		TableauWorkbook tb = new TableauWorkbook(path);
		tb.parseWorkbook();
		List<DatabaseTable> tables = tb.getTableNames();
		for (DatabaseTable t : tables) {
			System.out.println(t);
		}
		assertTrue("No tables", tables.size() > 0);
	}

}
