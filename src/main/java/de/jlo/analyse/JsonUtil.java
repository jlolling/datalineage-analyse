package de.jlo.analyse;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ParseContext;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;

public class JsonUtil {
	
	private final Map<String, JsonPath> compiledPathMap = new HashMap<String, JsonPath>();
	private static final Configuration JACKSON_JSON_NODE_CONFIGURATION = Configuration
            .builder()
            .mappingProvider(new JacksonMappingProvider())
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .build();
	private ParseContext parseContext = JsonPath.using(JACKSON_JSON_NODE_CONFIGURATION);
	private JsonNode rootNode = null;
	private DocumentContext rootContext = null;

	public JsonUtil(String jsonContent) throws Exception {
		parse(jsonContent);
	}
	
	public JsonUtil() {}
	
	public void parse(String jsonContent) throws Exception {
		if (jsonContent != null && jsonContent.trim().isEmpty() == false) {
			try {
				rootContext = parseContext.parse(jsonContent);
			} catch (Exception pe) {
				throw new Exception("Create json document from content:\n" + jsonContent + "\nfailed: " + pe.getMessage(), pe);
			}
		} else { 
			throw new IllegalArgumentException("Json input content cannot be empty or null");
		}
		rootNode = rootContext.read("$");
		JsonNode testNode = rootContext.read("$");
		if (rootNode != testNode) {
			throw new IllegalStateException("Cloned objects detected! Use the latest Jayway library 2.2.1+");
		}
	}

	public String getNodeValue(String jsonPath) {
		JsonNode valueNode = getNode(jsonPath);
		if (valueNode instanceof ValueNode) {
			return valueNode.textValue();
		} else if (valueNode != null) {
			return valueNode.toString();
		}
		return null;
	}
	
	/**
	 * returns the node start from the root
	 * @param jsonPath
	 * @return node or null if nothing found or a MissingNode was found
	 */
	public JsonNode getNode(String jsonPath) {
		if (jsonPath == null || jsonPath.trim().isEmpty()) {
			throw new IllegalArgumentException("jsonPath cannot be null or empty");
		}
		if (rootContext == null) {
			throw new IllegalStateException("No json document was parsed before");
		}
		try {
			JsonPath compiledPath = getCompiledJsonPath(jsonPath);
			JsonNode node = rootContext.read(compiledPath);
			if (node.isMissingNode() || node.isNull()) {
				return null;
			} else {
				return node;
			}
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	private JsonPath getCompiledJsonPath(String jsonPathStr) {
		JsonPath compiledPath = compiledPathMap.get(jsonPathStr);
		if (compiledPath == null) {
			compiledPath = JsonPath.compile(jsonPathStr);
			compiledPathMap.put(jsonPathStr, compiledPath);
		}
		return compiledPath;
	}
	
	public String getValueAsString(JsonNode valueNode) throws Exception {
		if (valueNode != null) {
			if (valueNode.isMissingNode()) {
				return null;
			}
			if (valueNode.isValueNode()) {
				return valueNode.asText();
			} else {
				return valueNode.toString();
			}
		} else {
			return null;
		}
	}

}
