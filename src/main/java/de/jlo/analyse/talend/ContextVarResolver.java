package de.jlo.analyse.talend;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContextVarResolver {
	
	private List<ContextParameter> context = null;
	private String contextVarRegex = "[\"]{0,1}[\\s]*[+]{0,1}[\\s]*context\\.([a-z0-9\\_]{1,})[\\s]*[+]{0,1}[\\s]*[\"]{0,1}";
	private String jobNameRegex = "[\"]{0,1}[\\s]*[+]{0,1}[\\s]*(jobName)[\\s]*[+]{0,1}[\\s]*[\"]{0,1}";
	private Pattern contextVarPattern = null;
	private Pattern jobNamePattern = null;
	private String jobName = null;
	public static final String PLACEHOLDER = "PLACEHOLDER";
	
	public ContextVarResolver() {
		contextVarPattern = Pattern.compile(contextVarRegex, Pattern.CASE_INSENSITIVE);
		jobNamePattern = Pattern.compile(jobNameRegex);
	}
	
	public void setContext(List<ContextParameter> context) {
		this.context = context;
	}
	
	private ContextParameter getContextParameter(String name) {
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("name cannot be null or empty");
		}
		if (context != null) {
			for (ContextParameter cp : this.context) {
				if (name.equals(cp.getName())) {
					return cp;
				}
			}
		}
		return null;
	}
	
	public void setContextParameter(String name, String value, String delimiter) {
		if (context == null) {
			context = new ArrayList<ContextParameter>();
		}
		ContextParameter p = new ContextParameter(name);
		p.setValue(value);
		p.setDelimiter(delimiter);
		if (context.contains(p)) {
			context.remove(p);
		}
		context.add(p);
	}
	
	public void clear() {
		context.clear();
	}

	private String getVariableValue(String name) {
		ContextParameter p = getContextParameter(name);
		if (p != null) {
			return p.getValue();
		}
		return PLACEHOLDER;
	}
	
	public String replace(String code) throws Exception {
		String s = replaceJobName(code);
		return replaceContextVars(s);
	}
	
	public static class TextPositionToReplace {
		int start = -1;
		int end = -1;
		String varName = null;
		String currentValue = null;
		@Override
		public String toString() {
			return varName + " at " + start + ":" + end;
		}
	}
	
	protected List<TextPositionToReplace> collectContextTextLocationToReplace(String code) {
		List<TextPositionToReplace> list = new ArrayList<TextPositionToReplace>();
		Matcher matcher = contextVarPattern.matcher(code);
		int lastEnd = 0;
		while (matcher.find()) {
			int startCode = matcher.start();
			if (matcher.groupCount() > 0) {
				lastEnd = matcher.end();
				String contextVarName = matcher.group(1);
				TextPositionToReplace tlr = new TextPositionToReplace();
				tlr.start = startCode;
				tlr.end = lastEnd;
				tlr.varName = contextVarName;
				list.add(tlr);
			}
		}
		return list;
	}
	
	protected String replaceContextVars(String code) {
		// first collect the positions to replace
		List<TextPositionToReplace> listPositions = collectContextTextLocationToReplace(code);
		return replaceContextVars(code, listPositions);
	}
	
	protected String replaceContextVars(String code, List<TextPositionToReplace> listPositions) {
		if (listPositions == null || listPositions.size() == 0) {
			return code;
		}
		StringBuilder result = new StringBuilder();
		int lastEnd = 0;
		String gap = null;
		TextPositionToReplace lastTp = null;
		for (TextPositionToReplace tp : listPositions) {
			gap = code.substring(lastEnd, tp.start);
			result.append(gap); // fill the gap between variables
			lastEnd = tp.end;
			result.append(getVariableValue(tp.varName));
			lastTp = tp;
		}
		if (lastTp != null && lastTp.end < code.length()) {
			gap = code.substring(lastTp.end);
			result.append(gap);
		}
		return result.toString();
	}

	public List<ContextParameter> collectContextVariables(String code) throws Exception {
		// collect context vars inside the script
		Matcher matcher = contextVarPattern.matcher(code);
		List<ContextParameter> names = new ArrayList<ContextParameter>();
		while (matcher.find()) {
			if (matcher.groupCount() > 0) {
				// copy the SQL code until the context-code
				String contextVarName = matcher.group(1);
				ContextParameter cp = getContextParameter(contextVarName);
				if (cp != null && names.contains(cp) == false) {
					names.add(cp);
				}
			}
		}
		return names;
	}

	public String replaceJobName(String code) throws Exception {
		if (jobName == null || jobName.trim().isEmpty()) {
			return code;
		}
		StringBuilder result = new StringBuilder();
		Matcher matcher = jobNamePattern.matcher(code);
		int lastEnd = 0;
		while (matcher.find()) {
			int startCode = matcher.start();
			if (matcher.groupCount() > 0) {
				// copy the SQL code until the context-code
				result.append(code.substring(lastEnd, startCode));
				lastEnd = matcher.end();
				// now add the context variable value
				String contextVarValue = jobName;
				result.append(contextVarValue);
			}
		}
		result.append(code.substring(lastEnd));
		return result.toString();
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
}
