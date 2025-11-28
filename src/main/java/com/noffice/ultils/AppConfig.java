package com.noffice.ultils;

import java.util.Locale;
import java.util.ResourceBundle;

public class AppConfig {
	private static ResourceBundle defaultBundle;

	// Dùng holder pattern để lazy load + có thể test
	private static class BundleHolder {
		static final ResourceBundle BUNDLE = ResourceBundle.getBundle("application");
	}

	public static String get(String key) {
		if (defaultBundle == null) {
			defaultBundle = BundleHolder.BUNDLE; // chỉ load 1 lần
		}
		return defaultBundle.getString(key);
	}

	public static String get(String key, Locale locale) {
		return ResourceBundle.getBundle("application", locale).getString(key);
	}

	// Thêm method này chỉ để test (package-private hoặc protected)
	static void __resetForTesting() {
		defaultBundle = null;
	}

	// Hoặc tốt hơn: cho phép inject mock từ test
	static void __setBundleForTesting(ResourceBundle bundle) {
		defaultBundle = bundle;
	}
}
