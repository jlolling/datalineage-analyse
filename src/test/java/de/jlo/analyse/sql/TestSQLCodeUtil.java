package de.jlo.analyse.sql;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class TestSQLCodeUtil {
	
	@Test
	public void testRemoveEnclosure() {
		String test = "xyz `abc` [123] \"cdf\"";
		String expected = "xyz abc 123 cdf";
		String actual = SQLCodeUtil.cleanupEnclosures(test);
		assertEquals("does not match", expected, actual);
	}

	@Test
	public void testCleanupEmptyLines() throws IOException {
		String test = "xyz\n\n\n\n\nabc";
		String expected = "xyz\n\nabc";
		String actual = SQLCodeUtil.cleanupEmptyLines(test);
		System.out.println("##################");
		System.out.println(actual);
		System.out.println("##################");
		assertEquals("does not match", expected, actual);
	}

	@Test
	public void testReplaceHashComments() throws IOException {
		String test = "xyz\n # comment1 \n#comment2\nset var := 'x'";
		String expected = "xyz\n --  comment1 \n-- comment2\nset var = 'x'";
		String actual = SQLCodeUtil.replaceHashCommentsAndAssignments(test);
		System.out.println(actual);
		assertEquals("does not match", expected, actual);
	}
	
	@Test
	public void testRemoveIntoFromSelect() throws IOException {
		String test = "SELECT MAX(id) FROM Report.BENELUX_SEGMENT_KPI_CALC_VERSIONS WHERE kpi_name='STOCK_ORDERS' INTO  @ver_1;";
		String expected = "SELECT MAX(id) FROM Report.BENELUX_SEGMENT_KPI_CALC_VERSIONS WHERE kpi_name='STOCK_ORDERS' ;";
		String actual = SQLCodeUtil.removeIntoFromSelect(test);
		System.out.println(actual);
		assertEquals("does not match", expected, actual);
	}

	@Test
	public void testRemoveInto2FromSelect() throws IOException {
		String test = "SELECT MAX(id) FROM Report.BENELUX_SEGMENT_KPI_CALC_VERSIONS WHERE kpi_name='STOCK_ORDERS' INTO  @ver_1, @var_2, @var_3;";
		String expected = "SELECT MAX(id) FROM Report.BENELUX_SEGMENT_KPI_CALC_VERSIONS WHERE kpi_name='STOCK_ORDERS' ;";
		String actual = SQLCodeUtil.removeIntoFromSelect(test);
		System.out.println(actual);
		assertEquals("does not match", expected, actual);
	}

	@Test
	public void testConvertCodeToSQL() throws IOException {
		String test = "\"SELECT MAX(id) FROM Report.BENELUX_SEGMENT_KPI_CALC_VERSIONS WHERE kpi_name='STOCK_ORDERS'\"";
		String expected = "SELECT MAX(id) FROM Report.BENELUX_SEGMENT_KPI_CALC_VERSIONS WHERE kpi_name='STOCK_ORDERS'";
		String actual = SQLCodeUtil.convertJavaToSqlCode(test);
		System.out.println(actual);
		assertEquals("does not match", expected, actual);
	}

	@Test
	public void testConvertTableNameToSQL() throws IOException {
		String test = "\"TABLE_1\"";
		String expected = "TABLE_1";
		String actual = SQLCodeUtil.convertJavaToSqlCode(test);
		System.out.println(actual);
		assertEquals("does not match", expected, actual);
	}

	@Test
	public void testRemoveDisturbing() throws IOException {
		String test = "CREATE OR REPLACE\n"
				+ "ALGORITHM = UNDEFINED VIEW `Report`.`vLogisticVehiclePosition` AS\n"
				+ "select cast(NULL as char(30) charset utf8mb3) AS `CUSTOMER_NAME` from table";
		String expected = "CREATE OR REPLACE\n"
				+ " VIEW `Report`.`vLogisticVehiclePosition` AS\n"
				+ "select cast(NULL as char(30) ) AS `CUSTOMER_NAME` from table";
		String actual = SQLCodeUtil.removeDisturbingTerms(test);
		System.out.println(actual);
		assertEquals("does not match", expected, actual);
	}

	@Test
	public void testRemoveDisturbing2() throws IOException {
		String test = "CREATE\n"
				+ "ALGORITHM = UNDEFINED TABLE TEST AS\n"
				+ "field varchar(10) default charset utf8mb3;";
		String expected = "CREATE\n"
				+ " TABLE TEST AS\n"
				+ "field varchar(10) ;";
		String actual = SQLCodeUtil.removeDisturbingTerms(test);
		System.out.println(actual);
		assertEquals("does not match", expected, actual);
	}

}
