package com.github.ssi_servlet.util;

import java.util.Locale;
import java.util.ResourceBundle;

public abstract class I18nResourceBundle extends ResourceBundle {

	public static final ResourceBundle getI18nBundle(String baseName) {
		return ResourceBundle.getBundle(baseName, Locale.getDefault(),
				I18nResourceBundleControl.INSTANCE);
	}

	public static final ResourceBundle getI18nClassBundle(String baseName) {
		return ResourceBundle.getBundle(baseName, Locale.getDefault(),
				I18nResourceBundleControl.getI18nControl(Control.FORMAT_CLASS));
	}

	public static final ResourceBundle getI18nPropertiesBundle(String baseName) {
		return ResourceBundle.getBundle(baseName, Locale.getDefault(),
				I18nResourceBundleControl
						.getI18nControl(Control.FORMAT_PROPERTIES));
	}

	public static final ResourceBundle getI18nBundle(String baseName,
			Control control) {
		return ResourceBundle.getBundle(baseName, Locale.getDefault(), control);
	}

	public static final ResourceBundle getI18nBundle(String baseName,
			Locale locale) {
		return ResourceBundle.getBundle(baseName, locale,
				I18nResourceBundleControl.INSTANCE);
	}

	public static final ResourceBundle getI18nClassBundle(String baseName,
			Locale locale) {
		return ResourceBundle.getBundle(baseName, locale,
				I18nResourceBundleControl.getI18nControl(Control.FORMAT_CLASS));
	}

	public static final ResourceBundle getI18nPropertiesBundle(String baseName,
			Locale locale) {
		return ResourceBundle.getBundle(baseName, locale,
				I18nResourceBundleControl
						.getI18nControl(Control.FORMAT_PROPERTIES));
	}

	public static final ResourceBundle getI18nBundle(String baseName,
			Locale targetLocale, Control control) {
		return ResourceBundle.getBundle(baseName, targetLocale, control);
	}

	public static ResourceBundle getI18nBundle(String baseName, Locale locale,
			ClassLoader loader) {
		if (loader == null) {
			throw new NullPointerException();
		}

		return ResourceBundle.getBundle(baseName, locale, loader,
				I18nResourceBundleControl.INSTANCE);
	}

	public static ResourceBundle getI18nClassBundle(String baseName,
			Locale locale, ClassLoader loader) {
		if (loader == null) {
			throw new NullPointerException();
		}

		return ResourceBundle.getBundle(baseName, locale, loader,
				I18nResourceBundleControl.getI18nControl(Control.FORMAT_CLASS));
	}

	public static ResourceBundle getI18nPropertiesBundle(String baseName,
			Locale locale, ClassLoader loader) {
		if (loader == null) {
			throw new NullPointerException();
		}

		return ResourceBundle.getBundle(baseName, locale, loader,
				I18nResourceBundleControl
						.getI18nControl(Control.FORMAT_PROPERTIES));
	}

	public static ResourceBundle getI18nBundle(String baseName,
			Locale targetLocale, ClassLoader loader, Control control) {
		if (loader == null || control == null) {
			throw new NullPointerException();
		}

		return ResourceBundle
				.getBundle(baseName, targetLocale, loader, control);
	}

}
