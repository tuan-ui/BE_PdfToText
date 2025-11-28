package com.noffice.ultils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppConfigTest {

    private static final String TEST_KEY = "save_path";
    private static final String TEST_VALUE_EN = "/var/files";
    private static final String TEST_VALUE_VI = "/duong/dan/tep";

    @Test
    void get_WithKeyAndLocale_ShouldReturnValueFromLocalizedBundle() {
        Locale vietnamese = new Locale("vi", "VN");

        try (MockedStatic<ResourceBundle> mocked = Mockito.mockStatic(ResourceBundle.class)) {
            ResourceBundle viBundle = Mockito.mock(ResourceBundle.class);
            Mockito.when(viBundle.getString(TEST_KEY)).thenReturn(TEST_VALUE_VI);

            mocked.when(() -> ResourceBundle.getBundle(eq("application"), eq(vietnamese)))
                    .thenReturn(viBundle);

            mocked.when(() -> ResourceBundle.getBundle("application")).thenReturn(Mockito.mock(ResourceBundle.class));

            String result = AppConfig.get(TEST_KEY, vietnamese);

            assertEquals(TEST_VALUE_VI, result);
            mocked.verify(() -> ResourceBundle.getBundle("application", vietnamese));
        }
    }

    @Test
    void get_WithInvalidLocale_ShouldThrowMissingResourceException() {
        Locale invalidLocale = new Locale("xx", "YY");

        try (MockedStatic<ResourceBundle> mocked = Mockito.mockStatic(ResourceBundle.class)) {
            mocked.when(() -> ResourceBundle.getBundle(eq("application"), eq(invalidLocale)))
                    .thenThrow(new java.util.MissingResourceException(
                            "Can't find bundle for base name application, locale xx_YY",
                            "application", "xx_YY"));

            // WHEN & THEN
            MissingResourceException exception = assertThrows(
                    java.util.MissingResourceException.class,
                    () -> AppConfig.get(TEST_KEY, invalidLocale)
            );

            assertTrue(exception.getMessage().contains("Can't find bundle"));
        }
    }

}
