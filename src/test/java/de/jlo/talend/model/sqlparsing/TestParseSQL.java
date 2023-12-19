package de.jlo.talend.model.sqlparsing;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import de.jlo.talend.model.parser.sql.ContextVarResolver;
import de.jlo.talend.model.parser.sql.SQLParser;
import de.jlo.talend.model.parser.sql.SQLStatement;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TableAndProcedureNameFinder;

public class TestParseSQL {

	@Test
	public void testSelectsStrict() throws Exception {
		String sql1 = "with ws1 as (\n"
				    + "    select schema_c.function1(x) as alias_x from schema_c.table_c\n"
				    + "), \n"
				    + "ws2 as (\n"
				    + "    select y from schema_d.table_d\n"
				    + ") \n"
				    + "select \n"
				    + "    a as alias_a, \n"
				    + "    b as alias_b, \n"
				    + "    (select c from schema_e.table_e) as alias_c \n"
				    + "from schema_a.table_1 ta \n"
				    + "join schema_b.table_b tb using(c) \n"
				    + "join ws1 using(c)";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.retrieveTablesAndFunctionSignatures(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNames();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(5, tableList.size());
		List<String> functionNames = tablesNamesFinder.getListFunctionSignatures();
		for (String f : functionNames) {
			System.out.println(f);
		}
		assertEquals(1, functionNames.size());
	}

	@Test
	public void testSelectsStrictOracle() throws Exception {
		String sql1 = "with ws1 as (\n"
				    + "    select schema_c.function1(x) as alias_x from schema_c.table_c\n"
				    + "), \n"
				    + "ws2 as (\n"
				    + "    select y from schema_d.table_d\n"
				    + ") \n"
				    + "select \n"
				    + "    a as alias_a, \n"
				    + "    b as alias_b, \n"
				    + "    (select c from schema_e.table_e) as alias_c \n"
				    + "from schema_a.table_1 ta,table_b tb, table_d td \n"
				    + "where ta.x = tb.y and ta.b = :var1";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.retrieveTablesAndFunctionSignatures(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNames();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(6, tableList.size());
		List<String> functionNames = tablesNamesFinder.getListFunctionSignatures();
		for (String f : functionNames) {
			System.out.println(f);
		}
		assertEquals(1, functionNames.size());
	}

	@Test
	public void testSelectsFromMethod() throws Exception {
		String sql1 = "with ws1 as (\n"
				    + "    select schema_c.function1(x) as alias_x from schema_c.table_c\n"
				    + "), \n"
				    + "ws2 as (\n"
				    + "    select y from schema_d.table_d\n"
				    + ") \n"
				    + "select \n"
				    + "    a as alias_a, \n"
				    + "    b as alias_b, \n"
				    + "    (select c from schema_e.table_e) as alias_c \n"
				    + "from schema_a.table_1 ta \n"
				    + "join schema_b.table_b tb using(c) \n"
				    + "join ws1 using(c)";
		List<String> tableList = SQLParser.findFromTables(sql1);
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(5, tableList.size());
	}
	
	@Test
	public void testFindWithNames() throws Exception {
		String sql1 = "with ws1 as (\n"
				    + "    select schema_c.function1(x) as alias_x from schema_c.table_c\n"
				    + "), \n"
				    + "ws2 AS(\n"
				    + "    select y from schema_d.table_d\n"
				    + "),ws3 as (select y from schema_d.table_d)\n"
				    + "select \n"
				    + "    a as alias_a, \n"
				    + "    b as alias_b, \n"
				    + "    (select c from schema_e.table_e) as alias_c \n"
				    + "from schema_a.table_1 ta \n"
				    + "join schema_b.table_b tb using(c) \n"
				    + "join ws1 using(c)";
		List<String> tableList = SQLParser.findWithNames(sql1);
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(3, tableList.size());
	}

	@Test
	public void testFindTableNames2() throws Exception {
		String sql1 = "with \n"
			    + " mandate_verfied as"
			    + " (select b17_core.catalog_id_by_sysname('mandate_verified', 'process_status') as id)\n"
			    + "select \n"
			    + "   cnt.businessobject_id as contract_bo_id,\n"
			    + "   cnt.contracttype_id, \n"
			    + "   cnt.name as contract_name,\n"
			    + "   mnd.mandate_id,\n"
			    + "   mnd.contract_id as mandate_contract_id,\n"
			    + "   mnd.valid_from as mandate_valid_from,\n"
			    + "   mnd.valid_to as mandate_valid_to,\n"
			    + "   mnd.tu_id as mandate_tu_id,\n"
			    + "   mnd.status_id as mandate_status_id,\n"
			    + "   b17_core.catalog_sysname_by_id(mnd.status_id) as mandate_status_sysname,\n"
			    + "   b17_core.catalog_label_by_id(mnd.status_id) as mandate_status_label,\n"
			    + "   pn.businesspartner_id as business_partner_id,\n"
			    + "   cnt.consignatory_id as business_partner_id_contract,\n"
			    + "   pn.name as business_partner_name,\n"
			    + "   pn.firstname as business_partner_firstname,\n"
			    + "   cntpn.businesspartner_id as contract_partner_id,\n"
			    + "   cnt.affiliate_id as contract_partner_id_contract,\n"
			    + "   cntpn.name as contract_partner_name,\n"
			    + "   cntpn.firstname as contract_partner_firstname\n"
			    + "from \n"
			    + " b17_core.contract cnt\n"
			    + " inner join b17_core.mandate mnd\n"
			    + "  on \n"
			    + "   mnd.contract_id = cnt.businessobject_id\n"
			    + " inner join b17_core.partnername pn\n"
			    + "  on \n"
			    + "   pn.businesspartner_id = cnt.consignatory_id\n"
			    + " inner join b17_core.partnername cntpn\n"
			    + "  on \n"
			    + "   cntpn.businesspartner_id = cnt.affiliate_id\n"
			    + "where\n"
			    + " pn.default_selection = true\n"
			    + " and\n"
			    + " cnt.affiliate_id =";
		List<String> tableList = SQLParser.findFromTables(sql1);
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(3, tableList.size());
		assertEquals("table 1 incorrect", "b17_core.contract",  tableList.get(0));
		assertEquals("table 2 incorrect", "b17_core.mandate",  tableList.get(1));
		assertEquals("table 2 incorrect", "b17_core.partnername",  tableList.get(2));
	}

	@Test
	public void testSelectProc() throws Exception {
		String sql1 = "select a from procedureCall(1,2)";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.retrieveTablesAndFunctionSignatures(stmt);
		List<String> functionNames = tablesNamesFinder.getListFunctionSignatures();
		for (String f : functionNames) {
			System.out.println(f);
		}
		assertEquals(1, functionNames.size());
	}

	@Test
	public void testTruncateTable() throws Exception {
		String sql1 = "truncate table schema_a.table_a";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.retrieveTablesAndFunctionSignatures(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNames();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(1, tableList.size());
	}

	@Test
	public void testDeleteTable() throws Exception {
		String sql1 = "delete from schema_a.table_a a";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.retrieveTablesAndFunctionSignatures(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNames();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(1, tableList.size());
	}

	@Test
	public void testUpdateTable() throws Exception {
		String sql1 = "update schema_a.table_a a set x = b.v from schema_a.table_b b";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.retrieveTablesAndFunctionSignatures(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNames();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(2, tableList.size());
	}
	
	@Test
	public void testSQLParserWithIncludedJavaCode() throws Exception {
		System.out.println("########################### testSQLParserWithIncludedJavaCode ######################");
		String code = "\"select\n"
			    + "businessobject_id,\n"
			    + "field * 1000 as object_revision,\n"
			    + "\" + context.B17_CORE_DB_Schema + \".catalog_sysname_by_id(datatype_id) as datatype_sysname,\n"
			    + "creation_date,\n"
			    + "created_by,\n"
			    + "\" + context.B17_CORE_DB_Schema + \".catalog_sysname_by_id(data_status_id)  as data_status_sysname,\n"
			    + "modified_by,\n"
			    + "\" + context.B17_CORE_DB_Schema + \".catalog_sysname_by_id(process_status_id) as process_status_sysname,\n"
			    + "created_by_system,\n"
			    + "modified_by_system, \" + \n"
			    + "(context.showDisplayString?\" b17_core.get_display_string(businessobject_id)\":\"null \")\n"
			    + "+ \" as display_string\n"
			    + "from \" + context.B17_CORE_DB_Schema + \".\" + context.target_table + \" \" + (String)globalMap.get(\"whereClause\")";
		ContextVarResolver r = new ContextVarResolver();
		r.addContextVar("B17_CORE_DB_Schema", "b17_core");
		r.addContextVar("target_table", "businessobject");
		String sql = r.replaceContextVars(code);
		SQLParser parser = new SQLParser();
		parser.parseScript(sql);
		List<SQLStatement> stats = parser.getStatements();
		assertEquals("Count statements wrong", 1, stats.size());
		String oneSQL = stats.get(0).getSQL();
		System.out.println("Resolved statement: " + oneSQL);
		List<String> fromTableNames = SQLParser.findFromTables(oneSQL);
		System.out.println("from-Tables found:");
		for (String name : fromTableNames) {
			System.out.println(name);
		}
		assertEquals("Count tables wrong", 1, fromTableNames.size());
		String expTableName = "b17_core.businessobject";
		String actTableName = fromTableNames.get(0);
		assertEquals("Table name wrong", expTableName, actTableName);
	}

	@Test
	public void testLineCommentParserIssue() {
		String sql1 = "BEGIN\n"
			    + "select 1;\n"
			    + "NULL;\n"
			    + "END;";
		String sql2 = "BEGIN\n"
			    + "select 2;\n"
			    + "END;";
		String script = sql1
			    + "\n/\n"
			    + "-- my comment\n" // <-- this is the problem!
			    + sql2;
		SQLParser p = new SQLParser();
		p.setUseScriptDetecting(true);
		p.parseScript(script);
		List<SQLStatement> stats = p.getStatements();
		for (SQLStatement s : stats) {
			System.out.println("################");
			System.out.println(s.getSQL());
		}
		assertEquals("Number statements wrong", 2, stats.size());
	}

	@Test
	public void testInsert() {
		String expected = "db.mytable";
		String test = "insert into\n" + expected + "\n (field1,field2)";
		String actual = SQLParser.findInsertUpdateTables(test);
		assertEquals("Table not found", expected, actual);
	}

	@Test
	public void testInsertWithComements() {
		String expected = "db.mytable";
		String test = "insert /* comment */ into -- comment \n" + expected + "\n (field1,field2)";
		test = SQLParser.removeAllComments(test);
		String actual = SQLParser.findInsertUpdateTables(test);
		assertEquals("Table not found", expected, actual);
	}

	@Test
	public void testInsertIgnore() {
		String expected = "db.mytable";
		String test = "\ninsert \nignore into " + expected + "\n (field1,field2)";
		String actual = SQLParser.findInsertUpdateTables(test);
		assertEquals("Table not found", expected, actual);
	}

	@Test
	public void testUpdate() {
		String expected = "db.mytable";
		String test = "update " + expected;
		String actual = SQLParser.findInsertUpdateTables(test);
		assertEquals("Table not found", expected, actual);
	}

}
