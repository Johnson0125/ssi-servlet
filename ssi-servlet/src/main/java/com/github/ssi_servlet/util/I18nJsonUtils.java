package com.github.ssi_servlet.util;

import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

import org.codehaus.jackson.map.ObjectMapper;

import com.github.ssi_servlet.SsiServletLogger;

public class I18nJsonUtils extends JsonUtils {

	public static final String I18N_JSON_PROPERTY = "i18n";
	public static final String I18N_JSON_EMPTY_MAP = "{\"" + I18N_JSON_PROPERTY
			+ "\":{}}";

	protected I18nJsonUtils() {
		super();
	}

	public static String getI18nJsonStringFromResourceBundleMap(
			Map<String, String> resBundleMap, boolean debug) {
		if ((resBundleMap == null) || (resBundleMap.size() == 0)) {
			return I18N_JSON_EMPTY_MAP;
		}

		String json = null;
		I18nJsonBean i18nJSON = new I18nJsonBean();
		i18nJSON.setI18n(resBundleMap);
		ObjectMapper mapper = new ObjectMapper();

		try {
			json = mapper.writeValueAsString(i18nJSON);
		} catch (Exception e) {
			json = null;
			SsiServletLogger.LOGGER.log(
					Level.SEVERE,
					"I18nJsonUtils - getI18nJSONFromResourceBundleMap "
							+ "- had the following exception: "
							+ e.getMessage(), e);
		}

		if ((json == null) || (json.length() == 0)) {
			json = I18N_JSON_EMPTY_MAP;
		}

		if (debug) {
			SsiServletLogger.LOGGER.warning("Debug Message: I18nJsonUtils - "
					+ "getI18nJSONFromResourceBundleMap - the i18n JSON: "
					+ json);
		}

		return json.replaceAll("\\\\u", "\\u");
	}

	public static String getI18nJsonStringFromCultureResourceBundle(
			String resBaseName, String language, String country,
			String resPackage, boolean javaClass, boolean debug) {
		return getI18nJsonStringFromResourceBundleMap(
				I18nResourceUtils.getCultureResourceBundleMap(resBaseName,
						language, country, resPackage, javaClass, debug), debug);
	}

	public static String getI18nJsonStringFromCultureResourceBundle(
			String resBaseName, Locale locale, String resPackage,
			boolean javaClass, boolean debug) {
		return getI18nJsonStringFromResourceBundleMap(
				I18nResourceUtils.getCultureResourceBundleMap(resBaseName,
						locale, resPackage, javaClass, debug), debug);
	}

}
