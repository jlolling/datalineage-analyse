package de.jlo.analyse.tableau;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;

public class Utils {
	
	private static final Logger LOG = LogManager.getLogger(Utils.class);
	
    public static Document readFile(File f) throws Exception {
    	try {
    		String fileContent = readFileText(f);
    		if (fileContent.contains("<<<<<") && fileContent.contains("=====") && fileContent.contains(">>>>>")) {
    			LOG.error("File: " + f.getAbsolutePath() + " contains merge conflicts! File will be skipped.");
    			return null;
    		}
    		// cleanup strange tag names
    		fileContent = fileContent.replace("_.fcp.", "").replace(".true...", "-").replace(".false...", "-");  	
    		return DocumentHelper.parseText(fileContent);
    	} catch (Throwable e) {
    		throw new Exception("Read file: " + f.getAbsolutePath() + " failed: " + e.getMessage(), e);
    	}
    }
    
    public static Document readDocument(String filePath) throws Exception {
    	File f = new File(filePath);
    	return readFile(f);
    }
    
    public static String readFileText(File f) throws Exception {
    	if (f.exists() == false) {
    		throw new Exception("File: " + f.getAbsolutePath() + " does not exist");
    	}
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


}
