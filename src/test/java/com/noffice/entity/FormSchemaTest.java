package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class FormSchemaTest {
    UUID tempId = UUID.randomUUID();
    @Test
    void dataAnnotation() {
        FormSchema formSchema = new FormSchema();
        formSchema.setFormCode("code");
        formSchema.setFormName("name");
        formSchema.setFormContent("content");
        formSchema.setDocTemplateId(tempId);

        assertEquals("code", formSchema.getFormCode());
        assertEquals("name", formSchema.getFormName());
        assertEquals("content", formSchema.getFormContent());
        assertEquals(tempId, formSchema.getDocTemplateId());
    }

    @Test
    void constructors() {
        FormSchema empty = new FormSchema();
        assertNotNull(empty);

        FormSchema full = new FormSchema("code", "name", "content", tempId);
        assertEquals("code", full.getFormCode());
        assertEquals("name", full.getFormName());
        assertEquals("content", full.getFormContent());
        assertEquals(tempId, full.getDocTemplateId());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        FormSchema d1 = new FormSchema("code", "name 1","content", tempId);
        FormSchema d2 = new FormSchema("code", "name 2","content", tempId);

        assertNotEquals(d1, d2);
    }

    @Test
    void testToStringContainsFieldNames() {
        FormSchema df = new FormSchema();
        df.setFormCode("code");
        df.setFormName("name");
        df.setFormContent("content");
        df.setDocTemplateId(tempId);

        String str = df.toString();
        assertThat(str).contains("formCode", "code");
        assertThat(str).contains("formName", "name");
        assertThat(str).contains("formContent", "content");
    }
}
