package de.jlo.analyse.talend;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.jlo.analyse.sql.SQLCodeUtil;

public class TestContextVarResolver {
	
	@Test
	public void testReplaceContextInOutput1() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setContextParameter("TABLE", "mytable", null);
		String testSQL = "context.TABLE + \"_temp\"";
		String expected = "mytable_temp\"";
		String actual = r.replace(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceJobName() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setJobName("test_job");
		r.setContextParameter("TABLE", "mytable", null);
		String testSQL = "context.TABLE + \"_temp_\" + jobName";
		String expected = "mytable_temp_test_job";
		String actual = r.replace(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceJobNameSingle0() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setJobName("test_job");
		String testSQL = "jobName";
		String expected = "test_job";
		String actual = r.replaceJobName(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceJobNameSingle() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setJobName("test_job");
		String testSQL = "jobName";
		String expected = "test_job";
		String actual = r.replace(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testKeepUnchangedIfNoContext() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setJobName("test_job");
		r.setContextParameter("TABLE", "mytable", null);
		String testSQL = "xyz";
		String expected = "xyz";
		String actual = r.replace(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextInOutput1ToJava() throws Exception {
		String test = "\"mytable_temp\"\"";
		String expected = "mytable_temp";
		String actual = SQLCodeUtil.convertJavaToSqlCode(test);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextInOutput2() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setContextParameter("TABLE", "mytable", null);
		String testSQL = "\"temp_\" + context.TABLE";
		String expected = "\"temp_mytable";
		String actual = r.replace(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextInOutput3() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setContextParameter("TABLE", "mytable", null);
		String testSQL = "\"temp_\" + context.TABLE + \"_xxx\"";
		String expected = "\"temp_mytable_xxx\"";
		String actual = r.replace(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextInOutput4() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setContextParameter("TABLE", "mytable", null);
		String testSQL = "context.TABLE + \"_temp\"";
		String expected = "mytable_temp\"";
		String actual = r.replace(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextInOutput5() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setContextParameter("TABLE", "mytable", null);
		String testSQL = "context.TABLE";
		String expected = "mytable";
		String actual = r.replace(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextEncapulated() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setContextParameter("DB1_Schema", "schema_1", null);
		r.setContextParameter("DB2_Schema", "schema_2", null);
		String testSQL = "select * from \" + context.DB1_Schema + \".table1,\n\" + context.DB2_Schema + \".table2";
		String expected = "select * from schema_1.table1,\nschema_2.table2";
		String actual = r.replace(testSQL);
		assertEquals("Fail", expected, actual);
	}
	
	@Test
	public void testReplaceContextEncapulated2() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setContextParameter("DB1_Schema", "schema_1", null);
		r.setContextParameter("DB2_Schema", "schema_2", null);
		String testSQL = "\"select * from \" + context.DB1_Schema + \".table1,\n\" + context.DB2_Schema + \".table2\"";
		String expected = "select * from schema_1.table1,\nschema_2.table2";
		String actual = SQLCodeUtil.convertJavaToSqlCode(r.replace(testSQL));
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextSimple() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setContextParameter("DB1_Schema", "schema_1", null);
		r.setContextParameter("DB2_Schema", "schema_2", null);
		String testSQL = "select * from \" + context.DB1_Schema + \".table1,\n\" + context.DB2_Schema + \".table2";
		String expected = "select * from schema_1.table1,\nschema_2.table2";
		String actual = r.replace(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextMissing() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		String testSQL = "select * from \" + context.DB1_Schema + \".table1";
		String expected = "select * from PLACEHOLDER.table1";
		String actual = r.replace(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextInTableName() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setContextParameter("target_table", "conditions", null);
		r.setContextParameter("KSCHL", "YC08", null);
		String testSQL = "context.target_table + \"_\" + context.KSCHL";
		String expected = "conditions_YC08";
		String actual = r.replace(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testRetrievePureSQL() throws Exception {
		String tq = "\"SELECT\n\\\"\"+context.B17_MANAGEMENT_DB_Database+\"\\\".\\\"\" + context.B17_MANAGEMENT_DB_Schema +  \"\\\".\\\"measureconfig\\\".\\\"job_instance_id\\\"\nFROM \\\"\"+context.B17_MANAGEMENT_DB_Database+\"\\\".\\\"\"+context.B17_MANAGEMENT_DB_Schema+\"\\\".\\\"measureconfig\\\"\"";
		System.out.println(tq);
		// first replace the context vars
		ContextVarResolver r = new ContextVarResolver();
		r.setContextParameter("B17_MANAGEMENT_DB_Database", "nucleus", null);
		r.setContextParameter("B17_MANAGEMENT_DB_Schema", "b17_management", null);
		String withoutContextActual = r.replace(tq);
		String withoutContextExcepted = "\"SELECT\n\\\"nucleus\\\".\\\"b17_management\\\".\\\"measureconfig\\\".\\\"job_instance_id\\\"\nFROM \\\"nucleus\\\".\\\"b17_management\\\".\\\"measureconfig\\\"\"";
		assertEquals("Context replacement failed", withoutContextExcepted, withoutContextActual);
		// convert String to SQL
		String actual = SQLCodeUtil.convertJavaToSqlCode(withoutContextActual).trim();
		String expected = "SELECT\n\"nucleus\".\"b17_management\".\"measureconfig\".\"job_instance_id\"\nFROM \"nucleus\".\"b17_management\".\"measureconfig\"";
		assertEquals("Convert Java to SQL failed", expected, actual);
	}
	
	@Test
	public void testReplaceContextVarsInSelect() throws Exception {
		String test = "\"SELECT \n"
				+ "contract_ref, \n"
				+ "product_no, \n"
				+ "product_group_ref, \n"
				+ "unit_factor, \n"
				+ "unit, \n"
				+ "currency, \n"
				+ "price, \n"
				+ "customer_nr, \n"
				+ "meta_index_within_file\n"
				+ "FROM \" + context.di_staging_otk_Database +  \".\" + context.table + \"_pricelist\n"
				+ "WHERE \n"
				+ "meta_excluded = false\n"
				+ "AND meta_file_id = \" + context.current_file_id";
		String expected = "\"SELECT \n"
				+ "contract_ref, \n"
				+ "product_no, \n"
				+ "product_group_ref, \n"
				+ "unit_factor, \n"
				+ "unit, \n"
				+ "currency, \n"
				+ "price, \n"
				+ "customer_nr, \n"
				+ "meta_index_within_file\n"
				+ "FROM staging_otk.otk_contract_pricelist\n"
				+ "WHERE \n"
				+ "meta_excluded = false\n"
				+ "AND meta_file_id = 99";
		ContextVarResolver r = new ContextVarResolver();
		r.setContextParameter("di_staging_otk_Database", "staging_otk", null);
		r.setContextParameter("current_file_id", "99", null);
		r.setContextParameter("table", "otk_contract", null);
		String actual = r.replace(test);
		System.out.println(actual);
		assertEquals("Convert context vars failed", expected, actual);
	}

	@Test
	public void testCollectTextPositionToReplace1() throws Exception {
		String test = "01234\" + context.v1 + \"5";
		ContextVarResolver r = new ContextVarResolver();
		r.setContextParameter("v1", "A", null);
		int expected = 1;
		List<ContextVarResolver.TextPositionToReplace> list = r.collectContextTextLocationToReplace(test);
		for (ContextVarResolver.TextPositionToReplace tp : list) {
			System.out.println(tp);
		}
		int actual = list.size();
		assertEquals("Number positions wrong", expected, actual);
		String expectedStr = "01234A5";
		String actualStr = r.replace(test);
		System.out.println(actualStr);
		assertEquals("Replace result wrong", expectedStr, actualStr);
	}

	@Test
	public void testCollectTextPositionToReplace2() throws Exception {
		String test = "01234\" + context.v1 + \"5\" + context.v2 + \"678";
		ContextVarResolver r = new ContextVarResolver();
		r.setContextParameter("v1", "A", null);
		r.setContextParameter("v2", "B", null);
		int expected = 2;
		List<ContextVarResolver.TextPositionToReplace> list = r.collectContextTextLocationToReplace(test);
		for (ContextVarResolver.TextPositionToReplace tp : list) {
			System.out.println(tp);
		}
		int actual = list.size();
		assertEquals("Number positions wrong", expected, actual);
		String expectedStr = "01234A5B678";
		String actualStr = r.replace(test);
		System.out.println(actualStr);
		assertEquals("Replace result wrong", expectedStr, actualStr);
	}

	@Test
	public void testReplaceContextMultiple() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.setJobName("testjob");
		r.setContextParameter("Target_Table_ZAVE009", "TABLE_ZAVE009", null);
		r.setContextParameter("Price_Condition", "YDXX", null);
		String testSQL = "CREATE TABLE IF NOT EXISTS `\"+jobName+\"_\"+context.Target_Table_ZAVE009+\"_\"+context.Price_Condition+\"` (";
		String expected = "CREATE TABLE IF NOT EXISTS `testjob_TABLE_ZAVE009_YDXX` (";
		String actual = r.replace(testSQL);
		assertEquals("Fail", expected, actual);
	}

}
