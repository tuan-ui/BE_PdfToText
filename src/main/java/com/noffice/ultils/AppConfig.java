package com.noffice.ultils;

import java.util.Locale;
import java.util.ResourceBundle;

public class AppConfig {
	private static final ResourceBundle defaultBundle = ResourceBundle.getBundle("application");

	public static String get(String key) {
		return defaultBundle.getString(key);
	}

	public static String get(String key, Locale locale) {
		return ResourceBundle.getBundle("application", locale).getString(key);
	}
}
