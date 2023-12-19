package de.jlo.talend.model;

import java.util.List;
import java.util.Map;

import de.jlo.talend.model.Model;
import de.jlo.talend.model.parser.sql.TaskJobDatabaseTableCollector;
import de.jlo.talend.model.Job;


public class PlayTalendModel {

	public static void main(String[] args) {
		//playCollectTablesInOneJob();
		playCollectEmbeddedJobs();
	}

	public static void playCollectTablesInAllJobs() {
		Model model = new Model();
		try {
			model.readProject("/Users/jan/development/tos-workspace/GVL_BEAT17");
			TaskJobDatabaseTableCollector c = new TaskJobDatabaseTableCollector(model);
			c.setPreferSQLParser(false);
			c.search(null, false);
			Map<Job, List<String>> inputTables = c.getInputTables();
			for (Map.Entry<Job, List<String>> entry : inputTables.entrySet()) {
				System.out.println(entry.getKey());
				for (String t : entry.getValue()) {
					System.out.println("   " + t);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void playCollectTablesInOneJob() {
		Model model = new Model();
		try {
			model.readProject("/Users/jan/development/tos-workspace/GVL_CORE_API");
			TaskJobDatabaseTableCollector c = new TaskJobDatabaseTableCollector(model);
			c.setPreferSQLParser(false);
			Job j = model.getJobByVersion("core_businessobjects_get", null);
			c.findTables(j);
			Map<String, Map<String, List<String>>> inputTablesPerComp = c.getInputTablesPerComponent();
			for (Map.Entry<String, Map<String, List<String>>> entryJob : inputTablesPerComp.entrySet()) {
				String jobName = entryJob.getKey();
				System.out.println("############## " + jobName);
				Map<String, List<String>> compTableMap = entryJob.getValue();
				for (Map.Entry<String, List<String>> entryComp : compTableMap.entrySet()) {
					String component = entryComp.getKey();
					System.out.println("\t" + component);
					List<String> tables = entryComp.getValue();
					for (String table : tables) {
						System.out.println("\t\t" + table);
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void playCollectEmbeddedJobs() {
		Model model = new Model();
		try {
			model.readProject("/Users/jan/development/tos-workspace/GVL_CORE_API");
			Job job = model.getLatestJob(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
