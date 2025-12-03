package com.noffice.config;

import com.noffice.exception.EncryptionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String encryptedUrl;

    @Value("${spring.datasource.username}")
    private String encryptedUsername;

    @Value("${spring.datasource.password}")
    private String encryptedPassword;

//    private static final String AES_KEY = AppConfig.get("AES_KEY");
//    private static final byte[] KEY_BYTES = AES_KEY.getBytes(StandardCharsets.UTF_8);

    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;

    @Bean
    public DataSource dataSource(@Value("${AES_KEY}") String aesKey) throws EncryptionException {
        if (aesKey == null || aesKey.isEmpty()) {
            throw new EncryptionException("AES_KEY chưa được set trong môi trường container");
        }
        byte[] KEY_BYTES = aesKey.getBytes(StandardCharsets.UTF_8);
        String url = decrypt(encryptedUrl,KEY_BYTES);
        String username = decrypt(encryptedUsername,KEY_BYTES);
        String password = decrypt(encryptedPassword,KEY_BYTES);

        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(url)
                .username(username)
                .password(password)
                .build();
    }

    /**
     * Giải mã bằng AES/GCM/NoPadding – chuẩn bảo mật 2025
     * Chỉ dùng cho dữ liệu được mã hóa bằng hàm encrypt() dưới đây
     */
    public static String decrypt(String encryptedData, byte[] KEY_BYTES) throws EncryptionException {
        if (encryptedData == null || encryptedData.isEmpty()) {
            throw new EncryptionException("Dữ liệu mã hóa không được để trống");
        }

        byte[] data = Base64.getDecoder().decode(encryptedData.trim());

        if (data.length <= GCM_IV_LENGTH) {
            throw new EncryptionException("Dữ liệu mã hóa không hợp lệ (quá ngắn)");
        }

        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(data, 0, iv, 0, GCM_IV_LENGTH);

            byte[] cipherText = new byte[data.length - GCM_IV_LENGTH];
            System.arraycopy(data, GCM_IV_LENGTH, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec keySpec = new SecretKeySpec(KEY_BYTES, "AES");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            byte[] decrypted = cipher.doFinal(cipherText);

            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new EncryptionException("Giải mã thất bại! Kiểm tra AES_KEY hoặc dữ liệu đã bị hỏng.", e);
        }
    }

    public static String encrypt(String plainText, byte[] KEY_BYTES) throws EncryptionException {
        if (plainText == null || plainText.isEmpty()) {
            throw new EncryptionException("Dữ liệu cần mã hóa không được để trống");
        }

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            SecretKeySpec keySpec = new SecretKeySpec(KEY_BYTES, "AES");

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] result = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(cipherText, 0, result, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(result);

        } catch (Exception e) {
            throw new EncryptionException("Mã hóa thất bại!", e);
        }
    }
}