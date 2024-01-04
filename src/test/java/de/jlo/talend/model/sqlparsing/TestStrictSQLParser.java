package de.jlo.talend.model.sqlparsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import de.jlo.talend.model.parser.sql.StrictSQLParser;
import de.jlo.talend.model.parser.sql.TableAndProcedureNameFinder;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;

public class TestStrictSQLParser {

	@Test
	public void testSelectsStrict() throws Exception {
		String sql1 = "with ws1 as (\n"
				    + "    select schema_c.function1(x) as alias_x from schema_c.table_c a\n"
				    + "), \n"
				    + "ws2 as (\n"
				    + "    select y from schema_d.table_d as b\n"
				    + ") \n"
				    + "select \n"
				    + "    a as alias_a, \n"
				    + "    b as alias_b, \n"
				    + "    (select c from schema_e.table_e) as alias_c \n"
				    + "from schema_a.table_1 ta \n"
				    + "join schema_b.table_b tb using(c)\n"
				    + "join ws1 using(c)"
				    + "where ta.field = @var1";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesInput();
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
	public void testCreateTableFromSelectStrict() throws Exception {
		String sql1 = "CREATE TABLE new_tbl as SELECT * FROM `orig_tbls`";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
		List<String> tableList2 = tablesNamesFinder.getListTableNamesCreate();
		for (String t : tableList2) {
			System.out.println(t);
		}
		assertEquals(1, tableList2.size());
		List<String> tableList = tablesNamesFinder.getListTableNamesInput();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(1, tableList.size());
	}
	
	@Test
	public void testRemoveEmptyLines() throws IOException {
		String test = "insert into schema_b.table_ins\n"
				+ "-- comment\n"
				+ "\n\n\n"
				+ "with ws1 as (\n";
		String expected =  "insert into schema_b.table_ins\n"
				+ "-- comment\n"
				+ "with ws1 as (\n";
		String actual = StrictSQLParser.cleanupEmptyLines(test);
		assertEquals("not match", expected, actual);
	}

	@Test
	public void testScriptStrict0() throws Exception {
		String sql1 = "set var1='1234';\n"
				+ ""
				+ "insert into schema_b.table_ins\n"
				+ "-- comment\n"
				+ "\n"
				+ "with ws1 as (\n"
			    + "    select schema_c.function1(x) as alias_x from schema_c.table_with1 a\n"
			    + "), \n"
			    + "ws2 as (\n"
			    + "    select y from schema_d.table_with2 b\n"
			    + ") \n"
			    + "select \n"
			    + "    a as alias_a, \n"
			    + "    b as alias_b, \n"
			    + "    (select c from schema_e.table_sel1) as alias_c \n"
			    + "from schema_a.table_sel2 ta \n"
			    + "join schema_b.table_sel3 tb using(c)\n"
			    + "join ws1 using(c)"
			    + "where ta.field = @var1;\n"
			    + ""
			    + "create table table_cr1 (field1 varchar, field2 varchar) engine InnoDB;\n";
		List<Statement> list = CCJSqlParserUtil.parseStatements(sql1);
		for (Statement stmt : list) {
			if (stmt instanceof Select || stmt instanceof Insert || stmt instanceof Update || stmt instanceof Delete || stmt instanceof Truncate) {
				System.out.println(stmt.toString());
				System.out.println("========================");
				TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
				tablesNamesFinder.analyse(stmt);
				List<String> tableListIn = tablesNamesFinder.getListTableNamesInput();
				for (String t : tableListIn) {
					System.out.println(t);
				}
				assertEquals("input", 5, tableListIn.size());
				List<String> tableListOut = tablesNamesFinder.getListTableNamesOutput();
				for (String t : tableListOut) {
					System.out.println(t);
				}
				assertEquals("output", 1, tableListOut.size());
				List<String> functionNames = tablesNamesFinder.getListFunctionSignatures();
				for (String f : functionNames) {
					System.out.println(f);
				}
				assertEquals("function", 1, functionNames.size());
			} else if (stmt instanceof CreateTable) {
				System.out.println(stmt.toString());
				System.out.println("========================");
				TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
				tablesNamesFinder.analyse(stmt);
				List<String> tableListCreate = tablesNamesFinder.getListTableNamesCreate();
				for (String t : tableListCreate) {
					System.out.println(t);
				}
				assertEquals("create", 1, tableListCreate.size());
			}
		}
	}

	@Test
	public void testScriptStrict() throws Exception {
		String sql1 = "set var1='1234';\n"
				+ "update `table_upd` set x=y;\n"
				+ "insert into schema_b.table_ins\n\n\n"
				+ "with ws1 as (\n"
			    + "    select schema_c.function1(x) as alias_x from schema_c.table_with1 a\n"
			    + "), \n"
			    + "ws2 as (\n"
			    + "    select y from schema_d.table_with2 b\n"
			    + ") \n"
			    + "select \n"
			    + "    a as alias_a, \n"
			    + "    b as alias_b, \n"
			    + "    (select c from schema_e.table_sel1) as alias_c \n"
			    + "from schema_a.table_sel2 ta \n"
			    + "join schema_b.table_sel3 tb using(c)\n"
			    + "join ws1 using(c)"
			    + "where ta.field = @var1;\n"
			    + "select * from table_sel4;\n"
			    + "create table table_cr1 (field1 varchar, field2 varchar) engine InnoDB;"
			    + "create table table_cr2 (field1 varchar, field2 varchar) engine InnoDB;\n";
		StrictSQLParser p = new StrictSQLParser();
		p.parseScriptFromCode(sql1);
		assertEquals("input", 6, p.getTablesRead().size());
		for (String t : p.getTablesWritten()) {
			System.out.println(t);
		}
		assertEquals("output", 2, p.getTablesWritten().size());
		assertEquals("create", 2, p.getTablesCreated().size());
	}

	@Test
	public void testCreateStrict() throws Exception {
		String sql1 = "create table table_cr1 (field1 varchar, field2 varchar) engine InnoDB;\n"
				+ "create table table_cr2 (field1 varchar, field2 varchar) engine InnoDB;";
		List<Statement> list = CCJSqlParserUtil.parseStatements(sql1);
		int count = 0;
		for (Statement stmt : list) {
			System.out.println(stmt.toString());
			System.out.println("========================");
			TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
			tablesNamesFinder.analyse(stmt);
			List<String> tableListCreate = tablesNamesFinder.getListTableNamesCreate();
			for (String t : tableListCreate) {
				System.out.println(t);
			}
			count = count + tableListCreate.size();
			assertEquals("create", 1, tableListCreate.size());
		}
		assertEquals("create total", 2, count);
	}

	@Test
	public void testStatWithoutTableStrict() throws Exception {
		String sql1 = "set var1='xyz'";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		if (stmt instanceof Insert || stmt instanceof Select || stmt instanceof Update || stmt instanceof Delete || stmt instanceof Truncate) {
			assertTrue(false);
		} else {
			System.out.println(stmt.getClass());
			assertTrue(true);
		}
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
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesInput();
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
	public void testSelectProc() throws Exception {
		String sql1 = "select a from procedureCall(1,2)";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
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
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesOutput();
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
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesOutput();
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
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesOutput();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(2, tableList.size());
	}
	
	@Test
	public void testDummySelect() throws Exception {
		String sql1 = "select current_timestamp";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesInput();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(0, tableList.size());
	}
	
	@Test
	public void testCreateTableFromSelect() throws Exception {
		String sql1 = "create table schema_a.table_target as select f1,f2 from schema_a.table_source";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesCreate();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(1, tableList.size());
		tableList = tablesNamesFinder.getListTableNamesInput();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(1, tableList.size());
	}
	
	@Test
	public void testCreateView() throws Exception {
		String sql1 = "create view schema_a.view_target as (select f1,f2 from schema_a.table_source)";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesCreate();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(1, tableList.size());
		tableList = tablesNamesFinder.getListTableNamesInput();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(1, tableList.size());
	}

	@Test
	public void testCreateViewWithJoin() throws Exception {
		String sql1 = "create view schema_a.view_target as (select a.f1,b.f2 from schema_a.table_source1 a inner join schema_a.table_source2 b on a.f1 = b.f1)";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesCreate();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(1, tableList.size());
		tableList = tablesNamesFinder.getListTableNamesInput();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(2, tableList.size());
	}

}
