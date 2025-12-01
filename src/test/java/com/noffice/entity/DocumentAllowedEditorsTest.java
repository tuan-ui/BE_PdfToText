package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class DocumentAllowedEditorsTest {
    private final UUID tempId = UUID.randomUUID();
    private final UUID tempDocumentTemplateId = UUID.randomUUID();
    private final UUID tempDocumentTypeId = UUID.randomUUID();
    @Test
    void dataAnnotation_Works() {
        DocumentAllowedEditors documentAllowedEditors = new DocumentAllowedEditors();
        documentAllowedEditors.setId(tempId);
        documentAllowedEditors.setDocumentId(tempDocumentTemplateId);
        documentAllowedEditors.setEditorId(tempDocumentTypeId);

        assertEquals(tempId, documentAllowedEditors.getId());
        assertEquals(tempDocumentTemplateId, documentAllowedEditors.getDocumentId());
        assertEquals(tempDocumentTypeId, documentAllowedEditors.getEditorId());
    }

    @Test
    void constructors_Works() {
        DocumentAllowedEditors empty = new DocumentAllowedEditors();
        assertNotNull(empty);

        DocumentAllowedEditors full = new DocumentAllowedEditors(tempId, tempDocumentTemplateId,tempDocumentTypeId);
        assertEquals(tempId, full.getId());
        assertEquals(tempDocumentTemplateId, full.getDocumentId());
        assertEquals(tempDocumentTypeId, full.getEditorId());
    }

    @Test
    void equalsHashCode_IgnoresBaseEntityFields() {
        DocumentAllowedEditors d1 = new DocumentAllowedEditors(tempId, tempDocumentTemplateId,tempDocumentTypeId);
        DocumentAllowedEditors d2 = new DocumentAllowedEditors(tempId, tempDocumentTemplateId,tempDocumentTypeId);

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        DocumentAllowedEditors d1 = new DocumentAllowedEditors(UUID.randomUUID(), tempDocumentTemplateId,tempDocumentTypeId);
        DocumentAllowedEditors d2 = new DocumentAllowedEditors(UUID.randomUUID(), tempDocumentTemplateId,tempDocumentTypeId);

        assertNotEquals(d1, d2);
    }

    @Test
    void testOnCreate_whenIdIsNull_shouldGenerateUUID() {

        DocumentAllowedEditors entity = new DocumentAllowedEditors();
        entity.setDocumentId(UUID.randomUUID());
        entity.setEditorId(UUID.randomUUID());

        assertThat(entity.getId()).isNull();
        entity.onCreate();
        assertThat(entity.getId()).isNotNull();
    }

    @Test
    void testOnCreate_whenIdAlreadyExists_shouldNotOverride() {

        UUID existingId = UUID.randomUUID();
        DocumentAllowedEditors entity = new DocumentAllowedEditors();
        entity.setId(existingId);
        entity.setDocumentId(UUID.randomUUID());
        entity.setEditorId(UUID.randomUUID());

        entity.onCreate();
        assertThat(entity.getId()).isEqualTo(existingId);
    }
}
