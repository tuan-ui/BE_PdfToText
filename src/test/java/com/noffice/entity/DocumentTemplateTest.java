package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class DocumentTemplateTest {
    private final UUID tempId = UUID.randomUUID();
    @Test
    void dataAnnotation() {
        DocumentTemplate documentTemplate = new DocumentTemplate();
        documentTemplate.setDocumentTemplateCode("code");
        documentTemplate.setDocumentTemplateName("name");
        documentTemplate.setDocumentTemplateDescription("description");
        documentTemplate.setAttachFileId(tempId);

        assertEquals("code", documentTemplate.getDocumentTemplateCode());
        assertEquals("name", documentTemplate.getDocumentTemplateName());
        assertEquals("description", documentTemplate.getDocumentTemplateDescription());
        assertEquals(tempId, documentTemplate.getAttachFileId());
    }

    @Test
    void constructors() {
        DocumentTemplate empty = new DocumentTemplate();
        assertNotNull(empty);

        DocumentTemplate full = new DocumentTemplate("code", "name","description", tempId);
        assertEquals("code", full.getDocumentTemplateCode());
        assertEquals("name", full.getDocumentTemplateName());
        assertEquals("description", full.getDocumentTemplateDescription());
        assertEquals(tempId, full.getAttachFileId());
    }

    @Test
    void equalsHashCode_IgnoresBaseEntityFields() {
        DocumentTemplate d1 = new DocumentTemplate("code", "name","description", tempId);
        DocumentTemplate d2 = new DocumentTemplate("code", "name","description", tempId);

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        DocumentTemplate d1 = new DocumentTemplate("code", "name 1","description", tempId);
        DocumentTemplate d2 = new DocumentTemplate("code", "name 2","description", tempId);

        assertNotEquals(d1, d2);
    }

    @Test
    void testEqualsAndHashCode() {
        DocumentTemplate df1 = new DocumentTemplate();

        df1.setDocumentTemplateCode("code");
        df1.setDocumentTemplateName("name");
        df1.setDocumentTemplateDescription("description");

        DocumentTemplate df2 = new DocumentTemplate();
        df2.setDocumentTemplateCode("code");
        df2.setDocumentTemplateName("name");
        df2.setDocumentTemplateDescription("description");

        assertThat(df1).isEqualTo(df2);
        assertThat(df1.hashCode()).isEqualTo(df2.hashCode());
    }

    @Test
    void testToStringContainsFieldNames() {
        DocumentTemplate df = new DocumentTemplate();
        df.setDocumentTemplateCode("code");
        df.setDocumentTemplateName("name");
        df.setDocumentTemplateDescription("description");

        String str = df.toString();
        assertThat(str).contains("documentTemplateCode", "code");
        assertThat(str).contains("documentTemplateName", "name");
        assertThat(str).contains("documentTemplateDescription", "description");
    }
}
