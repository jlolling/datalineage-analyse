package de.jlo.analyse.talend;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Project {
	
	private static final Logger LOG = LogManager.getLogger(Project.class);
	private Map<String, List<Job>> mapNameJobs = new HashMap<>();
	private Map<String, DatabaseConnection> mapIdConnections = new HashMap<>();
	private List<Job> listAllJobs = new ArrayList<>();
	private String projectRootDir = null;
	private String processFolderPath = null;
	private static Map<String, Project> projectCache = new HashMap<String, Project>();
	
	public Project(String projectRootFolderPath) throws Exception {
		readProject(projectRootFolderPath);
	}
	
	public Project() {}
	
	public static Project getProject(String projectRootFolderPath) throws Exception {
		Project p = projectCache.get(projectRootFolderPath);
		if (p == null) {
			p = new Project(projectRootFolderPath);
			projectCache.put(projectRootFolderPath, p);
		}
		return p;
	}
	
	/**
	 * Reads all Talend jobs and fills the job map
	 * @param rootDir points to project folder written the in capital letters 
	 * @return number jobs read
	 * @throws Exception
	 */
	public int readProject(String rootDir) throws Exception {
    	LOG.info("Start read jobs and connections from project root: " + rootDir);
    	projectRootDir = rootDir;
		File processFolder = new File(rootDir, "process");
		processFolderPath = processFolder.getAbsolutePath();
		registerJobs(processFolder);
    	LOG.info("Finished read " + listAllJobs.size() + " jobs from project root: " + rootDir);
    	LOG.info("Read database connections...");
    	File metadataConnectionFolder = new File(rootDir, "metadata/connections"); 
    	readDatabaseItemFiles(metadataConnectionFolder);
    	LOG.info("Finished read " + mapIdConnections.size() + " connections from project root: " + rootDir);
		return listAllJobs.size();
	}

	private void registerJob(Job job) throws Exception {
		if (job != null) {
			listAllJobs.add(job);
			List<Job> list = mapNameJobs.get(job.getJobName());
			if (list == null) {
				list = new ArrayList<Job>();
				mapNameJobs.put(job.getJobName(), list);
			}
			if (list.contains(job) == false) {
				list.add(job);
			}
		}
	}
	
	public Job getJobByVersion(String jobName, String version) {
		if (version == null || version.equals("Latest")) {
			return getLatestJob(jobName);
		} else {
			List<Job> list = mapNameJobs.get(jobName);
			if (list != null && list.isEmpty() == false) {
				for (Job job : list) {
					if (job.getVersion().equals(version)) {
						return job;
					}
				}
			}
			return null;
		}
	}
	
    public Job getLatestJob(String jobName) {
		List<Job> list = mapNameJobs.get(jobName);
		if (list != null && list.isEmpty() == false) {
			Collections.sort(list);
			// after sort, the latest is the first element
			return list.get(0);
		} else {
			return null;
		}
	}
    
    public List<Job> getAllJobs() {
    	return listAllJobs;
    }
    
    public List<Job> getAllLatestJobs() {
    	return getJobs(null, true);
    }
    
    public List<Job> getJobs(String jobNamePattern) {
    	return getJobs(jobNamePattern, false);
    }

    public Job getJobById(String id) {
    	if (id == null || id.trim().isEmpty()) {
    		throw new IllegalArgumentException("id cannot be null or empty");
    	}
    	for (Job job : listAllJobs) {
    		if (id.equals(job.getId())) {
    			return job;
    		}
    	}
    	return null;
    }
    
    public List<Job> getJobs(String jobNamePattern, boolean onlyLatestVersion) {
    	List<Job> list = new ArrayList<Job>();
    	Set<String> uniqueJobNames = new HashSet<>();
    	Pattern pattern = null;
    	if (jobNamePattern != null && jobNamePattern.trim().isEmpty() == false) {
        	pattern = Pattern.compile(jobNamePattern, Pattern.CASE_INSENSITIVE);
    	}
    	for (Job job : listAllJobs) {
    		if (pattern != null) {
        		Matcher m = pattern.matcher(job.getJobName());
        		if (m.find()) {
            		if (onlyLatestVersion) {
            			if (uniqueJobNames.contains(job.getJobName())) {
            				continue;
            			} else {
            				uniqueJobNames.add(job.getJobName());
            				job = getLatestJob(job.getJobName());
            			}
            		}
        			list.add(job);
        		}
    		} else {
        		if (onlyLatestVersion) {
        			if (uniqueJobNames.contains(job.getJobName())) {
        				continue;
        			} else {
        				uniqueJobNames.add(job.getJobName());
        				job = getLatestJob(job.getJobName());
        			}
        		}
    			list.add(job);
    		}
    	}
    	return list;
    }

    private void registerJobs(File folder) throws Exception {
        File[] list = folder.listFiles();
        if (list == null) {
        	return;
        }
        for (File f : list) {
            if (f.isDirectory()) {
            	registerJobs(f);
            	LOG.debug("Read properties in: " + f.getAbsoluteFile());
            } else if (f.getName().endsWith(".properties")) {
            	try {
					Job job = readTalendJobFromProperties(f);
					if (job != null) {
						registerJob(job);
					}
				} catch (Exception e) {
					LOG.error("Failed to read properties file: " + f.getAbsolutePath(), e);
					throw new Exception("Failed to read properties file: " + f.getAbsolutePath(), e);
				}
            }
        }
    }
    
    private void readDatabaseItemFiles(File metadataConnectionFolder) throws Exception {
    	LOG.debug("Read database connection item files from folder: " + metadataConnectionFolder.getAbsolutePath());
        File[] list = metadataConnectionFolder.listFiles();
        if (list == null) {
        	return;
        }
        for (File f : list) {
            if (f.isDirectory()) {
            	readDatabaseItemFiles(f);
            	LOG.debug("Read jobs in: " + f.getAbsoluteFile());
            } else if (f.getName().endsWith(".item")) {
            	try {
					DatabaseConnection conn = readDatabaseConnectionFromFile(f);
					if (conn != null) {
						mapIdConnections.put(conn.getId(), conn);
					}
				} catch (Exception e) {
					LOG.error("Failed to read item file: " + f.getAbsolutePath(), e);
					throw new Exception("Failed to read item file: " + f.getAbsolutePath(), e);
				}
            }
        }
    }
    
    public DatabaseConnection getDatabaseConnectionById(String id) {
    	return mapIdConnections.get(id);
    }
    
    public List<DatabaseConnection> getDatabaseConnections() {
    	List<DatabaseConnection> list = new ArrayList<>();
    	for (DatabaseConnection c : mapIdConnections.values()) {
    		list.add(c);
    	}
    	return list;
    }

    public Document readItem(Job job) throws Exception {
    	String filePath = job.getPathWithoutExtension() + ".item";
    	return readFile(new File(filePath));
    }
    
    private DatabaseConnection readDatabaseConnectionFromFile(File itemFile) throws Exception {
    	Document itemDoc = readFile(itemFile);
    	Element databaseConnectionNode = (Element) itemDoc.selectSingleNode("/TalendMetadata:DatabaseConnection");
    	if (databaseConnectionNode != null) {
        	return new DatabaseConnection(databaseConnectionNode);
    	} else {
    		return null;
    	}
    }
    
    public Job readTalendJobFromProperties(File propertiesFile) throws Exception {
    	Document propDoc = readFile(propertiesFile);
    	if (propDoc != null) {
        	Job job = new Job(this);
        	Element propertyNode = (Element) propDoc.selectSingleNode("/xmi:XMI/TalendProperties:Property");
        	QName nameId = new QName("id", null);
        	job.setId(propertyNode.attributeValue(nameId));
        	job.setJobName(propertyNode.attributeValue("label"));
        	job.setPath(propertiesFile.getAbsolutePath());
        	job.setVersion(propertyNode.attributeValue("version"));
        	String folder = propertiesFile.getParentFile().getAbsolutePath().replace(processFolderPath, "");
        	job.setJobFolder(folder);
        	if (LOG.isDebugEnabled()) {
            	LOG.debug("Read Talend job properties from file: " + propertiesFile.getAbsolutePath() + ". Id=" + job.getId());
        	}
        	return job;
    	} else {
    		return null;
    	}
    }

    public static Document readFile(File f) throws Exception {
    	try {
    		String fileContent = readFileText(f);
    		if (fileContent.contains("<<<<<") && fileContent.contains("=====") && fileContent.contains(">>>>>")) {
    			LOG.error("File: " + f.getAbsolutePath() + " contains merge conflicts! File will be skipped.");
    			return null;
    		}
        	return DocumentHelper.parseText(fileContent);
    	} catch (Throwable e) {
    		throw new Exception("Read file: " + f.getAbsolutePath() + " failed: " + e.getMessage(), e);
    	}
    }
    
    public static String readFileText(File f) throws Exception {
    	try {
    		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
    		String line = null;
    		StringBuilder sb = new StringBuilder();
    		while ((line = reader.readLine()) != null) {
    			sb.append(line.trim());
    			sb.append('\n');
    		}
    		reader.close();
        	return sb.toString();
    	} catch (Exception e) {
    		throw new Exception("Read file: " + f.getAbsolutePath() + " failed: " + e.getMessage(), e);
    	}
    }

	public String getProjectRootDir() {
		return projectRootDir;
	}
	
	public int getCountJobs() {
		return listAllJobs.size();
	}

}
