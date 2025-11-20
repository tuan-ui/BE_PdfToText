package com.noffice.config;

import com.noffice.exception.EncryptionException;
import com.noffice.ultils.AppConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class DataSourceConfig {
    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    private static String AES_KEY= AppConfig.get("AES_KEY");
    private static final byte[] KEY_BYTES = AES_KEY.getBytes(StandardCharsets.UTF_8);
    @Bean
    public DataSource dataSource() {
        try {
            url=decrypt(url);
            username=decrypt(username);
            password=decrypt(password);
        } catch (Exception e) {
            System.out.println("Lỗi connect database");
        }

        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
    public static String decrypt(String encryptedData) throws EncryptionException {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }

        byte[] data = Base64.getDecoder().decode(encryptedData);

        // Ưu tiên thử AES/GCM (dữ liệu mới)
        if (data.length > 12) {
            try {
                byte[] iv = new byte[12];
                System.arraycopy(data, 0, iv, 0, 12);

                byte[] cipherText = new byte[data.length - 12];
                System.arraycopy(data, 12, cipherText, 0, cipherText.length);

                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                GCMParameterSpec spec = new GCMParameterSpec(128, iv);
                SecretKeySpec keySpec = new SecretKeySpec(KEY_BYTES, "AES");

                cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
                byte[] decrypted = cipher.doFinal(cipherText);
                return new String(decrypted, StandardCharsets.UTF_8);

            } catch (Exception e) {
                System.out.println("GCM failed, trying legacy ECB mode...");
            }
        }

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec keySpec = new SecretKeySpec(KEY_BYTES, "AES");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);

            byte[] decrypted = cipher.doFinal(data);
            return new String(decrypted, StandardCharsets.UTF_8).trim();

        } catch (Exception e) {
            // Bắt hết lỗi và ném ra exception riêng của mình
            throw new EncryptionException("Không thể giải mã dữ liệu. Dữ liệu có thể bị hỏng hoặc key sai.", e);
        }
    }

    public static String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        byte[] iv = new byte[12];
        new java.security.SecureRandom().nextBytes(iv);

        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        SecretKeySpec keySpec = new SecretKeySpec(KEY_BYTES, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);

        byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        byte[] result = new byte[iv.length + cipherText.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(cipherText, 0, result, iv.length, cipherText.length);

        return Base64.getEncoder().encodeToString(result);
    }
}
