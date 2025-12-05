package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DocumentAllowedViewersTest {
    private final UUID tempId = UUID.randomUUID();
    private final UUID tempDocumentTemplateId = UUID.randomUUID();
    private final UUID tempDocumentTypeId = UUID.randomUUID();
    @Test
    void dataAnnotation_Works() {
        DocumentAllowedViewers documentAllowedViewers = new DocumentAllowedViewers();
        documentAllowedViewers.setId(tempId);
        documentAllowedViewers.setDocumentId(tempDocumentTemplateId);
        documentAllowedViewers.setViewerId(tempDocumentTypeId);

        assertEquals(tempId, documentAllowedViewers.getId());
        assertEquals(tempDocumentTemplateId, documentAllowedViewers.getDocumentId());
        assertEquals(tempDocumentTypeId, documentAllowedViewers.getViewerId());
    }

    @Test
    void constructors_Works() {
        DocumentAllowedViewers empty = new DocumentAllowedViewers();
        assertNotNull(empty);

        DocumentAllowedViewers full = new DocumentAllowedViewers(tempId, tempDocumentTemplateId,tempDocumentTypeId);
        assertEquals(tempId, full.getId());
        assertEquals(tempDocumentTemplateId, full.getDocumentId());
        assertEquals(tempDocumentTypeId, full.getViewerId());
    }

    @Test
    void equalsHashCode_IgnoresBaseEntityFields() {
        DocumentAllowedViewers d1 = new DocumentAllowedViewers(tempId, tempDocumentTemplateId,tempDocumentTypeId);
        DocumentAllowedViewers d2 = new DocumentAllowedViewers(tempId, tempDocumentTemplateId,tempDocumentTypeId);

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        DocumentAllowedViewers d1 = new DocumentAllowedViewers(UUID.randomUUID(), tempDocumentTemplateId,tempDocumentTypeId);
        DocumentAllowedViewers d2 = new DocumentAllowedViewers(UUID.randomUUID(), tempDocumentTemplateId,tempDocumentTypeId);

        assertNotEquals(d1, d2);
    }

    @Test
    void testOnCreate_whenIdIsNull_shouldGenerateUUID() {

        DocumentAllowedViewers entity = new DocumentAllowedViewers();
        entity.setDocumentId(UUID.randomUUID());
        entity.setViewerId(UUID.randomUUID());

        assertThat(entity.getId()).isNull();
        entity.onCreate();
        assertThat(entity.getId()).isNotNull();
    }

    @Test
    void testOnCreate_whenIdAlreadyExists_shouldNotOverride() {

        UUID existingId = UUID.randomUUID();
        DocumentAllowedViewers entity = new DocumentAllowedViewers();
        entity.setId(existingId);
        entity.setDocumentId(UUID.randomUUID());
        entity.setViewerId(UUID.randomUUID());

        entity.onCreate();
        assertThat(entity.getId()).isEqualTo(existingId);
    }
}
