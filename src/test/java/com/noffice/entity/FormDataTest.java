package com.noffice.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class FormDataTest {
    @Test
    void dataAnnotation() {
        FormData formData = new FormData();
        formData.setFormCode("code");
        formData.setFormContent("name");

        assertEquals("code", formData.getFormCode());
        assertEquals("name", formData.getFormContent());
    }

    @Test
    void constructors() {
        FormData empty = new FormData();
        assertNotNull(empty);

        FormData full = new FormData("code", "name");
        assertEquals("code", full.getFormCode());
        assertEquals("name", full.getFormContent());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        FormData d1 = new FormData("code", "name 1");
        FormData d2 = new FormData("code", "name 2");

        assertNotEquals(d1, d2);
    }

    @Test
    void testToStringContainsFieldNames() {
        FormData df = new FormData();
        df.setFormCode("code");
        df.setFormContent("name");

        String str = df.toString();
        assertThat(str).contains("formCode", "code");
        assertThat(str).contains("formContent", "name");
    }
}
