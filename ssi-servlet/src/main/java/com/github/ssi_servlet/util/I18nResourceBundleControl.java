package com.github.ssi_servlet.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

public class I18nResourceBundleControl extends Control {

	public static final Control INSTANCE = new I18nResourceBundleControl();

	public static final Control getI18nControl(List<String> formats) {
		if (formats.equals(Control.FORMAT_PROPERTIES)) {
			return I18nSingleFormatControl.PROPERTIES_ONLY;
		}

		if (formats.equals(Control.FORMAT_CLASS)) {
			return I18nSingleFormatControl.CLASS_ONLY;
		}

		if (formats.equals(Control.FORMAT_DEFAULT)) {
			return I18nResourceBundleControl.INSTANCE;
		}

		throw new IllegalArgumentException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public ResourceBundle newBundle(String baseName, Locale locale,
			String format, ClassLoader loader, boolean reload)
			throws IllegalAccessException, InstantiationException, IOException {
		String bundleName = toBundleName(baseName, locale);
		ResourceBundle bundle = null;

		if (format.equals("java.class")) {
			try {
				Class<? extends ResourceBundle> bundleClass = (Class<? extends ResourceBundle>) loader
						.loadClass(bundleName);

				// If the class isn't a ResourceBundle subclass, throw a
				// ClassCastException.
				if (ResourceBundle.class.isAssignableFrom(bundleClass)) {
					bundle = bundleClass.newInstance();
				} else {
					throw new ClassCastException(bundleClass.getName()
							+ " cannot be cast to ResourceBundle");
				}
			} catch (ClassNotFoundException e) {
			}
		} else if (format.equals("java.properties")) {
			final String resourceName = toResourceName(bundleName, "properties");
			final ClassLoader classLoader = loader;
			final boolean reloadFlag = reload;
			InputStream stream = null;

			try {
				stream = AccessController
						.doPrivileged(new PrivilegedExceptionAction<InputStream>() {
							public InputStream run() throws IOException {
								InputStream is = null;

								if (reloadFlag) {
									URL url = classLoader
											.getResource(resourceName);

									if (url != null) {
										URLConnection connection = url
												.openConnection();
										if (connection != null) {
											// Disable caches to get fresh data
											// for
											// reloading.
											connection.setUseCaches(false);
											is = connection.getInputStream();
										}
									}
								} else {
									is = classLoader
											.getResourceAsStream(resourceName);
								}

								return is;
							}
						});
			} catch (PrivilegedActionException e) {
				throw (IOException) e.getException();
			}

			if (stream != null) {
				try {
					bundle = new I18nPropertyResourceBundle(stream);
				} finally {
					stream.close();
				}
			}
		} else {
			throw new IllegalArgumentException("unknown format: " + format);
		}

		return bundle;
	}

	public static class I18nSingleFormatControl extends
			I18nResourceBundleControl {
		public static final Control PROPERTIES_ONLY = new I18nSingleFormatControl(
				FORMAT_PROPERTIES);

		public static final Control CLASS_ONLY = new I18nSingleFormatControl(
				FORMAT_CLASS);

		private final List<String> formats;

		public I18nSingleFormatControl(List<String> formats) {
			this.formats = formats;
		}

		public List<String> getFormats(String baseName) {
			if (baseName == null) {
				throw new NullPointerException();
			}

			return formats;
		}
	}
}
