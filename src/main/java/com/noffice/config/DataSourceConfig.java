package com.noffice.config;

import com.noffice.ultils.AppConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.sql.DataSource;
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
    public static String encrypt(String data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decrypt(String encryptedData) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        return new String(cipher.doFinal(decoded));
    }
}
