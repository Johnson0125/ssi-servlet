package com.github.ssi_servlet.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.github.ssi_servlet.SsiServletLogger;

public class I18nResourceUtils extends SsiServletUtils {

	protected I18nResourceUtils() {
		super();
	}

	public static Map<String, String> getCultureResourceBundleMap(
			String resBaseName, String language, String country,
			String resPackage, boolean javaClass, boolean debug) {
		if ((resBaseName == null) || (resBaseName.trim().length() == 0)
				|| (language == null) || (language.trim().length() == 0)) {
			return new HashMap<String, String>();
		}

		StringBuilder resBasePath = new StringBuilder();

		if ((resPackage != null) && (resPackage.trim().length() > 0)) {
			resBasePath.append(resPackage.trim());

			if (!resBaseName.trim().startsWith(".")
					&& !resPackage.trim().endsWith(".")) {
				resBasePath.append(".");
			}
		}

		resBasePath.append(resBaseName.trim());
		Locale culture = null;

		if ((country != null) && (country.trim().length() > 0)) {
			culture = new Locale(language.trim(), country.trim());

			if (debug) {
				SsiServletLogger.LOGGER
						.warning("Debug Message: I18nResourceUtils - "
								+ "getCultureResourceBundleMap - is using the resource base '"
								+ resBasePath + "' with the language '"
								+ language.trim() + "' and the country '"
								+ country.trim() + "'.");
			}
		} else {
			culture = new Locale(language.trim());

			if (debug) {
				SsiServletLogger.LOGGER
						.warning("Debug Message: I18nResourceUtils - "
								+ "getCultureResourceBundleMap - is using the resource base '"
								+ resBasePath + "' with the language '"
								+ language.trim() + "'.");
			}
		}

		ResourceBundle bundle = null;

		try {
			if (javaClass) {
				bundle = I18nResourceBundle.getI18nClassBundle(
						resBasePath.toString(), culture);
			} else {
				bundle = I18nResourceBundle.getI18nPropertiesBundle(
						resBasePath.toString(), culture);
			}
		} catch (Exception e) {
			bundle = null;
		}

		if (debug) {
			SsiServletLogger.LOGGER
					.warning("Debug Message: I18nResourceUtils - "
							+ "getCultureResourceBundleMap - the resource bundle "
							+ ((bundle == null) ? "could not be found."
									: "was found and is for locale '"
											+ (((bundle.getLocale() != null) && (bundle
													.getLocale().toString()
													.length() > 0)) ? bundle
													.getLocale().toString()
													: "DEFAULT: "
															+ DEFAULT_LOCALE
																	.toString())
											+ "'."));
		}

		Map<String, String> resMap = null;

		if (bundle instanceof I18nPropertyResourceBundle) {
			resMap = ((I18nPropertyResourceBundle) bundle).getMap();
		} else if (bundle != null) {
			resMap = new HashMap<String, String>();

			for (String key : bundle.keySet()) {
				if (key == null) {
					continue;
				}

				String value = bundle.getString(key);

				if (value == null) {
					continue;
				}

				resMap.put(key, value);
			}
		}

		if (resMap == null) {
			resMap = new HashMap<String, String>();
		}

		if (debug) {
			SsiServletLogger.LOGGER
					.warning("Debug Message: I18nResourceUtils - "
							+ "getCultureResourceBundleMap - the i18n Map: "
							+ resMap);
		}

		return resMap;
	}

	public static Map<String, String> getCultureResourceBundleMap(
			String resBaseName, Locale locale, String resPackage,
			boolean javaClass, boolean debug) {
		if ((resBaseName == null) || (resBaseName.trim().length() == 0)
				|| (locale == null)) {
			return new HashMap<String, String>();
		}

		return getCultureResourceBundleMap(resBaseName, locale.getLanguage(),
				locale.getCountry(), resPackage, javaClass, debug);
	}

}
