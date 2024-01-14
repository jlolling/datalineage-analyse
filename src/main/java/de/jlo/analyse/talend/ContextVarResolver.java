package de.jlo.analyse.talend;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContextVarResolver {
	
	private Properties contextVars = new Properties();
	private String contextVarRegex = "[\"]{0,1}[\\s]*[+]{0,1}[\\s]*context\\.([a-z0-9\\_]{1,})[\\s]*[+]{0,1}[\\s]*[\"]{0,1}";
	private Pattern contextVarPattern = null;
	
	public ContextVarResolver() {
		contextVarPattern = Pattern.compile(contextVarRegex, Pattern.CASE_INSENSITIVE);
	}
	
	public void setContext(List<ContextParameter> context) {
		for (ContextParameter p : context) {
			addContextVar(p.getName(), p.getValue());
		}
	}
	
	public void addContextVar(String name, String value) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("name cannot be null or empty");
		}
		if (value != null && value.trim().isEmpty() == false) {
			contextVars.put(name, value);
		}
	}
	
	public void clear() {
		contextVars.clear();
	}

	public void initContextVars(String contextFilePath) throws IOException {
		File contextFile = new File(contextFilePath);
		if (contextFile.exists() == false) {
			throw new IOException("Context file: " + contextFile.getAbsolutePath() + " does not exists!");
		}
		InputStream in = new FileInputStream(contextFile);
		contextVars.load(in);
	}
	
	public String getVariableValue(String name) {
		if (name.startsWith("context.")) {
			name = name.substring("context.".length());
		}
		return contextVars.getProperty(name);
	}
	
	public void readContextVars(String code) {
		Matcher matcher = contextVarPattern.matcher(code);
		while (matcher.find()) {
			if (matcher.groupCount() > 0) {
				String contextVarName = matcher.group(1);
				if (contextVars.containsKey(contextVarName) == false) {
					contextVars.put(contextVarName, "");
				}
			}
		}
	}
	
	public String replaceContextVars(String code) throws Exception {
		StringBuilder result = new StringBuilder();
		Matcher matcher = contextVarPattern.matcher(code);
		int lastEnd = 0;
		while (matcher.find()) {
			int startCode = matcher.start();
			if (matcher.groupCount() > 0) {
				// copy the SQL code until the context-code
				result.append(code.substring(lastEnd, startCode));
				lastEnd = matcher.end();
				// now add the context variable value
				String contextVarName = matcher.group(1);
				String contextVarValue = contextVars.getProperty(contextVarName, contextVarName.replace("context.", "context_"));
				result.append(contextVarValue);
			}
		}
		result.append(code.substring(lastEnd));
		return result.toString();
	}

	public Properties getContextVars() {
		return contextVars;
	}
	
}
