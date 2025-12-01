package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class DocTemplateDocTypesTest {
    private final UUID tempId = UUID.randomUUID();
    private final UUID tempDocumentTemplateId = UUID.randomUUID();
    private final UUID tempDocumentTypeId = UUID.randomUUID();
    @Test
    void dataAnnotation_Works() {
        DocTemplateDocTypes docTemplateDocTypes = new DocTemplateDocTypes();
        docTemplateDocTypes.setId(tempId);
        docTemplateDocTypes.setDocumentTemplateId(tempDocumentTemplateId);
        docTemplateDocTypes.setDocumentTypeId(tempDocumentTypeId);

        assertEquals(tempId, docTemplateDocTypes.getId());
        assertEquals(tempDocumentTemplateId, docTemplateDocTypes.getDocumentTemplateId());
        assertEquals(tempDocumentTypeId, docTemplateDocTypes.getDocumentTypeId());
    }

    @Test
    void constructors_Works() {
        DocTemplateDocTypes empty = new DocTemplateDocTypes();
        assertNotNull(empty);

        DocTemplateDocTypes full = new DocTemplateDocTypes(tempId, tempDocumentTemplateId,tempDocumentTypeId);
        assertEquals(tempId, full.getId());
        assertEquals(tempDocumentTemplateId, full.getDocumentTemplateId());
        assertEquals(tempDocumentTypeId, full.getDocumentTypeId());
    }

    @Test
    void equalsHashCode_IgnoresBaseEntityFields() {
        DocTemplateDocTypes d1 = new DocTemplateDocTypes(tempId, tempDocumentTemplateId,tempDocumentTypeId);
        DocTemplateDocTypes d2 = new DocTemplateDocTypes(tempId, tempDocumentTemplateId,tempDocumentTypeId);

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void equals_DifferentCode_NotEqual() {
        DocTemplateDocTypes d1 = new DocTemplateDocTypes(UUID.randomUUID(), tempDocumentTemplateId,tempDocumentTypeId);
        DocTemplateDocTypes d2 = new DocTemplateDocTypes(UUID.randomUUID(), tempDocumentTemplateId,tempDocumentTypeId);

        assertNotEquals(d1, d2);
    }

    @Test
    void testOnCreate_whenIdIsNull_shouldGenerateUUID() {

        DocTemplateDocTypes entity = new DocTemplateDocTypes();
        entity.setDocumentTemplateId(UUID.randomUUID());
        entity.setDocumentTypeId(UUID.randomUUID());

        assertThat(entity.getId()).isNull();
        entity.onCreate();
        assertThat(entity.getId()).isNotNull();
    }

    @Test
    void testOnCreate_whenIdAlreadyExists_shouldNotOverride() {

        UUID existingId = UUID.randomUUID();
        DocTemplateDocTypes entity = new DocTemplateDocTypes();
        entity.setId(existingId);
        entity.setDocumentTemplateId(UUID.randomUUID());
        entity.setDocumentTypeId(UUID.randomUUID());

        entity.onCreate();
        assertThat(entity.getId()).isEqualTo(existingId);
    }
}
