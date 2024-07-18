package de.jlo.analyse.tableau;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.jlo.analyse.DatabaseTable;
import de.jlo.analyse.TestUtil;

public class TestTableDatasource {

	@Test
	public void testCollectTable() throws Exception {
		String resource = "/orders_data.tds";
		String path = "/temp" + resource;
		TestUtil.saveResourceAsFile(resource, path);
		TableauDatasource ds = new TableauDatasource(path);
		ds.parseDatasource();
		String id = ds.getId();
		assertEquals("Id does not match", "orders_data", id);
		List<DatabaseTable> tables = ds.getTableNames();
		for (DatabaseTable t : tables) {
			System.out.println(t);
		}
		assertEquals("Number tables wrong", 1, tables.size());
	}
	
}
