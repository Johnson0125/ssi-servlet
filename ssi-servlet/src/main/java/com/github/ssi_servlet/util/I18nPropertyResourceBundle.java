package com.github.ssi_servlet.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class I18nPropertyResourceBundle extends I18nResourceBundle {

	private Properties props;
	private Set<String> keys;

	public I18nPropertyResourceBundle(InputStream stream) throws IOException {
		super();
		props = new I18nProperties();
		props.load(stream);
		setupKeys();
	}

	public I18nPropertyResourceBundle(Reader reader) throws IOException {
		super();
		props = new I18nProperties();
		props.load(reader);
		setupKeys();
	}

	public Map<String, String> getMap() {
		if (keys == null) {
			throw new NullPointerException();
		}

		Map<String, String> map = new HashMap<String, String>();

		for (String key : keys) {
			if (key == null) {
				continue;
			}

			String value = getString(key);

			if (value == null) {
				continue;
			}

			map.put(key, value);
		}

		return map;
	}

	@Override
	public Object handleGetObject(String key) {
		if ((props == null) || (key == null)) {
			throw new NullPointerException();
		}

		return props.get(key);
	}

	@Override
	public Enumeration<String> getKeys() {
		if (keys == null) {
			throw new NullPointerException();
		}

		return Collections.enumeration(keys);
	}

	@Override
	protected Set<String> handleKeySet() {
		if (keys == null) {
			throw new NullPointerException();
		}

		return Collections.unmodifiableSet(keys);
	}

	private void setupKeys() {
		if (props == null) {
			throw new NullPointerException();
		}

		keys = new HashSet<String>();

		for (Object key : props.keySet()) {
			if (key == null) {
				continue;
			}

			keys.add(key.toString());
		}
	}
}
