package de.jlo.analyse.talend;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.jlo.analyse.sql.SQLCodeUtil;

public class TestContextVarResolver {
	
	@Test
	public void testReplaceContextInOutput1() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.addContextVar("TABLE", "mytable");
		String testSQL = "context.TABLE + \"_temp\"";
		String expected = "mytable_temp\"";
		String actual = r.replaceContextVars(testSQL);
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
		r.addContextVar("TABLE", "mytable");
		String testSQL = "\"temp_\" + context.TABLE";
		String expected = "\"temp_mytable";
		String actual = r.replaceContextVars(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextInOutput3() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.addContextVar("TABLE", "mytable");
		String testSQL = "\"temp_\" + context.TABLE + \"_xxx\"";
		String expected = "\"temp_mytable_xxx\"";
		String actual = r.replaceContextVars(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextInOutput4() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.addContextVar("TABLE", "mytable");
		String testSQL = "context.TABLE + \"_temp\"";
		String expected = "mytable_temp\"";
		String actual = r.replaceContextVars(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextInOutput5() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.addContextVar("TABLE", "mytable");
		String testSQL = "context.TABLE";
		String expected = "mytable";
		String actual = r.replaceContextVars(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextEncapulated() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.addContextVar("DB1_Schema", "schema_1");
		r.addContextVar("DB2_Schema", "schema_2");
		String testSQL = "select * from \" + context.DB1_Schema + \".table1,\n\" + context.DB2_Schema + \".table2";
		String expected = "select * from schema_1.table1,\nschema_2.table2";
		String actual = r.replaceContextVars(testSQL);
		assertEquals("Fail", expected, actual);
	}
	
	@Test
	public void testReplaceContextEncapulated2() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.addContextVar("DB1_Schema", "schema_1");
		r.addContextVar("DB2_Schema", "schema_2");
		String testSQL = "\"select * from \" + context.DB1_Schema + \".table1,\n\" + context.DB2_Schema + \".table2\"";
		String expected = "select * from schema_1.table1,\nschema_2.table2";
		String actual = SQLCodeUtil.convertJavaToSqlCode(r.replaceContextVars(testSQL));
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testReplaceContextSimple() throws Exception {
		ContextVarResolver r = new ContextVarResolver();
		r.addContextVar("DB1_Schema", "schema_1");
		r.addContextVar("DB2_Schema", "schema_2");
		String testSQL = "select * from \" + context.DB1_Schema + \".table1,\n\" + context.DB2_Schema + \".table2";
		String expected = "select * from schema_1.table1,\nschema_2.table2";
		String actual = r.replaceContextVars(testSQL);
		assertEquals("Fail", expected, actual);
	}

	@Test
	public void testRetrievePureSQL() throws Exception {
		String tq = "\"SELECT\n\\\"\"+context.B17_MANAGEMENT_DB_Database+\"\\\".\\\"\" + context.B17_MANAGEMENT_DB_Schema +  \"\\\".\\\"measureconfig\\\".\\\"job_instance_id\\\"\nFROM \\\"\"+context.B17_MANAGEMENT_DB_Database+\"\\\".\\\"\"+context.B17_MANAGEMENT_DB_Schema+\"\\\".\\\"measureconfig\\\"\"";
		System.out.println(tq);
		// first replace the context vars
		ContextVarResolver r = new ContextVarResolver();
		r.addContextVar("B17_MANAGEMENT_DB_Database", "nucleus");
		r.addContextVar("B17_MANAGEMENT_DB_Schema", "b17_management");
		String withoutContextActual = r.replaceContextVars(tq);
		String withoutContextExcepted = "\"SELECT\n\\\"nucleus\\\".\\\"b17_management\\\".\\\"measureconfig\\\".\\\"job_instance_id\\\"\nFROM \\\"nucleus\\\".\\\"b17_management\\\".\\\"measureconfig\\\"\"";
		assertEquals("Context replacement failed", withoutContextExcepted, withoutContextActual);
		// convert String to SQL
		String actual = SQLCodeUtil.convertJavaToSqlCode(withoutContextActual).trim();
		String expected = "SELECT\n\"nucleus\".\"b17_management\".\"measureconfig\".\"job_instance_id\"\nFROM \"nucleus\".\"b17_management\".\"measureconfig\"";
		assertEquals("Convert Java to SQL failed", expected, actual);
	}
	
	@Test
	public void testReplaceGlobalMapVars() throws Exception {
		String tq = "\"select * from schema_a.table_a where x = \" + ((String) globalMap.get(\"key\"))";
		String replacedVarsActual = SQLCodeUtil.replaceGlobalMapVars(tq);
		String replacedVarsExpected = "\"select * from schema_a.table_a where x = 999999";
		assertEquals("Replace globalMap failed", replacedVarsExpected, replacedVarsActual);
		String actual = SQLCodeUtil.convertJavaToSqlCode(replacedVarsActual);
		String expected = "select * from schema_a.table_a where x = 999999";
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
		r.addContextVar("di_staging_otk_Database", "staging_otk");
		r.addContextVar("current_file_id", "99");
		r.addContextVar("table", "otk_contract");
		String actual = r.replaceContextVars(test);
		assertEquals("Convert context vars failed", expected, actual);
	}

	@Test
	public void testReplaceGlobalMapVarsNotFound() throws Exception {
		String expected = "with idtype_til1 as (\n"
	    + "        select b17_core.catalog_id_by_sysname('track_id_label'::character varying, 'productidentity_type'::character varying) as id\n"
	    + "    ),\n"
	    + "    idtype_isrc as (\n"
	    + "        select b17_core.catalog_id_by_sysname('isrc'::character varying, 'productidentity_type'::character varying) as id\n"
	    + "    ),idtype_isrc1 as (\n"
	    + "        select b17_core.catalog_id_by_sysname('isrc'::character varying, 'productidentity_type'::character varying) as id\n"
	    + "    ) , \n"
	    + "    data_status_del as (\n"
	    + "        select b17_core.catalog_id_by_sysname('deleted'::character varying, 'data_status'::character varying) as id\n"
	    + "    )\n"
	    + "    select \n"
	    + "        ro.owner_id,\n"
	    + "        tpu.tu_id,\n"
	    + "        c.product_id,\n"
	    + "        b17_core.catalog_sysname_by_id(c.role_id) as participation_role,\n"
	    + "        p.display_mainartist,\n"
	    + "        t.title,\n"
	    + "        id_til.identifier as track_id_label,\n"
	    + "        id_isrc.identifier as isrc,\n"
	    + "        rop.job_instance_id,\n"
	    + "        tpu.valid_from,\n"
	    + "        tpu.valid_to,\n"
	    + "        b17_core.catalog_sysname_by_id(bo_ro.process_status_id) as right_owner_ship_status\n"
	    + "    from navi.rights_ownership_participation rop\n"
	    + "    join navi.credits c on c.businessobject_id = rop.credit_id\n"
	    + "    join navi.rights_ownership ro on ro.businessobject_id = rop.rights_ownership_id\n"
	    + "    join navi.businessobject bo_ro on bo_ro.businessobject_id = rop.rights_ownership_id \n"
	    + "        and bo_ro.data_status_id <> (select id from data_status_del)\n"
	    + "    join navi.product p on p.businessobject_id = c.product_id\n"
	    + "    join navi.businessobject bo_p on bo_p.businessobject_id = c.product_id and bo_p.data_status_id <> (select id from data_status_del)\n"
	    + "    join navi.title t on t.product_id = c.product_id and t.primary_selection = true\n"
	    + "    left join navi.identifier id_til on id_til.businessobject_id = c.product_id \n"
	    + "        and id_til.identifiertype_id = (select id from idtype_til)\n"
	    + "    left join navi.identifier id_isrc on id_isrc.businessobject_id = c.product_id \n"
	    + "        and id_isrc.identifiertype_id = (select id from idtype_isrc)\n"
	    + "    join b17_core.tpu tpu on tpu.tpu_id = ro.tpu_id"
	    + "		        WHERE   trav.TRAV_STATUS IN ( 1, 2, 3 )\n"
	    + "		                AND trav.TRAV_SPA_ID IN ( 12, 13, 31, 32, 33, 34, 35, 36, 37, 39, 40,\n"
	    + "		                                          43, 44, 46, 48, 49, 50, 51, 52, 54, 55, 134,\n"
	    + "		                                          135, 138, 140, 145, 150, 151, 152, 155, 157,\n"
	    + "		                                          162, 167, 169, 170, 182, 183, 184, 185, 195,\n"
	    + "		                                          196, 198, 199, 200, 201, 202, 203, 204, 205,\n"
	    + "		                                          206, 207, 208, 209, 210, 211, 213, 214, 215,\n"
	    + "		                                          216, 217, 218, 219, 221, 222, 223, 224, 225,\n"
	    + "		                                          226, 228, 229, 230, 231, 233, 234, 235, 243 )\n";
		// we expect an unchanged code!
		String actual = SQLCodeUtil.replaceGlobalMapVars(expected);
		assertEquals("The code has been changed!", expected, actual);
	}

	@Test
	public void testReplaceGlobalMapVarsFound() throws Exception {
		String test = "with idtype_til1 as (\n"
	    + "        select b17_core.catalog_id_by_sysname('track_id_label'::character varying, 'productidentity_type'::character varying) as id\n"
	    + "    ),\n"
	    + "    idtype_isrc as (\n"
	    + "        select b17_core.catalog_id_by_sysname('isrc'::character varying, 'productidentity_type'::character varying) as id\n"
	    + "    ),idtype_isrc1 as (\n"
	    + "        select b17_core.catalog_id_by_sysname('isrc'::character varying, 'productidentity_type'::character varying) as id\n"
	    + "    ) , \n"
	    + "    data_status_del as (\n"
	    + "        select b17_core.catalog_id_by_sysname('deleted'::character varying, 'data_status'::character varying) as id\n"
	    + "    )\n"
	    + "    select \n"
	    + "        ro.owner_id,\n"
	    + "        tpu.tu_id,\n"
	    + "        c.product_id,\n"
	    + "        b17_core.catalog_sysname_by_id(c.role_id) as participation_role,\n"
	    + "        p.display_mainartist,\n"
	    + "        t.title,\n"
	    + "        id_til.identifier as track_id_label,\n"
	    + "        id_isrc.identifier as isrc,\n"
	    + "        rop.job_instance_id,\n"
	    + "        tpu.valid_from,\n"
	    + "        tpu.valid_to,\n"
	    + "        b17_core.catalog_sysname_by_id(bo_ro.process_status_id) as right_owner_ship_status\n"
	    + "    from navi.rights_ownership_participation rop\n"
	    + "    join navi.credits c on c.businessobject_id = rop.credit_id\n"
	    + "    join navi.rights_ownership ro on ro.businessobject_id = rop.rights_ownership_id\n"
	    + "    join navi.businessobject bo_ro on bo_ro.businessobject_id = rop.rights_ownership_id \n"
	    + "        and bo_ro.data_status_id <> (select id from data_status_del)\n"
	    + "    join navi.product p on p.businessobject_id = c.product_id and x = \" + ((String) globalMap.get(\"key\")) + \"\n"
	    + "    join navi.businessobject bo_p on bo_p.businessobject_id = c.product_id and bo_p.data_status_id <> (select id from data_status_del)\n"
	    + "    join navi.title t on t.product_id = c.product_id and t.primary_selection = true\n"
	    + "    left join navi.identifier id_til on id_til.businessobject_id = c.product_id \n"
	    + "        and id_til.identifiertype_id = (select id from idtype_til)\n"
	    + "    left join navi.identifier id_isrc on id_isrc.businessobject_id = c.product_id \n"
	    + "        and id_isrc.identifiertype_id = (select id from idtype_isrc)\n"
	    + "    join b17_core.tpu tpu on tpu.tpu_id = ro.tpu_id"
	    + "		        WHERE   trav.TRAV_STATUS IN ( 1, 2, 3 )\n"
	    + "		                AND trav.TRAV_SPA_ID IN ( 12, 13, 31, 32, 33, 34, 35, 36, 37, 39, 40,\n"
	    + "		                                          43, 44, 46, 48, 49, 50, 51, 52, 54, 55, 134,\n"
	    + "		                                          135, 138, 140, 145, 150, 151, 152, 155, 157,\n"
	    + "		                                          162, 167, 169, 170, 182, 183, 184, 185, 195,\n"
	    + "		                                          196, 198, 199, 200, 201, 202, 203, 204, 205,\n"
	    + "		                                          206, 207, 208, 209, 210, 211, 213, 214, 215,\n"
	    + "		                                          216, 217, 218, 219, 221, 222, 223, 224, 225,\n"
	    + "		                                          226, 228, 229, 230, 231, 233, 234, 235, 243 )\n";
		String expected = "with idtype_til1 as (\n"
			    + "        select b17_core.catalog_id_by_sysname('track_id_label'::character varying, 'productidentity_type'::character varying) as id\n"
			    + "    ),\n"
			    + "    idtype_isrc as (\n"
			    + "        select b17_core.catalog_id_by_sysname('isrc'::character varying, 'productidentity_type'::character varying) as id\n"
			    + "    ),idtype_isrc1 as (\n"
			    + "        select b17_core.catalog_id_by_sysname('isrc'::character varying, 'productidentity_type'::character varying) as id\n"
			    + "    ) , \n"
			    + "    data_status_del as (\n"
			    + "        select b17_core.catalog_id_by_sysname('deleted'::character varying, 'data_status'::character varying) as id\n"
			    + "    )\n"
			    + "    select \n"
			    + "        ro.owner_id,\n"
			    + "        tpu.tu_id,\n"
			    + "        c.product_id,\n"
			    + "        b17_core.catalog_sysname_by_id(c.role_id) as participation_role,\n"
			    + "        p.display_mainartist,\n"
			    + "        t.title,\n"
			    + "        id_til.identifier as track_id_label,\n"
			    + "        id_isrc.identifier as isrc,\n"
			    + "        rop.job_instance_id,\n"
			    + "        tpu.valid_from,\n"
			    + "        tpu.valid_to,\n"
			    + "        b17_core.catalog_sysname_by_id(bo_ro.process_status_id) as right_owner_ship_status\n"
			    + "    from navi.rights_ownership_participation rop\n"
			    + "    join navi.credits c on c.businessobject_id = rop.credit_id\n"
			    + "    join navi.rights_ownership ro on ro.businessobject_id = rop.rights_ownership_id\n"
			    + "    join navi.businessobject bo_ro on bo_ro.businessobject_id = rop.rights_ownership_id \n"
			    + "        and bo_ro.data_status_id <> (select id from data_status_del)\n"
			    + "    join navi.product p on p.businessobject_id = c.product_id and x = 999999\n"
			    + "    join navi.businessobject bo_p on bo_p.businessobject_id = c.product_id and bo_p.data_status_id <> (select id from data_status_del)\n"
			    + "    join navi.title t on t.product_id = c.product_id and t.primary_selection = true\n"
			    + "    left join navi.identifier id_til on id_til.businessobject_id = c.product_id \n"
			    + "        and id_til.identifiertype_id = (select id from idtype_til)\n"
			    + "    left join navi.identifier id_isrc on id_isrc.businessobject_id = c.product_id \n"
			    + "        and id_isrc.identifiertype_id = (select id from idtype_isrc)\n"
			    + "    join b17_core.tpu tpu on tpu.tpu_id = ro.tpu_id"
			    + "		        WHERE   trav.TRAV_STATUS IN ( 1, 2, 3 )\n"
			    + "		                AND trav.TRAV_SPA_ID IN ( 12, 13, 31, 32, 33, 34, 35, 36, 37, 39, 40,\n"
			    + "		                                          43, 44, 46, 48, 49, 50, 51, 52, 54, 55, 134,\n"
			    + "		                                          135, 138, 140, 145, 150, 151, 152, 155, 157,\n"
			    + "		                                          162, 167, 169, 170, 182, 183, 184, 185, 195,\n"
			    + "		                                          196, 198, 199, 200, 201, 202, 203, 204, 205,\n"
			    + "		                                          206, 207, 208, 209, 210, 211, 213, 214, 215,\n"
			    + "		                                          216, 217, 218, 219, 221, 222, 223, 224, 225,\n"
			    + "		                                          226, 228, 229, 230, 231, 233, 234, 235, 243 )\n";
		// we expect an unchanged code!
		String actual = SQLCodeUtil.replaceGlobalMapVars(test);
		assertEquals("Change is incorrect!", expected, actual);
	}

	@Test
	public void testReplaceGlobalMapVarsFoundInMS_SQL() throws Exception {
		String test = "SELECT  trav.TRAV_ID AS 'AV_Produktnummer_ARTSYS' ,\n"
			    + "		        trpv.TRPV_ID AS 'AV_Mitwirkungsnummer_ARTSYS'\n"
			    + "		        FROM    artist.gasgvl.TBL_TRACKS_VIDEO trav\n"
			    + "		                JOIN artist.gasgvl.TBL_CHARACTER_VIDEO chv ON chv.CHV_TRAV_ID = trav.TRAV_ID\n"
			    + "		                JOIN artist.gasgvl.TBL_PARTICIPATION_VIDEO trpv ON trpv.TRPV_CHV_ID = chv.CHV_ID\n"
			    + "		                LEFT JOIN artist.gasgvl.TBL_BASIC_ARTISTS kue ON kue.KUE_ID = trpv.TRPV_KUE_ID\n"
			    + "		                LEFT JOIN artist.gasgvl.TBL_APP_USER usr ON usr.USR_KUE_ID = kue.KUE_ID\n"
			    + "		        WHERE   trav.TRAV_STATUS IN ( 1, 2, 3 ) and x = \" + ((String) globalMap.get(\"key\")) + \"\n"
			    + "		                AND trav.TRAV_SPA_ID IN ( 12, 13, 31, 32, 33, 34, 35, 36, 37, 39, 40,\n"
			    + "		                                          43, 44, 46, 48, 49, 50, 51, 52, 54, 55, 134,\n"
			    + "		                                          135, 138, 140, 145, 150, 151, 152, 155, 157,\n"
			    + "		                                          162, 167, 169, 170, 182, 183, 184, 185, 195,\n"
			    + "		                                          196, 198, 199, 200, 201, 202, 203, 204, 205,\n"
			    + "		                                          206, 207, 208, 209, 210, 211, 213, 214, 215,\n"
			    + "		                                          216, 217, 218, 219, 221, 222, 223, 224, 225,\n"
			    + "		                                          226, 228, 229, 230, 231, 233, 234, 235, 243 )\n"
			    + "		                AND trpv.TRPV_SSO_ID = 1 -- nur GVL-Direktberechtigte und Cast-Infos\n"
			    + "		        -- keine MWM von GVL-Mitarbeitern\n"
			    + "		                AND ( usr.USR_UGR_ID != 8\n"
			    + "		                      OR usr.USR_UGR_ID IS NULL\n"
			    + "		                    )  and x = \" + ((String) globalMap.get(\"key\")) + \"\n"
			    + "		        -- Verifikationsstatus darf nicht 9 sein\n"
			    + "		                AND trpv.TRPV_ADDSTATE != 9\n"
			    + "		        -- Musicaldarsteller/-in / Mitglied Tanzensemble ausschließen\n"
			    + "		                AND NOT ( trpv.TRPV_BRO_ID = 4000000026\n"
			    + "		                          AND trpv.TRPV_BFU_ID = 11\n"
			    + "		                        )\n"
			    + "		                AND trpv.TRPV_BRO_ID IS NOT NULL\n"
			    + "		                AND trpv.TRPV_KUE_ID IS NOT NULL\n"
			    + "		        -- AND existiert an einem migrierten AV-Bestandsprodukt\n"
			    + "		        -- Geschäftspartner mit zu ignorierenden Vertragsnummern herausfiltern\n"
			    + "		        -- aber Cast-Informationen nicht unterdrücken\n"
			    + "		                AND ( NOT kue.KUE_KUENR1_PK IN ( '090009', '090232', '090367',\n"
			    + "		                                                 '090989', '090991', '090992',\n"
			    + "		                                                 '090993', '090994', '090995',\n"
			    + "		                                                 '090996', '090997', '091058',\n"
			    + "		                                                 '091059', '099845', '099846',\n"
			    + "		                                                 '099847', '099848', '099849',\n"
			    + "		                                                 '099850', '099851', '099852',\n"
			    + "		                                                 '099853', '099854', '099855',\n"
			    + "		                                                 '099856', '099857', '099858',\n"
			    + "		                                                 '099859', '099860', '099861',\n"
			    + "		                                                 '099862', '099863', '099864',\n"
			    + "		                                                 '099865', '099867', '099868',\n"
			    + "		                                                 '099869', '099870', '099871',\n"
			    + "		                                                 '099872', '099875', '099880',\n"
			    + "		                                                 '099887', '099888', '099889',\n"
			    + "		                                                 '099890', '099891', '099892',\n"
			    + "		                                                 '099893', '099894', '099895',\n"
			    + "		                                                 '099896', '099897', '099898',\n"
			    + "		                                                 '099899', '099900', '099901',\n"
			    + "		                                                 '099902', '099903', '099904',\n"
			    + "		                                                 '099905', '099906', '099907',\n"
			    + "		                                                 '099908', '099909', '099910',\n"
			    + "		                                                 '099911', '099912', '099913',\n"
			    + "		                                                 '099914', '099915', '099916',\n"
			    + "		                                                 '099917', '099918', '099919',\n"
			    + "		                                                 '099920', '099921', '099922',\n"
			    + "		                                                 '099923', '099924', '099925',\n"
			    + "		                                                 '099926', '099927', '099928',\n"
			    + "		                                                 '099929', '099930', '099931',\n"
			    + "		                                                 '099932', '099933', '099934',\n"
			    + "		                                                 '099935', '099936', '099937',\n"
			    + "		                                                 '099938', '099939', '099940',\n"
			    + "		                                                 '099942', '099943', '099944',\n"
			    + "		                                                 '099945', '099946', '099947',\n"
			    + "		                                                 '099948', '099949', '099950',\n"
			    + "		                                                 '099951', '099952', '099953',\n"
			    + "		                                                 '099954', '099955', '099958',\n"
			    + "		                                                 '099959', '099960', '099960',\n"
			    + "		                                                 '099962', '099963', '099964',\n"
			    + "		                                                 '099965', '099966', '099967',\n"
			    + "		                                                 '099968', '099969', '099970',\n"
			    + "		                                                 '099971', '099972', '099973',\n"
			    + "		                                                 '099974', '099975', '099976',\n"
			    + "		                                                 '099977', '099978', '099979',\n"
			    + "		                                                 '099980', '099981', '099982',\n"
			    + "		                                                 '099983', '099984', '099985',\n"
			    + "		                                                 '099986', '099987', '099988',\n"
			    + "		                                                 '099989', '099990', '099991',\n"
			    + "		                                                 '099992', '099993', '099994',\n"
			    + "		                                                 '099995', '099996', '099997',\n"
			    + "		                                                 '099998', '099999' )\n"
			    + "		                    )";
		String expected = "SELECT  trav.TRAV_ID AS 'AV_Produktnummer_ARTSYS' ,\n"
			    + "		        trpv.TRPV_ID AS 'AV_Mitwirkungsnummer_ARTSYS'\n"
			    + "		        FROM    artist.gasgvl.TBL_TRACKS_VIDEO trav\n"
			    + "		                JOIN artist.gasgvl.TBL_CHARACTER_VIDEO chv ON chv.CHV_TRAV_ID = trav.TRAV_ID\n"
			    + "		                JOIN artist.gasgvl.TBL_PARTICIPATION_VIDEO trpv ON trpv.TRPV_CHV_ID = chv.CHV_ID\n"
			    + "		                LEFT JOIN artist.gasgvl.TBL_BASIC_ARTISTS kue ON kue.KUE_ID = trpv.TRPV_KUE_ID\n"
			    + "		                LEFT JOIN artist.gasgvl.TBL_APP_USER usr ON usr.USR_KUE_ID = kue.KUE_ID\n"
			    + "		        WHERE   trav.TRAV_STATUS IN ( 1, 2, 3 ) and x = 999999\n"
			    + "		                AND trav.TRAV_SPA_ID IN ( 12, 13, 31, 32, 33, 34, 35, 36, 37, 39, 40,\n"
			    + "		                                          43, 44, 46, 48, 49, 50, 51, 52, 54, 55, 134,\n"
			    + "		                                          135, 138, 140, 145, 150, 151, 152, 155, 157,\n"
			    + "		                                          162, 167, 169, 170, 182, 183, 184, 185, 195,\n"
			    + "		                                          196, 198, 199, 200, 201, 202, 203, 204, 205,\n"
			    + "		                                          206, 207, 208, 209, 210, 211, 213, 214, 215,\n"
			    + "		                                          216, 217, 218, 219, 221, 222, 223, 224, 225,\n"
			    + "		                                          226, 228, 229, 230, 231, 233, 234, 235, 243 )\n"
			    + "		                AND trpv.TRPV_SSO_ID = 1 -- nur GVL-Direktberechtigte und Cast-Infos\n"
			    + "		        -- keine MWM von GVL-Mitarbeitern\n"
			    + "		                AND ( usr.USR_UGR_ID != 8\n"
			    + "		                      OR usr.USR_UGR_ID IS NULL\n"
			    + "		                    )  and x = 999999\n"
			    + "		        -- Verifikationsstatus darf nicht 9 sein\n"
			    + "		                AND trpv.TRPV_ADDSTATE != 9\n"
			    + "		        -- Musicaldarsteller/-in / Mitglied Tanzensemble ausschließen\n"
			    + "		                AND NOT ( trpv.TRPV_BRO_ID = 4000000026\n"
			    + "		                          AND trpv.TRPV_BFU_ID = 11\n"
			    + "		                        )\n"
			    + "		                AND trpv.TRPV_BRO_ID IS NOT NULL\n"
			    + "		                AND trpv.TRPV_KUE_ID IS NOT NULL\n"
			    + "		        -- AND existiert an einem migrierten AV-Bestandsprodukt\n"
			    + "		        -- Geschäftspartner mit zu ignorierenden Vertragsnummern herausfiltern\n"
			    + "		        -- aber Cast-Informationen nicht unterdrücken\n"
			    + "		                AND ( NOT kue.KUE_KUENR1_PK IN ( '090009', '090232', '090367',\n"
			    + "		                                                 '090989', '090991', '090992',\n"
			    + "		                                                 '090993', '090994', '090995',\n"
			    + "		                                                 '090996', '090997', '091058',\n"
			    + "		                                                 '091059', '099845', '099846',\n"
			    + "		                                                 '099847', '099848', '099849',\n"
			    + "		                                                 '099850', '099851', '099852',\n"
			    + "		                                                 '099853', '099854', '099855',\n"
			    + "		                                                 '099856', '099857', '099858',\n"
			    + "		                                                 '099859', '099860', '099861',\n"
			    + "		                                                 '099862', '099863', '099864',\n"
			    + "		                                                 '099865', '099867', '099868',\n"
			    + "		                                                 '099869', '099870', '099871',\n"
			    + "		                                                 '099872', '099875', '099880',\n"
			    + "		                                                 '099887', '099888', '099889',\n"
			    + "		                                                 '099890', '099891', '099892',\n"
			    + "		                                                 '099893', '099894', '099895',\n"
			    + "		                                                 '099896', '099897', '099898',\n"
			    + "		                                                 '099899', '099900', '099901',\n"
			    + "		                                                 '099902', '099903', '099904',\n"
			    + "		                                                 '099905', '099906', '099907',\n"
			    + "		                                                 '099908', '099909', '099910',\n"
			    + "		                                                 '099911', '099912', '099913',\n"
			    + "		                                                 '099914', '099915', '099916',\n"
			    + "		                                                 '099917', '099918', '099919',\n"
			    + "		                                                 '099920', '099921', '099922',\n"
			    + "		                                                 '099923', '099924', '099925',\n"
			    + "		                                                 '099926', '099927', '099928',\n"
			    + "		                                                 '099929', '099930', '099931',\n"
			    + "		                                                 '099932', '099933', '099934',\n"
			    + "		                                                 '099935', '099936', '099937',\n"
			    + "		                                                 '099938', '099939', '099940',\n"
			    + "		                                                 '099942', '099943', '099944',\n"
			    + "		                                                 '099945', '099946', '099947',\n"
			    + "		                                                 '099948', '099949', '099950',\n"
			    + "		                                                 '099951', '099952', '099953',\n"
			    + "		                                                 '099954', '099955', '099958',\n"
			    + "		                                                 '099959', '099960', '099960',\n"
			    + "		                                                 '099962', '099963', '099964',\n"
			    + "		                                                 '099965', '099966', '099967',\n"
			    + "		                                                 '099968', '099969', '099970',\n"
			    + "		                                                 '099971', '099972', '099973',\n"
			    + "		                                                 '099974', '099975', '099976',\n"
			    + "		                                                 '099977', '099978', '099979',\n"
			    + "		                                                 '099980', '099981', '099982',\n"
			    + "		                                                 '099983', '099984', '099985',\n"
			    + "		                                                 '099986', '099987', '099988',\n"
			    + "		                                                 '099989', '099990', '099991',\n"
			    + "		                                                 '099992', '099993', '099994',\n"
			    + "		                                                 '099995', '099996', '099997',\n"
			    + "		                                                 '099998', '099999' )\n"
			    + "		                    )";
		// we expect an unchanged code!
		String actual = SQLCodeUtil.replaceGlobalMapVars(test);
		assertEquals("The code has been changed!", expected, actual);
	}

}
