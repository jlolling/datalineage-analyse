package de.jlo.analyse.sql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

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
	public void testIgnoreDummyTableFromSelectStrict() throws Exception {
		String sql1 = "SELECT * FROM dual";
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
	public void testCreateTempoTableFromSelectStrict0() throws Exception {
		String sql1 = "CREATE TEMPORARY TABLE IF NOT EXISTS new_tbl as SELECT * FROM `orig_tbls`";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
		List<String> tableList2 = tablesNamesFinder.getListTableNamesCreate();
		for (String t : tableList2) {
			System.out.println(t);
		}
		assertEquals(0, tableList2.size());
		List<String> tableList = tablesNamesFinder.getListTableNamesInput();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(1, tableList.size());
		tableList = tablesNamesFinder.getListTableNamesTemp();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals(1, tableList.size());
	}

	@Test
	public void testCreateTempoTableFromSelectStrict() throws Exception {
		String sql1 = "CREATE TEMPORARY TABLE IF NOT EXISTS new_tbl as SELECT * FROM `orig_tbls`";
		StrictSQLParser p = new StrictSQLParser();
		p.parseScriptFromCode(sql1);
		List<String> tableList2 = p.getTablesCreated();
		for (String t : tableList2) {
			System.out.println(t);
		}
		assertEquals("create", 0, tableList2.size());
		List<String> tableList = p.getTablesRead();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("input", 1, tableList.size());
	}

	@Test
	public void testCreateTempoTableFromSelectStrictPreventTemp() throws Exception {
		String sql1 = "CREATE TEMPORARY TABLE IF NOT EXISTS temp_tbl as SELECT * FROM `orig_tbls`;\n"
				+ "select * from temp_tbl;\n"
				+ "select * from any_table;";
		StrictSQLParser p = new StrictSQLParser();
		p.parseScriptFromCode(sql1);
		List<String> listTables = p.getTablesCreated();
		for (String name : listTables) {
			System.out.println(name);
		}
		assertEquals("create", 0, listTables.size());
		listTables = p.getTablesRead();
		for (String name : listTables) {
			System.out.println(name);
		}
		assertEquals("input", 2, listTables.size());
	}

	@Test
	public void testRemoveEmptyLines() throws IOException {
		String test = "insert into schema_b.table_ins\n"
				+ "-- comment\n"
				+ "\n\n\n"
				+ "with ws1 as (\n";
		String expected =  "insert into schema_b.table_ins\n"
				+ "-- comment\n"
				+ "\n"
				+ "with ws1 as (";
		String actual = SQLCodeUtil.cleanupEmptyLines(test);
		assertEquals("not match", expected, actual);
	}

	@Test
	public void testScriptStrict0() throws Exception {
		String sql1 = "set @var1 = '1234';\n"
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
	public void testLongSQL() throws Exception {
		String sql1 = "CREATE TABLE Report.Customer_Service_Report_1_cw AS ( \n"
				+ "SELECT \n"
				+ "	YEAR(MAX(DATE_ADD(CURRENT_DATE, INTERVAL - WEEKDAY(CURRENT_DATE) - 1 DAY)))     as cyearMax, \n"
				+ "	YEAR(MIN(DATE_ADD(CURRENT_DATE, INTERVAL - WEEKDAY(CURRENT_DATE) - 1 DAY)))     as cyearMin, \n"
				+ "	WEEK(MAX(DATE_ADD(CURRENT_DATE, INTERVAL - WEEKDAY(CURRENT_DATE) - 1 DAY)), 3)  as cwMax, \n"
				+ "	CURRENT_DATE                                                                    as last_update, \n"
				+ "	base.cyear, \n"
				+ "	CASE \n"
				+ "    WHEN  \n"
				+ "      (base.cyear NOT IN (2016, 2020, 2024) AND base.cw = 53) \n"
				+ "      OR (MONTH(base.cw_dtMin) = 1 AND base.cw > 51) \n"
				+ "    THEN 0  \n"
				+ "    ELSE base.cw \n"
				+ "  END AS cw, \n"
				+ "	base.cw_dtMin, \n"
				+ "	base.brand, \n"
				+ "	base.brand_type, \n"
				+ "	base.ticket_type, \n"
				+ "	IF(base.type_total = '', 'not_in_type_total', base.type_total) type_total, \n"
				+ "	base.channel, \n"
				+ "	base.`28491752_value` as tool, \n"
				+ "	base.satisfaction_rating, \n"
				+ "	IFNULL(data.tickets, 0) tickets, \n"
				+ "	data.min_first_reply, \n"
				+ "	data.min_resolution \n"
				+ "/* \n"
				+ "################################ \n"
				+ "# BASE: all Dimensions each Week (ensures that there are no missing values in tableau) \n"
				+ "################################ \n"
				+ "*/ \n"
				+ "FROM ( \n"
				+ "        SELECT \n"
				+ "        dts.cyear, \n"
				+ "        dts.cw, \n"
				+ "        dts.cw_dtMin, \n"
				+ "        dim.brand, \n"
				+ "        dim.brand_type, \n"
				+ "        dim.ticket_type, \n"
				+ "        dim.type_total, \n"
				+ "        dim.channel, \n"
				+ "        dim.`28491752_value`, \n"
				+ "        dim.satisfaction_rating \n"
				+ " \n"
				+ "        FROM (SELECT  \n"
				+ "                -- `date`                                                                          as dt, \n"
				+ "                WEEK(`date`, 3)                                                                 as cw, \n"
				+ "                YEAR(`date`)                                                                    as cyear, \n"
				+ "                DATE(MAX(`date`))     as cw_dtMax, \n"
				+ "                DATE(MIN(`date`))     as cw_dtMin \n"
				+ "                         \n"
				+ "              from ARBEIT.DIM_DATE  \n"
				+ "              WHERE `date` >= '2020-01-01'  \n"
				+ "                and `date` <= DATE_ADD(CURRENT_DATE, INTERVAL - WEEKDAY(CURRENT_DATE) - 1 DAY) \n"
				+ "              GROUP BY 1, 2 \n"
				+ "        ) dts \n"
				+ " \n"
				+ "        CROSS JOIN ( \n"
				+ "        SELECT \n"
				+ "                  src.brand, \n"
				+ "                  IF(src.brand = 'Admin Support (intern)'  \n"
				+ "                      AND src.`360006503440_id` IS NOT NULL \n"
				+ "                      AND `SYSTEM` = 0 \n"
				+ "                      AND ABANDONED = 0, \"ka\", \"not_ka\") as brand_type,  \n"
				+ "                      IFNULL( \n"
				+ "                          (CASE  \n"
				+ "                              WHEN src.brand like 'Support %' or src.brand = 'Kyto' THEN  \n"
				+ "                                  (CASE \n"
				+ "                                    WHEN src.`25180779_id` = 28636729 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Registration/Account' \n"
				+ "                                    WHEN src.`25180779_id` = 28636739 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'My Order' \n"
				+ "                                    WHEN src.`25180779_id` = 28636769 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Delivery and shipping conditions' \n"
				+ "                                    WHEN src.`25180779_id` = 29236425 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Other' \n"
				+ "                                    WHEN src.`25180779_id` = 29526209 AND SALES = 0 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 1 THEN 'Feedback' \n"
				+ "                                    WHEN src.`25180779_id` = 29635975 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Product' \n"
				+ "                                    WHEN src.`25180779_id` = 360000229869 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Product from other merchant' \n"
				+ "                                    WHEN src.`25180779_id` = 38679545 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Payment/Invoice' \n"
				+ "                                    WHEN src.`25180779_id` = 38679565 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Order cancellation' \n"
				+ "                                    WHEN src.`25180779_id` = 38679605 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Incident' \n"
				+ "                                    WHEN src.`25180779_id` = 40983905 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Product claim' \n"
				+ "                                    WHEN src.`25180779_id` IS NULL AND src.`360006503440_id` IS NULL AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Empty' \n"
				+ "                                    ELSE 'not_in_by_brand' \n"
				+ "                                  END) \n"
				+ "                              WHEN src.brand = 'Admin Support (intern)' AND src.`360006503440_id` IS NULL THEN \n"
				+ "                                  (CASE  \n"
				+ "                                    -- Admin Support (intern) - not KA \n"
				+ "                                    WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758519 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Hanging�orders' \n"
				+ "                                    WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758539 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Hanging�contracts'  \n"
				+ "                                    WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758559 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Hanging Quote�requests' \n"
				+ "                                    WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758579 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Login' \n"
				+ "                                    WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758599 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Registration' \n"
				+ "                                    WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758619 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Products Onlineshop' \n"
				+ "                                    WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758639 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Quote' \n"
				+ "                                    WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758659 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Orderoverview' \n"
				+ "                                    WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758679 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Contractportal' \n"
				+ "                                    WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758699 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'KSC' \n"
				+ "                                    WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758719 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Marketplace' \n"
				+ "                                    WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758739 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Other Errors in Shop' \n"
				+ "                                    WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` IS NULL AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Empty'  \n"
				+ "                                    ELSE 'not_in_by_brand' \n"
				+ "                                   END) \n"
				+ "                              WHEN src.brand = 'Admin Support (intern)' AND src.`360006503440_id` IS NOT NULL THEN     \n"
				+ "                                   (CASE \n"
				+ "                                   -- Admin Support (intern) - KA \n"
				+ "                                    WHEN src.`360006503440_id` IN (360006842880, 360014238659) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Issue with customer configuration in KA' \n"
				+ "                                    WHEN src.`360006503440_id` IN (360006842900, 360014238679) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Problem with PDF extraction' \n"
				+ "                                    WHEN src.`360006503440_id` IN (360006842920, 360014238699) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'DocParser' \n"
				+ "                                    WHEN src.`360006503440_id` IN (360006842940, 360014238719) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Wrong handling of KA' \n"
				+ "                                    WHEN src.`360006503440_id` IN (360006842960, 360014238759) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Feedback KA' \n"
				+ "                                    WHEN src.`360006503440_id` IN (360006503440, 360014238739) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Bug ERP' \n"
				+ "                                    WHEN src.`360006503440_id` IN (360014219399)               AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Bug KA' \n"
				+ "                                    WHEN src.`360006503440_id` IN (360013353300)               AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Match' \n"
				+ "                                    WHEN src.`360006503440_id` IN (1900002398094)              AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'IEPO' \n"
				+ "                                    WHEN src.`360006503440_id` IN (5205486064402)              AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Offboarding employee' \n"
				+ "                                    WHEN src.`360006503440_id` IN (10190157790354)             AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'DnD' \n"
				+ "                                    WHEN src.`360006503440_id` IN (360009439140, 360014238779) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Other KA'                 \n"
				+ "                                    ELSE 'not_in_by_brand' \n"
				+ "                                    END) \n"
				+ "                            END), 'not_in_by_brand') AS ticket_type, \n"
				+ "                            src.type_total, \n"
				+ "                            src.channel, \n"
				+ "                            src.`28491752_value`, -- tool \n"
				+ "                            src.satisfaction_rating \n"
				+ "              FROM       \n"
				+ "                  Report.Customer_Service_Report_0_base AS src \n"
				+ "              -- WHERE src.brand LIKE 'Support %' or src.brand = 'Kyto' or src.brand = 'Admin Support (intern)' \n"
				+ "                   \n"
				+ "              GROUP BY  \n"
				+ "                   \n"
				+ "                  src.brand,  \n"
				+ "                  ticket_type, \n"
				+ "                  brand_type, \n"
				+ "                  src.type_total, \n"
				+ "                  src.channel, \n"
				+ "                  src.`28491752_value`, -- tool \n"
				+ "                  src.satisfaction_rating \n"
				+ "                   \n"
				+ "               -- HAVING ticket_type IS NOT NULL \n"
				+ "        ) dim \n"
				+ "      GROUP BY 1, 2, 3, 4, 5, 6, 7, 8, 9, 10  \n"
				+ ") base \n"
				+ "/* \n"
				+ "################################ \n"
				+ "# DATA: actual data \n"
				+ "################################ \n"
				+ "*/ \n"
				+ "LEFT JOIN ( \n"
				+ "      SELECT \n"
				+ "          src.cwMax, \n"
				+ "          src.cyearMax, \n"
				+ "          src.cw_start_solved, \n"
				+ "          src.cw_solved, \n"
				+ "          src.cyear_solved, \n"
				+ "          src.brand, \n"
				+ "          IF(src.brand = 'Admin Support (intern)'  \n"
				+ "              AND src.`360006503440_id` IS NOT NULL \n"
				+ "              AND `SYSTEM` = 0 \n"
				+ "              AND ABANDONED = 0, \"ka\", \"not_ka\") as brand_type,  \n"
				+ "          IFNULL( \n"
				+ "              (CASE  \n"
				+ "                  WHEN src.brand like 'Support %' or src.brand = 'Kyto' THEN  \n"
				+ "                      (CASE \n"
				+ "                        WHEN src.`25180779_id` = 28636729 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Registration/Account' \n"
				+ "                        WHEN src.`25180779_id` = 28636739 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'My Order' \n"
				+ "                        WHEN src.`25180779_id` = 28636769 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Delivery and shipping conditions' \n"
				+ "                        WHEN src.`25180779_id` = 29236425 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Other' \n"
				+ "                        WHEN src.`25180779_id` = 29526209 AND SALES = 0 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 1 THEN 'Feedback' \n"
				+ "                        WHEN src.`25180779_id` = 29635975 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Product' \n"
				+ "                        WHEN src.`25180779_id` = 360000229869 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Product from other merchant' \n"
				+ "                        WHEN src.`25180779_id` = 38679545 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Payment/Invoice' \n"
				+ "                        WHEN src.`25180779_id` = 38679565 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Order cancellation' \n"
				+ "                        WHEN src.`25180779_id` = 38679605 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Incident' \n"
				+ "                        WHEN src.`25180779_id` = 40983905 AND SALES = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND TECHNICAL = 0 THEN 'Product claim' \n"
				+ "                        WHEN src.`25180779_id` IS NULL AND src.`360006503440_id` IS NULL AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Empty' \n"
				+ "                        ELSE \"not_in_by_brand\" \n"
				+ "                      END) \n"
				+ "                  WHEN src.brand = 'Admin Support (intern)' AND src.`360006503440_id` IS NULL THEN \n"
				+ "                      (CASE  \n"
				+ "                        -- Admin Support (intern) - not KA \n"
				+ "                        WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758519 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Hanging�orders' \n"
				+ "                        WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758539 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Hanging�contracts'  \n"
				+ "                        WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758559 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Hanging Quote�requests' \n"
				+ "                        WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758579 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Login' \n"
				+ "                        WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758599 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Registration' \n"
				+ "                        WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758619 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Products Onlineshop' \n"
				+ "                        WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758639 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Quote' \n"
				+ "                        WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758659 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Orderoverview' \n"
				+ "                        WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758679 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Contractportal' \n"
				+ "                        WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758699 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'KSC' \n"
				+ "                        WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758719 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Marketplace' \n"
				+ "                        WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` = 360020758739 AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Other Errors in Shop' \n"
				+ "                        WHEN src.`360006503440_id` IS NULL AND src.`360016102099_id` IS NULL AND TECHNICAL = 1 AND `SYSTEM` = 0 AND ABANDONED = 0 AND SALES = 0 THEN 'Empty'  \n"
				+ "                        ELSE \"not_in_by_brand\" \n"
				+ "                       END) \n"
				+ "                  WHEN src.brand = 'Admin Support (intern)' AND src.`360006503440_id` IS NOT NULL THEN     \n"
				+ "                       (CASE \n"
				+ "                       -- Admin Support (intern) - KA \n"
				+ "                        WHEN src.`360006503440_id` IN (360006842880, 360014238659) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Issue with customer configuration in KA' \n"
				+ "                        WHEN src.`360006503440_id` IN (360006842900, 360014238679) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Problem with PDF extraction' \n"
				+ "                        WHEN src.`360006503440_id` IN (360006842920, 360014238699) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'DocParser' \n"
				+ "                        WHEN src.`360006503440_id` IN (360006842940, 360014238719) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Wrong handling of KA' \n"
				+ "                        WHEN src.`360006503440_id` IN (360006842960, 360014238759) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Feedback KA' \n"
				+ "                        WHEN src.`360006503440_id` IN (360006503440, 360014238739) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Bug ERP' \n"
				+ "                        WHEN src.`360006503440_id` IN (360014219399)               AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Bug KA' \n"
				+ "                        WHEN src.`360006503440_id` IN (360013353300)               AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Match' \n"
				+ "                        WHEN src.`360006503440_id` IN (1900002398094)              AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'IEPO' \n"
				+ "                        WHEN src.`360006503440_id` IN (5205486064402)              AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Offboarding employee' \n"
				+ "                        WHEN src.`360006503440_id` IN (10190157790354)             AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'DnD' \n"
				+ "                        WHEN src.`360006503440_id` IN (360009439140, 360014238779) AND `SYSTEM` = 0 AND ABANDONED = 0 THEN 'Other KA'                 \n"
				+ "                        ELSE \"not_in_by_brand\" \n"
				+ "                        END) \n"
				+ "                END), 'not_in_by_brand') AS ticket_type, \n"
				+ "            src.type_total, \n"
				+ "            src.channel, \n"
				+ "            src.`28491752_value`, -- tool \n"
				+ "            src.satisfaction_rating, \n"
				+ "            COUNT(src.id) tickets, \n"
				+ "            SUM(src.reply_time_in_minutes_business) min_first_reply, \n"
				+ "            SUM(src.first_resolution_time_in_minutes_business) min_resolution \n"
				+ "      FROM       \n"
				+ "          Report.Customer_Service_Report_0_base AS src \n"
				+ "      -- WHERE src.brand LIKE 'Support %' or src.brand = 'Kyto' or src.brand = 'Admin Support (intern)' \n"
				+ "          -- src.brand != 'Admin Support (intern)' \n"
				+ "          -- AND src.brand LIKE 'Support %' or src.brand = 'Kyto' or src.brand = 'Admin Support (intern)' \n"
				+ "          -- AND src.`360006503440_id` IS NULL -- sonst werden auch KA-Themen in Admin Support (intern) mitegez�hlt unter Empty \n"
				+ "      GROUP BY  \n"
				+ "          src.cwMax, \n"
				+ "          src.cyearMax, \n"
				+ "          src.cw_start_solved, \n"
				+ "          src.cw_solved, \n"
				+ "          src.cyear_solved, \n"
				+ "          src.brand,  \n"
				+ "          ticket_type, \n"
				+ "          src.type_total, \n"
				+ "          src.channel, \n"
				+ "          src.`28491752_value`, -- tool \n"
				+ "          src.satisfaction_rating, \n"
				+ "          brand_type \n"
				+ "           \n"
				+ "       -- HAVING ticket_type IS NOT NULL \n"
				+ ") data \n"
				+ "ON base.cyear                   = data.cyear_solved  \n"
				+ "  AND base.cw                   = data.cw_solved  \n"
				+ "  AND base.brand                = data.brand  \n"
				+ "  AND base.brand_type           = data.brand_type \n"
				+ "  AND base.ticket_type          = data.ticket_type \n"
				+ "  AND base.type_total           = data.type_total \n"
				+ "  AND base.channel              = data.channel \n"
				+ "  AND base.`28491752_value`       = data.`28491752_value` -- tool \n"
				+ "  AND base.satisfaction_rating  = data.satisfaction_rating \n"
				+ "  \n"
				+ "GROUP BY  \n"
				+ "  base.cyear, \n"
				+ "  base.cw, \n"
				+ "  base.cw_dtMin, \n"
				+ "  base.brand, \n"
				+ "  base.brand_type, \n"
				+ "  base.ticket_type, \n"
				+ "  base.type_total, \n"
				+ "  base.channel, \n"
				+ "  base.`28491752_value`, -- tool \n"
				+ "  base.satisfaction_rating, \n"
				+ "  data.tickets, \n"
				+ "  data.min_first_reply, \n"
				+ "  data.min_resolution \n"
				+ ")";
		StrictSQLParser p = new StrictSQLParser();
		p.parseScriptFromCode(sql1);
		p.setTimeout(2000l);
		for (String t : p.getTablesRead()) {
			System.out.println(t);
		}
		assertEquals("input", 2, p.getTablesRead().size());
		assertEquals("create", 1, p.getTablesCreated().size());
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
	public void testSetStrict() throws Exception {
		String sql1 = "SET @to_date := CURRENT_DATE() - INTERVAL 1 DAY";
		StrictSQLParser p = new StrictSQLParser();
		p.parseScriptFromCode(sql1);
		String log = p.getParserErrorLog();
		if (log != null) {
			System.out.println(log);
		}
		assertTrue(log == null);
	}

	@Test
	public void testSelectsStrictOracle0() throws Exception {
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
		assertEquals("wrong number of read tables", 6, tableList.size());
		List<String> functionNames = tablesNamesFinder.getListFunctionSignatures();
		for (String f : functionNames) {
			System.out.println(f);
		}
		assertEquals("wrong number of functions", 1, functionNames.size());
	}

	@Test
	public void testSelectProc0() throws Exception {
		String sql1 = "select a from procedureCall(1,2)";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
		List<String> functionNames = tablesNamesFinder.getListFunctionSignatures();
		for (String f : functionNames) {
			System.out.println(f);
		}
		assertEquals("wrong number of functions", 1, functionNames.size());
	}

	@Test
	public void testTruncateTable0() throws Exception {
		String sql1 = "truncate table schema_a.table_a";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesOutput();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of written tables", 1, tableList.size());
	}

	@Test
	public void testDeleteTable0() throws Exception {
		String sql1 = "delete from schema_a.table_a a";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesOutput();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of written tables", 1, tableList.size());
	}

	@Test
	public void testUpdateTable0() throws Exception {
		String sql1 = "update schema_a.table_a a set x = b.v from schema_a.table_b b";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesOutput();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of written tables", 2, tableList.size());
	}
	
	@Test
	public void testDummySelect0() throws Exception {
		String sql1 = "select current_timestamp";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesInput();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of read tables", 0, tableList.size());
	}
	
	@Test
	public void testCreateTableFromSelect0() throws Exception {
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
		assertEquals("wrong number of read tables", 1, tableList.size());
	}
	
	@Test
	public void testCreateView0() throws Exception {
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
		assertEquals("wrong number of read tables", 1, tableList.size());
	}

	@Test
	public void testCreateViewWithJoin0() throws Exception {
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
		assertEquals("wrong number of read tables", 2, tableList.size());
	}

	@Test
	public void testInsertWithVars0() throws Exception {
		String sql1 = "INSERT INTO Report.BENELUX_SEGMENT_KPI_CALC_VERSIONS\n"
				+ "(kpi_name, comment)\n"
				+ "values ('STOCK_ORDERS', @comment_ext);";
		Statement stmt = CCJSqlParserUtil.parse(sql1);
		TableAndProcedureNameFinder tablesNamesFinder = new TableAndProcedureNameFinder();
		tablesNamesFinder.analyse(stmt);
		List<String> tableList = tablesNamesFinder.getListTableNamesOutput();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of written tables", 1, tableList.size());
	}

	@Test
	public void testInsertIntervalAndAt() throws Exception {
		String sql1 = "CREATE TEMPORARY TABLE IF NOT EXISTS order_history_init\n" + 
					"(KEY join2(CUSTOMER, DOC_NUMBER)) as \n" +
					"SELECT \n" +
					"    c.CUSTOMER, \n" +
					"    o.DOC_NUMBER,  \n" +
					"    COUNT(*) AS line_cou\n" + 
					"FROM \n" +
					"    Report.BNLX_EARLY_WARNING_CUST_SCOPE_TMP AS c \n" +
					"    INNER JOIN ARBEIT.SAP_ORDERS AS o \n" +
					"        ON c.CUSTOMER = o.CUSTOMER \n" +
					"WHERE \n" +
					"    o.CREATED_ON BETWEEN c.last_order_date - INTERVAL @history_days DAY AND c.last_order_date \n" +
					"    AND o.DOC_NUMBER IS NOT NULL \n" +
					"GROUP BY 1, 2;";
		StrictSQLParser parser = new StrictSQLParser();
		parser.parseStatementFromCode(sql1);
		List<String> tableList = parser.getTablesRead();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of read tables", 2, tableList.size());
		tableList = parser.getTablesCreated();
		assertEquals("wrong number of create tables", 0, tableList.size());
	}

	@Test
	public void testSelectWithInto() throws Exception {
		String sql1 = "SELECT MAX(id) FROM Report.BENELUX_SEGMENT_KPI_CALC_VERSIONS WHERE kpi_name='STOCK_ORDERS' INTO @ver_1;";
		StrictSQLParser p = new StrictSQLParser();
		p.parseScriptFromCode(sql1);
		List<String> tableList = p.getTablesRead();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of read tables", 1, tableList.size());
	}

	@Test
	public void testSelectEnquotedFields() throws Exception {
		String sql1 = "SELECT 1234_a FROM Report.BENELUX_SEGMENT_KPI_CALC_VERSIONS";
		StrictSQLParser p = new StrictSQLParser();
		p.parseScriptFromCode(sql1);
		List<String> tableList = p.getTablesRead();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of read tables", 1, tableList.size());
	}

	@Test
	public void testSelectWithCase() throws Exception {
		String sql1 = "SELECT\n case when f1 > 0 then 1 else 0 end as field1,\n field2 \nFROM Report.BENELUX_SEGMENT_KPI_CALC_VERSIONS";
		StrictSQLParser p = new StrictSQLParser();
		p.parseScriptFromCode(sql1);
		List<String> tableList = p.getTablesRead();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of read tables", 1, tableList.size());
	}

	@Test
	public void testInsertIgnore() throws Exception {
		String sql1 = "insert ignore into ins_table (f1,f2) values('1','2')";
		StrictSQLParser p = new StrictSQLParser();
		p.parseScriptFromCode(sql1);
		List<String> tableList = p.getTablesWritten();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of written tables", 1, tableList.size());
	}

	@Test
	public void testInsertOnConflict() throws Exception {
		String sql1 = "INSERT INTO t1 (a,b,c) VALUES (1,2,3),(4,5,6) ON DUPLICATE KEY UPDATE c=VALUES(a)+VALUES(b)";
		StrictSQLParser p = new StrictSQLParser();
		p.parseScriptFromCode(sql1);
		List<String> tableList = p.getTablesWritten();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of written tables", 1, tableList.size());
	}
	
	@Test
	public void testAnalyseView() throws Exception {
		String sql = "create or replace\n"
				+ "view ARBEIT.DIM_DATE as\n"
				+ "select\n"
				+ "	`monitor`.`datalab_calendar`.`DATE_AS_INT` AS `DATE_AS_INT`,\n"
				+ "	`monitor`.`datalab_calendar`.`DATE_AS_DATE` AS `date`,\n"
				+ "	`monitor`.`datalab_calendar`.`DAY_OF_YEAR_AS_INT` AS `day_of_year`,\n"
				+ "	`monitor`.`datalab_calendar`.`DAY_OF_WEEK_AS_INT` AS `day_of_week`,\n"
				+ "	(case\n"
				+ "		when (`monitor`.`datalab_calendar`.`DAY_OF_WEEK_AS_INT` > 5) then 1\n"
				+ "		else 0\n"
				+ "	end) AS `weekend`,\n"
				+ "	`monitor`.`datalab_calendar`.`WEEK_DAY_NAME` AS `dayname`,\n"
				+ "	`monitor`.`datalab_calendar`.`WEEK_DAY_SHORT_NAME` AS `dayname_s`,\n"
				+ "	`monitor`.`datalab_calendar`.`WEEK_AS_INT` AS `week_of_year`,\n"
				+ "	(case\n"
				+ "		when (`monitor`.`datalab_calendar`.`WEEK_AS_INT` < 10) then concat(`monitor`.`datalab_calendar`.`CAL_YEAR_AS_INT`, '_', '0', `monitor`.`datalab_calendar`.`WEEK_AS_INT`)\n"
				+ "		else concat(`monitor`.`datalab_calendar`.`CAL_YEAR_AS_INT`, '_', `monitor`.`datalab_calendar`.`WEEK_AS_INT`)\n"
				+ "	end) AS `year_week`,\n"
				+ "	`monitor`.`datalab_calendar`.`WEEK_START_DATE` AS `WEEK_START_DATE`,\n"
				+ "	`monitor`.`datalab_calendar`.`WEEK_END_DATE` AS `WEEK_END_DATE`,\n"
				+ "	(`monitor`.`datalab_calendar`.`WEEK_END_DATE` + interval -(1) day) AS `WEEK_LAST_DATE`,\n"
				+ "	`monitor`.`datalab_calendar`.`YEAR_OF_WEEK_AS_INT` AS `YEAR_OF_WEEK_AS_INT`,\n"
				+ "	`monitor`.`datalab_calendar`.`DAY_OF_MONTH_AS_INT` AS `day_of_month`,\n"
				+ "	`monitor`.`datalab_calendar`.`MONTH_AS_INT` AS `MONTH_AS_INT`,\n"
				+ "	(case\n"
				+ "		when (`monitor`.`datalab_calendar`.`MONTH_AS_INT` < 10) then concat('0', `monitor`.`datalab_calendar`.`MONTH_AS_INT`)\n"
				+ "		else `monitor`.`datalab_calendar`.`MONTH_AS_INT`\n"
				+ "	end) AS `month`,\n"
				+ "	(case\n"
				+ "		when (`monitor`.`datalab_calendar`.`MONTH_AS_INT` < 10) then concat(`monitor`.`datalab_calendar`.`CAL_YEAR_AS_INT`, '_', '0', `monitor`.`datalab_calendar`.`MONTH_AS_INT`)\n"
				+ "		else concat(`monitor`.`datalab_calendar`.`CAL_YEAR_AS_INT`, '_', `monitor`.`datalab_calendar`.`MONTH_AS_INT`)\n"
				+ "	end) AS `year_month`,\n"
				+ "	`monitor`.`datalab_calendar`.`MONTH_NAME` AS `monthname`,\n"
				+ "	`monitor`.`datalab_calendar`.`MONTH_SHORT_NAME` AS `monthname_s`,\n"
				+ "	`monitor`.`datalab_calendar`.`MONTH_START_DATE` AS `MONTH_START_DATE`,\n"
				+ "	`monitor`.`datalab_calendar`.`MONTH_END_DATE` AS `MONTH_END_DATE`,\n"
				+ "	`monitor`.`datalab_calendar`.`QUARTER_AS_INT` AS `quarter`,\n"
				+ "	`monitor`.`datalab_calendar`.`CAL_YEAR_AS_INT` AS `year`,\n"
				+ "	`monitor`.`datalab_calendar`.`FIN_YEAR_AS_INT` AS `FIN_YEAR_AS_INT`,\n"
				+ "	`monitor`.`datalab_calendar`.`FIN_MONTH_AS_INT` AS `FIN_MONTH_AS_INT`,\n"
				+ "	`monitor`.`datalab_calendar`.`FIN_QUARTER_AS_INT` AS `FIN_QUARTER_AS_INT`,\n"
				+ "	`monitor`.`datalab_calendar`.`UTC_MILLISECONDS` AS `UTC_MILLISECONDS`,\n"
				+ "	`monitor`.`datalab_calendar`.`IS_LAST_DAY_OF_MONTH` AS `IS_LAST_DAY_OF_MONTH`\n"
				+ "from\n"
				+ "	`monitor`.`datalab_calendar`";
		StrictSQLParser p = new StrictSQLParser();
		p.setTimeout(100000l);	
		p.parseScriptFromCode(sql);
		List<String> tableList = p.getTablesRead();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of read tables", 1, tableList.size());
	}
	
	@Test
	public void testAnalyseView2() throws Exception {
		String sql = "CREATE OR REPLACE\n"
				+ "ALGORITHM = UNDEFINED VIEW `Report`.`vLogisticVehiclePosition` AS\n"
				+ "select\n"
				+ "    `Report`.`LogisticVehiclePosition`.`SITE` AS `SITE`,\n"
				+ "    `Report`.`LogisticVehiclePosition`.`TKNUM` AS `TKNUM`,\n"
				+ "    `Report`.`LogisticVehiclePosition`.`DPTBG` AS `DPTBG`,\n"
				+ "    `Report`.`LogisticVehiclePosition`.`TRUCK_ID` AS `TRUCK_ID`,\n"
				+ "    `Report`.`LogisticVehiclePosition`.`TIMESTAMP` AS `TIMESTAMP`,\n"
				+ "    `Report`.`LogisticVehiclePosition`.`LONGITUDE` AS `LONGITUDE`,\n"
				+ "    `Report`.`LogisticVehiclePosition`.`LATITUDE` AS `LATITUDE`,\n"
				+ "    `Report`.`LogisticVehiclePosition`.`EVENT_ID` AS `EVENT_ID`\n"
				+ "    ,row_number() OVER (PARTITION BY `Report`.`LogisticVehiclePosition`.`TKNUM` "
				+ "ORDER BY "
				+ "    `Report`.`LogisticVehiclePosition`.`UTC_TIMESTAMP` ) AS `DRIVE_ORDER`\n"
				+ "    ,cast(NULL as char(30) charset utf8mb3) AS `CUSTOMER_NAME`\n"
				+ "from\n"
				+ "    `Report`.`LogisticVehiclePosition`;";
		StrictSQLParser p = new StrictSQLParser();
		p.setThrowExeptionInsteadOfErrorText(true);
		p.setTimeout(100000l);	
		p.parseScriptFromCode(sql);
		List<String> tableList = p.getTablesRead();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of read tables", 1, tableList.size());
		tableList = p.getTablesCreated();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of created tables", 1, tableList.size());
	}

	@Test
	public void testAnalyseView3() throws Exception {
		String sql = "create or replace\n"
				+ "view Logistics.vTransportData as\n"
				+ "select\n"
				+ "	`vttk`.`TKNUM` AS `VttkTknum`,\n"
				+ "	`yt14`.`TKNUM` AS `Yt14Tknum`,\n"
				+ "	`yt13`.`TKNUM` AS `yt13Tknum`,\n"
				+ "	year(`vttk`.`DPTBG`) AS `YearDptbg`,\n"
				+ "	month(`vttk`.`DPTBG`) AS `MonthDptbg`,\n"
				+ "	dayofmonth(`vttk`.`DPTBG`) AS `DayDptbg`,\n"
				+ "	(weekday(`vttk`.`DPTBG`) + 1) AS `Weekday`,\n"
				+ "	left(`vttk`.`TPLST`,\n"
				+ "	2) AS `CountrySalesorg`,\n"
				+ "	`vttk`.`TPLST` AS `TPLST`,\n"
				+ "	`yt14`.`PG_ID` AS `PG_ID`,\n"
				+ "	`yt14`.`DEPOT` AS `DEPOT`,\n"
				+ "	`vttk`.`ERDAT` AS `VttkErdat`,\n"
				+ "	`vttk`.`ERZET` AS `VttkErzet`,\n"
				+ "	`vttk`.`AEDAT` AS `VttkAedat`,\n"
				+ "	`vttk`.`AEZET` AS `VttkAezet`,\n"
				+ "	`yt14`.`ERDAT` AS `Yt14Erdat`,\n"
				+ "	`yt14`.`ERZET` AS `Yt14Erzet`,\n"
				+ "	`yt14`.`AEDAT` AS `Yt14Aedat`,\n"
				+ "	`yt14`.`AEZET` AS `Yt14Aezet`,\n"
				+ "	`vttk`.`SHTYP` AS `SHTYP`,\n"
				+ "	`vttk`.`TPBEZ` AS `TPBEZ`,\n"
				+ "	`vttk`.`SIGNI` AS `SIGNI`,\n"
				+ "	`vttk`.`EXTI1` AS `EXTI1`,\n"
				+ "	`vttk`.`EXTI2` AS `EXTI2`,\n"
				+ "	`vttk`.`ADD01` AS `ADD01`,\n"
				+ "	`vttk`.`ADD02` AS `ADD02`,\n"
				+ "	`vttk`.`ADD03` AS `ADD03`,\n"
				+ "	`vttk`.`ADD04` AS `ADD04`,\n"
				+ "	`vttk`.`TEXT1` AS `TEXT1`,\n"
				+ "	`vttk`.`TEXT2` AS `TEXT2`,\n"
				+ "	`vttk`.`TEXT3` AS `TEXT3`,\n"
				+ "	`vttk`.`TEXT4` AS `TEXT4`,\n"
				+ "	(case\n"
				+ "		when (coalesce(`yt14`.`YY_PAYLOAD`, 0) = 0) then `vttk`.`ALLOWED_TWGT`\n"
				+ "		else `yt14`.`YY_PAYLOAD`\n"
				+ "	end) AS `Payload`,\n"
				+ "	`yt14`.`RETURN_TO_HUB_YN` AS `RETURN_TO_HUB_YN`,\n"
				+ "	`yt14`.`YY_TOUR_REV` AS `YY_TOUR_REV`,\n"
				+ "	`yt14`.`PLART` AS `PLART`,\n"
				+ "	`yt14`.`DRIVING_PROFILE` AS `DRIVING_PROFILE`,\n"
				+ "	(case\n"
				+ "		when (coalesce(`vttk`.`YY_GROSS_WEIGHT`, 0) = 0) then (case\n"
				+ "			when (coalesce(`vttk`.`YY_TOT_WEIGHT`, 0) = 0) then (case\n"
				+ "				when (coalesce(`vttk`.`YY_TWGTA`, 0) = 0) then `yt13Tp1`.`GEWICHT_DECR`\n"
				+ "				else `vttk`.`YY_TWGTA`\n"
				+ "			end)\n"
				+ "			else `vttk`.`YY_TOT_WEIGHT`\n"
				+ "		end)\n"
				+ "		else `vttk`.`YY_GROSS_WEIGHT`\n"
				+ "	end) AS `TotalLoadWeight`,\n"
				+ "	(case\n"
				+ "		when (coalesce(`yt14`.`UTILIZATION`, 0) = 0) then `vttk`.`YY_CAP_UT_TRUCK`\n"
				+ "		else `yt14`.`UTILIZATION`\n"
				+ "	end) AS `CapacityUtilisation`,\n"
				+ "	(case\n"
				+ "		when (coalesce(`yt14`.`YY_GR_MARGIN_TOT`, 0) = 0) then `vttk`.`YY_GM_MAP`\n"
				+ "		else `yt14`.`YY_GR_MARGIN_TOT`\n"
				+ "	end) AS `GrossMargin`,\n"
				+ "	`vttk`.`TNDR_LDLG` AS `TNDR_LDLG`,\n"
				+ "	`vttk`.`TDLNR` AS `TDLNR`,\n"
				+ "	(case\n"
				+ "		when ((left(`vttk`.`TDLNR`,\n"
				+ "		2) = '08')\n"
				+ "		or (left(`vttk`.`TDLNR`,\n"
				+ "		1) = '8')) then 'Y'\n"
				+ "		else 'N'\n"
				+ "	end) AS `OwnFleet`,\n"
				+ "	`vttk`.`DPTBG` AS `DPTBG`,\n"
				+ "	`vttk`.`UPTBG` AS `UPTBG`,\n"
				+ "	`yt14`.`DATBG` AS `DATBG`,\n"
				+ "	`yt14`.`UATBG` AS `UATBG`,\n"
				+ "	`vttk`.`DPTEN` AS `DPTEN`,\n"
				+ "	`vttk`.`UPTEN` AS `UPTEN`,\n"
				+ "	`yt14`.`DATEN` AS `DATEN`,\n"
				+ "	`yt14`.`UATEN` AS `UATEN`,\n"
				+ "	((((`yt13MaxTpNum`.`DAUER` DIV 10000) * 3600) + (((`yt13MaxTpNum`.`DAUER` DIV 100) % 100) * 60)) + (`yt13MaxTpNum`.`DAUER` % 100)) AS `DAUER`,\n"
				+ "	((((`vttk`.`GESZTD` DIV 10000) * 3600) + (((`vttk`.`GESZTD` DIV 100) % 100) * 60)) + (`vttk`.`GESZTD` % 100)) AS `GESZTD`,\n"
				+ "	((((`yt14`.`GESZTDA` DIV 10000) * 3600) + (((`yt14`.`GESZTDA` DIV 100) % 100) * 60)) + (`yt14`.`GESZTDA` % 100)) AS `GESZTDA`,\n"
				+ "	((((`vttk`.`FAHZTD` DIV 10000) * 3600) + (((`vttk`.`FAHZTD` DIV 100) % 100) * 60)) + (`vttk`.`FAHZTD` % 100)) AS `FAHZTD`,\n"
				+ "	((((`yt14`.`FAHZTDA` DIV 10000) * 3600) + (((`yt14`.`FAHZTDA` DIV 100) % 100) * 60)) + (`yt14`.`FAHZTDA` % 100)) AS `FAHZTDA`,\n"
				+ "	((((`vttk`.`WARZTD` DIV 10000) * 3600) + (((`vttk`.`WARZTD` DIV 100) % 100) * 60)) + (`vttk`.`WARZTD` % 100)) AS `WARZTD`,\n"
				+ "	((((`yt14`.`WARZTDA` DIV 10000) * 3600) + (((`yt14`.`WARZTDA` DIV 100) % 100) * 60)) + (`yt14`.`WARZTDA` % 100)) AS `WARZTDA`,\n"
				+ "	`vttk`.`DISTZ` AS `DISTZ`,\n"
				+ "	`yt14`.`YY_DIST_TOT_ACT` AS `YY_DIST_TOT_ACT`,\n"
				+ "	`vttk`.`MEDST` AS `MEDST`,\n"
				+ "	`yt14`.`YY_PLAN_TOO_LONG` AS `YY_PLAN_TOO_LONG`,\n"
				+ "	`yt14`.`YY_DUR_TOO_LONG` AS `YY_DUR_TOO_LONG`,\n"
				+ "	`vtts`.`CntPlanedStops` AS `CntPlanedStops`,\n"
				+ "	`yt13Weight`.`CntVisitedStops` AS `CntVisitedStops`,\n"
				+ "	`yt13`.`AvgPlanedDropWeight` AS `AvgPlanedDropWeight`,\n"
				+ "	`yt13Weight`.`AvgActualDropWeight` AS `AvgActualDropWeight`,\n"
				+ "	`yt14`.`YY_STOP_CANCLD` AS `YY_STOP_CANCLD`,\n"
				+ "	`yt14`.`YY_ITEM_DEFIC` AS `YY_ITEM_DEFIC`,\n"
				+ "	(((hour(`yt13MaxTpNum`.`FAHRZ`) * 3600) + (minute(`yt13MaxTpNum`.`FAHRZ`) * 60)) + second(`yt13MaxTpNum`.`FAHRZ`)) AS `PlannedTimeBackDepot`,\n"
				+ "	((((((((`yt14`.`GESZTDA` DIV 10000) * 3600) + (((`yt14`.`GESZTDA` DIV 100) % 100) * 60)) + (`yt14`.`GESZTDA` % 100)) - ((`yt14`.`WARZTDA` DIV 10000) * 3600)) + (((`yt14`.`WARZTDA` DIV 100) % 100) * 60)) + (`yt14`.`WARZTDA` % 100)) - `yt13AllSum`.`SumFahztda`) AS `ActualTimeBackDepot`,\n"
				+ "	(case\n"
				+ "		when (coalesce(`yt13MaxTpNum`.`DISTANZ`, 0) = 0) then `yt13MaxTpNum`.`DISTANZ_MI`\n"
				+ "		else `yt13MaxTpNum`.`DISTANZ`\n"
				+ "	end) AS `PlannedDistanceBackDepot`,\n"
				+ "	(`yt14`.`YY_DIST_TOT_ACT` - `yt13AllSum`.`SumYyDistTpAct`) AS `ActualDistBackDepot`,\n"
				+ "	`yt13AllSum`.`FixTimeIncl` AS `FixTimeIncl`,\n"
				+ "	`yt13AllSum`.`PdfShare` AS `PdfShare`\n"
				+ "from\n"
				+ "	(((((((`Logistics`.`L_L_005_Load_VTTK` `vttk`\n"
				+ "left join `Logistics`.`L_L_010_Load_YKLLO_T_014` `yt14` on\n"
				+ "	(((`vttk`.`MANDT` = `yt14`.`MANDT`)\n"
				+ "		and (`vttk`.`TKNUM` = `yt14`.`TKNUM`)\n"
				+ "			and (`yt14`.`flag_active` = 1))))\n"
				+ "left join (\n"
				+ "	select\n"
				+ "		`Logistics`.`L_L_007_Load_VTTS`.`MANDT` AS `MANDT`,\n"
				+ "		`Logistics`.`L_L_007_Load_VTTS`.`TKNUM` AS `TKNUM`,\n"
				+ "		count(0) AS `CntPlanedStops`\n"
				+ "	from\n"
				+ "		`Logistics`.`L_L_007_Load_VTTS`\n"
				+ "	group by\n"
				+ "		`Logistics`.`L_L_007_Load_VTTS`.`MANDT`,\n"
				+ "		`Logistics`.`L_L_007_Load_VTTS`.`TKNUM`) `vtts` on\n"
				+ "	(((`vttk`.`MANDT` = `vtts`.`MANDT`)\n"
				+ "		and (`vttk`.`TKNUM` = `vtts`.`TKNUM`))))\n"
				+ "left join (\n"
				+ "	select\n"
				+ "		`yt13`.`MANDT` AS `MANDT`,\n"
				+ "		`yt13`.`TKNUM` AS `TKNUM`,\n"
				+ "		count(0) AS `CntVisitedStops`,\n"
				+ "		avg(`yt13`.`GEWICHT`) AS `AvgActualDropWeight`\n"
				+ "	from\n"
				+ "		`Logistics`.`L_L_011_Load_YKLLO_T_013` `yt13`\n"
				+ "	where\n"
				+ "		((`yt13`.`ID` < 1000)\n"
				+ "			and (`yt13`.`YY_TSNUM_ACT` > 0)\n"
				+ "				and (`yt13`.`flag_active` = 1))\n"
				+ "	group by\n"
				+ "		`yt13`.`MANDT`,\n"
				+ "		`yt13`.`TKNUM`) `yt13Weight` on\n"
				+ "	(((`vttk`.`MANDT` = `yt13Weight`.`MANDT`)\n"
				+ "		and (`vttk`.`TKNUM` = `yt13Weight`.`TKNUM`))))\n"
				+ "left join (\n"
				+ "	select\n"
				+ "		`yt13`.`MANDT` AS `MANDT`,\n"
				+ "		`yt13`.`TKNUM` AS `TKNUM`,\n"
				+ "		avg(`yt13`.`GEWICHT`) AS `AvgPlanedDropWeight`\n"
				+ "	from\n"
				+ "		`Logistics`.`L_L_011_Load_YKLLO_T_013` `yt13`\n"
				+ "	where\n"
				+ "		((`yt13`.`ID` < 1000)\n"
				+ "			and (`yt13`.`flag_active` = 1))\n"
				+ "	group by\n"
				+ "		`yt13`.`MANDT`,\n"
				+ "		`yt13`.`TKNUM`) `yt13` on\n"
				+ "	(((`vttk`.`MANDT` = `yt13`.`MANDT`)\n"
				+ "		and (`vttk`.`TKNUM` = `yt13`.`TKNUM`))))\n"
				+ "left join (\n"
				+ "	select\n"
				+ "		`yt13`.`MANDT` AS `MANDT`,\n"
				+ "		`yt13`.`TKNUM` AS `TKNUM`,\n"
				+ "		`yt13`.`GEWICHT_DECR` AS `GEWICHT_DECR`\n"
				+ "	from\n"
				+ "		`Logistics`.`L_L_011_Load_YKLLO_T_013` `yt13`\n"
				+ "	where\n"
				+ "		((`yt13`.`TPNUM` = 1)\n"
				+ "			and (`yt13`.`flag_active` = 1))) `yt13Tp1` on\n"
				+ "	(((`vttk`.`MANDT` = `yt13Tp1`.`MANDT`)\n"
				+ "		and (`vttk`.`TKNUM` = `yt13Tp1`.`TKNUM`))))\n"
				+ "left join (\n"
				+ "	select\n"
				+ "		`yt13`.`MANDT` AS `MANDT`,\n"
				+ "		`yt13`.`TKNUM` AS `TKNUM`,\n"
				+ "		`yt13`.`DAUER` AS `DAUER`,\n"
				+ "		`yt13`.`FAHRZ` AS `FAHRZ`,\n"
				+ "		`yt13`.`DISTANZ` AS `DISTANZ`,\n"
				+ "		`yt13`.`DISTANZ_MI` AS `DISTANZ_MI`\n"
				+ "	from\n"
				+ "		(`Logistics`.`L_L_011_Load_YKLLO_T_013` `yt13`\n"
				+ "	join (\n"
				+ "		select\n"
				+ "			`yt13`.`MANDT` AS `MANDT`,\n"
				+ "			`yt13`.`TKNUM` AS `TKNUM`,\n"
				+ "			max(`yt13`.`TPNUM`) AS `TPNUM`\n"
				+ "		from\n"
				+ "			`Logistics`.`L_L_011_Load_YKLLO_T_013` `yt13`\n"
				+ "		where\n"
				+ "			(`yt13`.`flag_active` = 1)\n"
				+ "		group by\n"
				+ "			`yt13`.`MANDT`,\n"
				+ "			`yt13`.`TKNUM`) `yt13_2` on\n"
				+ "		(((`yt13`.`MANDT` = `yt13_2`.`MANDT`)\n"
				+ "			and (`yt13`.`TKNUM` = `yt13_2`.`TKNUM`)\n"
				+ "				and (`yt13`.`TPNUM` = `yt13_2`.`TPNUM`))))\n"
				+ "	where\n"
				+ "		(`yt13`.`flag_active` = 1)) `yt13MaxTpNum` on\n"
				+ "	(((`vttk`.`MANDT` = `yt13MaxTpNum`.`MANDT`)\n"
				+ "		and (`vttk`.`TKNUM` = `yt13MaxTpNum`.`TKNUM`))))\n"
				+ "left join (\n"
				+ "	select\n"
				+ "		`yt13`.`MANDT` AS `MANDT`,\n"
				+ "		`yt13`.`TKNUM` AS `TKNUM`,\n"
				+ "		sum(((((`yt13`.`FAHZTDA` DIV 10000) * 3600) + (((`yt13`.`FAHZTDA` DIV 100) % 100) * 60)) + (`yt13`.`FAHZTDA` % 100))) AS `SumFahztda`,\n"
				+ "		sum(`yt13`.`YY_DIST_TP_ACT`) AS `SumYyDistTpAct`,\n"
				+ "		max(`yt13`.`YY_FIX_TIME`) AS `FixTimeIncl`,\n"
				+ "		(sum((case when (`yt13`.`YY_PDF_OK` = 'X') then 1 else 0 end)) / sum((case when (`yt13`.`YY_TSNUM_ACT` > 0) then 1 else 0 end))) AS `PdfShare`\n"
				+ "	from\n"
				+ "		`Logistics`.`L_L_011_Load_YKLLO_T_013` `yt13`\n"
				+ "	where\n"
				+ "		(`yt13`.`flag_active` = 1)\n"
				+ "	group by\n"
				+ "		`yt13`.`MANDT`,\n"
				+ "		`yt13`.`TKNUM`) `yt13AllSum` on\n"
				+ "	(((`yt13AllSum`.`MANDT` = `vttk`.`MANDT`)\n"
				+ "		and (`yt13AllSum`.`TKNUM` = `vttk`.`TKNUM`))))\n"
				+ "where\n"
				+ "	((1 = 1)\n"
				+ "		and (`vttk`.`SHTYP` not in ('YK99', 'YC20')))\n"
				+ "";
		StrictSQLParser p = new StrictSQLParser();
		p.setThrowExeptionInsteadOfErrorText(false);
		p.setTimeout(100000l);	
		p.parseScriptFromCode(sql);
		List<String> tableList = p.getTablesRead();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of read tables", 4, tableList.size());
		tableList = p.getTablesCreated();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of created tables", 1, tableList.size());
	}

	@Test
	public void testAnalyseView4() throws Exception {
		String sql = "	select\n"
				+ "		`yt13`.`MANDT` AS `MANDT`,\n"
				+ "		`yt13`.`TKNUM` AS `TKNUM`,\n"
				+ "		sum(((`yt13`.`FAHZTDA` / 10000) * 3600) + (((`yt13`.`FAHZTDA` / 100) % 100) * 60) + (`yt13`.`FAHZTDA` % 100)) AS `SumFahztda`,\n"
//				+ "     sum(((`yt13`.`FAHZTDA` / 10000) * 3600) + (((`yt13`.`FAHZTDA` / 100) % 100) * 60) + (`yt13`.`FAHZTDA` % 100)) AS `SumFahztda`,\n"
				+ "		sum(`yt13`.`YY_DIST_TP_ACT`) AS `SumYyDistTpAct`,\n"
				+ "		max(`yt13`.`YY_FIX_TIME`) AS `FixTimeIncl`,\n"
				+ "		sum((case when (`yt13`.`YY_PDF_OK` = 'X') then 1 else 0 end)) / sum((case when (`yt13`.`YY_TSNUM_ACT` > 0) then 1 else 0 end)) AS `PdfShare`\n"
				+ "	from\n"
				+ "		`Logistics`.`L_L_011_Load_YKLLO_T_013` `yt13`\n"
				+ "	where\n"
				+ "		(`yt13`.`flag_active` = 1)\n"
				+ "	group by\n"
				+ "		`yt13`.`MANDT`,\n"
				+ "		`yt13`.`TKNUM`"
		;
		StrictSQLParser p = new StrictSQLParser();
		p.setThrowExeptionInsteadOfErrorText(true);
		p.setTimeout(100000l);	
		p.parseScriptFromCode(sql);
		List<String> tableList = p.getTablesRead();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of read tables", 1, tableList.size());
		tableList = p.getTablesCreated();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of created tables", 0, tableList.size());
	}
	
	@Test
	public void testSelectWithDoubleQuoteStrict() throws Exception {
		String sql1 = "/* ApplicationName=L_L_013_VehiclePosition_PARCON_DTGPS_PS.tMysqlTableTransfer_1 */\n"
				+ "SELECT \n"
				+ "    MANDT,\n"
				+ "    GUID,\n"
				+ "    UTC_TIMESTAMP,\n"
				+ "    CASE WHEN UTC_DATE IS NOT NULL AND UTC_DATE > '00000000' THEN to_date(UTC_DATE,'YYYYMMDD') ELSE NULL end AS UTC_DATE,\n"
				+ "    CASE WHEN UTC_TIME IS NOT NULL AND UTC_TIME > '000000' THEN to_time(UTC_TIME,'HHMISS') ELSE NULL end AS UTC_TIME,\n"
				+ "    SOURCE_APP,\n"
				+ "    SOURCE_DEVICE,\n"
				+ "    LONGITUDE,\n"
				+ "    LATITUDE,\n"
				+ "    ALTITUDE,\n"
				+ "    POS_TEXT,\n"
				+ "    SPEED,\n"
				+ "    COURSE,\n"
				+ "    NUMSAT,\n"
				+ "    ACCURACY,\n"
				+ "    RELATION_TYPE,\n"
				+ "    RELATION_KEY,\n"
				+ "    DRIVER,\n"
				+ "    REL_DRIVER,\n"
				+ "    TAG1,\n"
				+ "    TAG2\n"
				+ "FROM\n"
				+ "    SAPSR3.\"/PARCON/DTGPS_PS\"\n"
				+ "WHERE\n"
				+ "	UTC_TIMESTAMP > '20240419111542'";
		StrictSQLParser p = new StrictSQLParser();
		p.setThrowExeptionInsteadOfErrorText(true);
		p.setTimeout(100000l);	
		p.parseScriptFromCode(sql1);
		List<String> tableList = p.getTablesRead();
		for (String t : tableList) {
			System.out.println(t);
		}
		assertEquals("wrong number of read tables", 1, tableList.size());
	}


}
