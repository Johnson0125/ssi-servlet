package com.github.ssi_servlet.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SsiServletUtils {

	public static final Locale DEFAULT_LOCALE = Locale.US;
	public static final String DEFAULT_HTML_LANG = DEFAULT_LOCALE.toString()
			.replaceFirst("_", "-");

	public static final List<String> RTL_LANGUAGES = new ArrayList<String>();

	static {
		RTL_LANGUAGES.add("ar");
		RTL_LANGUAGES.add("dv");
		RTL_LANGUAGES.add("fa");
		RTL_LANGUAGES.add("he");
		RTL_LANGUAGES.add("iw");
		RTL_LANGUAGES.add("prs");
		RTL_LANGUAGES.add("ps");
		RTL_LANGUAGES.add("syr");
		RTL_LANGUAGES.add("ug");
		RTL_LANGUAGES.add("ur");
	}

	protected SsiServletUtils() {
		super();
	}

	public static boolean isRightToLeftLanguage(String language) {
		if ((language == null) || (language.trim().length() == 0)) {
			return false;
		}

		return RTL_LANGUAGES.contains(language.trim());
	}

	public static boolean isRightToLeftLanguage(Locale locale) {
		if (locale == null) {
			return false;
		}

		return isRightToLeftLanguage(locale.getLanguage());
	}

	public static String getCultureHTMLLangAttributeValue(String language,
			String country) {
		if ((language == null) || (language.trim().length() == 0)) {
			return DEFAULT_HTML_LANG;
		}

		StringBuilder htmlLang = new StringBuilder(language.trim());

		if ((country != null) && (country.trim().length() > 0)) {
			htmlLang.append("-");
			htmlLang.append(country.trim());
		}

		return htmlLang.toString();
	}

	public static String getCultureHTMLLangAttributeValue(Locale locale) {
		if (locale == null) {
			return DEFAULT_HTML_LANG;
		}

		return getCultureHTMLLangAttributeValue(locale.getLanguage(),
				locale.getCountry());
	}

	public static String getLanguageHTMLDirAttributeValue(String language) {
		if ((language == null) || (language.trim().length() == 0)) {
			return "ltr";
		}

		if (isRightToLeftLanguage(language.trim())) {
			return "rtl";
		}

		return "ltr";
	}

	public static String getLanguageHTMLDirAttributeValue(Locale locale) {
		if (locale == null) {
			return "ltr";
		}

		return getLanguageHTMLDirAttributeValue(locale.getLanguage());
	}

}
