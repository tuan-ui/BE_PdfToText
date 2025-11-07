package com.noffice.ultils;
import java.security.SecureRandom;

public class PasswordGenerator {
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}|;:,.<>?";

    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;
    private static final SecureRandom random = new SecureRandom();

    public static String generate(int length) {
        if (length < 6) throw new IllegalArgumentException("Password length should be at least 6 characters");
        StringBuilder password = new StringBuilder(length);

        // Đảm bảo có ít nhất 1 ký tự mỗi loại
        password.append(getRandomChar(UPPER));
        password.append(getRandomChar(LOWER));
        password.append(getRandomChar(DIGITS));
        password.append(getRandomChar(SPECIAL));

        // Phần còn lại chọn ngẫu nhiên
        for (int i = 4; i < length; i++) {
            password.append(getRandomChar(ALL));
        }

        // Xáo trộn chuỗi kết quả
        return shuffleString(password.toString());
    }

    private static char getRandomChar(String chars) {
        return chars.charAt(random.nextInt(chars.length()));
    }

    private static String shuffleString(String input) {
        char[] a = input.toCharArray();
        for (int i = a.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = a[i];
            a[i] = a[j];
            a[j] = tmp;
        }
        return new String(a);
    }

    // Test
    public static void main(String[] args) {
        String password = generate(12);
        System.out.println("Random Password: " + password);
    }
}
