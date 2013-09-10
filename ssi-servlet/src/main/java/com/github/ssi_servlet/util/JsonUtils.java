package com.github.ssi_servlet.util;

import java.util.Map;
import java.util.logging.Level;

import org.codehaus.jackson.map.ObjectMapper;

import com.github.ssi_servlet.SsiServletLogger;

public class JsonUtils {

	public static final String JSON_EMPTY_MAP = "{}";

	protected JsonUtils() {
		super();
	}

	public static String getJsonStringFromMap(Map<String, String> inMap) {
		if ((inMap == null) || (inMap.size() == 0)) {
			return JSON_EMPTY_MAP;
		}

		String json = null;
		ObjectMapper mapper = new ObjectMapper();

		try {
			json = mapper.writeValueAsString(inMap);
		} catch (Exception e) {
			json = null;
			SsiServletLogger.LOGGER.log(
					Level.SEVERE,
					"JsonUtils - getJsonStringFromMap "
							+ "- had the following exception: "
							+ e.getMessage(), e);
		}

		if ((json == null) || (json.length() == 0)) {
			json = JSON_EMPTY_MAP;
		}

		return json;
	}

}
