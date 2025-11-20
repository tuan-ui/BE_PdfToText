package com.noffice.ultils;

import java.util.regex.Pattern;

public class UUIDUtil {
        private static final Pattern UUID_PATTERN = Pattern.compile(
                "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$"
        );
    public static boolean isUUID(String str) {
        return str != null && UUID_PATTERN.matcher(str).matches();
    }

}
