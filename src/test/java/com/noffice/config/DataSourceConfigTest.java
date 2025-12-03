package com.noffice.config;

import com.noffice.exception.EncryptionException;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class DataSourceConfigTest {

    private DataSourceConfig config;
    private byte[] KEY_BYTES;

    @BeforeEach
    void setup() {
        config = new DataSourceConfig();
        KEY_BYTES = "1234567890123456".getBytes(StandardCharsets.UTF_8);
    }

    @Test
    void testDataSource_ShouldReturnDecryptedValues() throws Exception {
        // Plain values
        String url = "jdbc:postgresql://localhost:5432/mydb";
        String username = "dbuser";
        String password = "dbpass";

        // Encrypt before injecting
        String encUrl = DataSourceConfig.encrypt(url, KEY_BYTES);
        String encUser = DataSourceConfig.encrypt(username, KEY_BYTES);
        String encPass = DataSourceConfig.encrypt(password, KEY_BYTES);

        // Inject vào private fields
        ReflectionTestUtils.setField(config, "encryptedUrl", encUrl);
        ReflectionTestUtils.setField(config, "encryptedUsername", encUser);
        ReflectionTestUtils.setField(config, "encryptedPassword", encPass);

        // Run method
        DataSource ds = config.dataSource("1234567890123456");

        assertNotNull(ds);

        HikariDataSource hikari = (HikariDataSource) ds;

        assertEquals(url, hikari.getJdbcUrl());
        assertEquals(username, hikari.getUsername());
        assertEquals(password, hikari.getPassword());

    }

    @Test
    void testDataSource_MissingAESKey_ShouldThrow() {
        assertThrows(EncryptionException.class, () -> config.dataSource(""));
    }

    @Test
    void testDecrypt_DataNull_ShouldThrow() {
        assertThrows(EncryptionException.class, () -> DataSourceConfig.decrypt("", KEY_BYTES));
    }

    @Test
    void testDecrypt_ShortData_ShouldThrow() {
        assertThrows(EncryptionException.class, () -> DataSourceConfig.decrypt("123123", KEY_BYTES));
    }
    @Test
    void testEncrypt_DataNull_ShouldThrow() {
        assertThrows(EncryptionException.class, () -> DataSourceConfig.encrypt("", KEY_BYTES));
    }
}
