package de.jlo.analyse.sql;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jlo.analyse.RegexUtil;

public class SQLCodeUtil {
	
	private static Pattern globalMapVarPattern = null;
	
	/**
	 * Replace the globalMap.get("<any-key>") term with the the value 999999
	 * @param javaCode
	 * @return java code without the String concatenation for the globalMap access
	 */
	public static String replaceGlobalMapVars(String javaCode) {
		if (javaCode.contains("globalMap")) {
			if (globalMapVarPattern == null) {
				globalMapVarPattern = Pattern.compile("[\"]{0,1}[\\s]*[+]{0,1}[\\s]*[\\(]{0,1}[\\s]*[\\(]{0,1}[\\s]*(String|Integer|Long|Boolean|Date)[\\s]*\\){0,1}[\\s]*globalMap.get[\\s]*\\([\\s]*\"[a-z0-9\\.-_]{1,}\"[\\s]*[\\)]{0,1}[\\s]*[\\)]{0,1}[\\s]*[+]{0,1}[\\s]*[\"]{0,1}", Pattern.CASE_INSENSITIVE);
			}
			Matcher m = globalMapVarPattern.matcher(javaCode);
			m.reset();
	        boolean result = m.find();
	        if (result) {
	            StringBuffer sb = new StringBuffer();
	            do {
	                m.appendReplacement(sb, "XXX");
	                result = m.find();
	            } while (result);
	            m.appendTail(sb);
	            return sb.toString();
	        } else {
	        	return javaCode;
	        }
		} else {
			return javaCode;
		}
	}

	/**
	 * takes the given java code and extracts the possible SQL code - embedded as String in the java code
	 * @param javaCode
	 * @return the extracted SQL code
	 */
    public static String convertJavaToSqlCode(String javaCode) {
        javaCode = javaCode.replace("\r", "")
        					.replace("\t", " ")
        					.replace("\\n", "");
        final StringBuilder sb = new StringBuilder(javaCode.length());
        // Zeile fuer Zeile verpacken in doppelte Hochkomma
        char c;
        char c1 = ' ';
        boolean inString = false;
        boolean inSqlCode = false;
        boolean masked = false;
        for (int i = 0; i < javaCode.length(); i++) {
            c = javaCode.charAt(i);
            if (i < javaCode.length() - 1) {
                c1 = javaCode.charAt(i + 1);
            }
            if (inSqlCode) {
                // innerhalb des SQL-Codes darf ein Hochkomma nicht als Java-Code interpretiert werden
                if (inString) {
                    if (c == '\'') {
                        inString = false;
                    }
                } else {
                    if (c == '\'') {
                        inString = true;
                    }
                }
                if (c == '\"') {
                    if (!masked) {
                        // nur wenn KEINE Maskierung
                        inSqlCode = false;
                        if (inString) {
                            inString = false;
                        } else {
                            // den Zeilenumbruch nicht innerhalb eines Strings
                            //sb.append('\n');
                        }
                    } else {
                        masked = false;
                        sb.append(c);
                    }
                } else if (c == '\\') {
                    // maskierung gefunden
                    // wenn nachfolgend ein Doppeltes Hochkomma oder Backslash,
                    // dann die maskierung nicht schreiben
                    if (c1 == '\"' || c1 == '\\') {
                        // maskiertes hochkomma gefunden
                        masked = true;
                    } else {
                        masked = false;
                        sb.append(c);
                    }
                } else {
                    masked = false;
                    sb.append(c);
                }
            } else {
                // wenn nicht in SQL-Code dann Zeichen verwerfen
                // und auf Beginn des SQL-Codes testen
                if (c == '\"') {
                    // ok gefunden
                    inSqlCode = true;
                }
            }
        }
        return sb.toString();
    }

	public static String cleanupEnclosures(String name) {
		if (name != null) {
			return name.replace("`", "").replace("[", "").replace("]", "").replace("\"", "");
		} else {
			return null;
		}
	}
	
	public static String cleanupEmptyLines(String text) throws IOException {
		BufferedReader r = new BufferedReader(new StringReader(text));
		String line = null;
		StringBuilder sb = new StringBuilder();
		boolean prevLineNotEmpty = false;
		while ((line = r.readLine()) != null) {
			if (line.trim().isEmpty() == false) {
				sb.append(line);
				sb.append("\n");
				prevLineNotEmpty = true;
			} else {
				if (prevLineNotEmpty) {
					sb.append("\n");
				}
				prevLineNotEmpty = false;
			}
		}
		return sb.toString().trim();
	}
	
	public static String replaceHashCommentsAndAssignments(String text) throws IOException {
		BufferedReader r = new BufferedReader(new StringReader(text));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = r.readLine()) != null) {
			if (line.trim().startsWith("#")) {
				line = line.replace("#", "-- ");
			}
			sb.append(line.replace(":=", "="));
			sb.append("\n");
		}
		return sb.toString().trim();
	}
	
	public static String removeIntoFromSelect(String text) throws IOException {
		BufferedReader r = new BufferedReader(new StringReader(text));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = r.readLine()) != null) {
			line = RegexUtil.replaceByRegexGroups(line, "(into\\s{1,}@[a-z0-9_]{1,}\\s{0,},{0,1}\\s{0,}[@a-z0-9_]{0,}\\s{0,},{0,1}\\s{0,}[@a-z0-9_]{0,})", "");
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString().trim();
	}
	
	public static String removeBraketsAroundNumbers(String text) throws IOException {
		BufferedReader r = new BufferedReader(new StringReader(text));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = r.readLine()) != null) {
			line = RegexUtil.replaceByRegexGroups(line, "(\\()[0-9]{1,}(\\))", "1");
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString().trim();
	}
	
	public static String readContentfromFile(String filePath, String charset) throws Exception {
		if (filePath == null) {
			return null;
		}
		File f = new File(filePath);
		if (f.exists() == false) {
			throw new Exception("File: " + filePath + " does not exist.");
		}
		if (charset == null || charset.trim().isEmpty()) {
			charset = "UTF-8";
		}
		Path p = java.nio.file.Paths.get(filePath);
		byte[] bytes = Files.readAllBytes(p);
		if (bytes != null && bytes.length > 0) {
			return new String(bytes, charset);
		} else {
			return null;
		}
	}

}
