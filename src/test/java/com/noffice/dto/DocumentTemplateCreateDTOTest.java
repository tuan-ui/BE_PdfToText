package com.noffice.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentTemplateCreateDTOTest {

    @Test
    void testGetterSetterEqualsHashCodeToString() {
        UUID id = UUID.randomUUID();
        Long version = 1L;
        Boolean isActive = true;
        String documentTemplateCode = "code";
        String documentTemplateName = "name";
        String documentTemplateDescription = "description";
        List<UUID> documentTypeIds = List.of(UUID.randomUUID());
        UUID attachFileId = UUID.randomUUID();
        String fileName = "name";
        String wopiUrl = "wopiUrl";
        List<UUID> allowedEditors = List.of(UUID.randomUUID());
        List<UUID> allowedViewers = List.of(UUID.randomUUID());

        // Test NoArgsConstructor + Setter
        DocumentTemplateCreateDTO dto = new DocumentTemplateCreateDTO();
        dto.setId(id);
        dto.setVersion(version);
        dto.setIsActive(isActive);
        dto.setDocumentTemplateCode(documentTemplateCode);
        dto.setDocumentTemplateName(documentTemplateName);
        dto.setDocumentTemplateDescription(documentTemplateDescription);
        dto.setDocumentTypeIds(documentTypeIds);
        dto.setAttachFileId(attachFileId);
        dto.setFileName(fileName);
        dto.setWopiUrl(wopiUrl);
        dto.setAllowedEditors(allowedEditors);
        dto.setAllowedViewers(allowedViewers);

        // Test Getter
        assertEquals(id, dto.getId());
        assertEquals(version, dto.getVersion());
        assertEquals(isActive, dto.getIsActive());
        assertEquals(documentTemplateCode, dto.getDocumentTemplateCode());
        assertEquals(documentTemplateName, dto.getDocumentTemplateName());
        assertEquals(documentTemplateDescription, dto.getDocumentTemplateDescription());

        // Test AllArgsConstructor
        DocumentTemplateCreateDTO dto2 = new DocumentTemplateCreateDTO(id, version,isActive, documentTemplateCode,documentTemplateName, documentTemplateDescription,
                documentTypeIds,attachFileId, fileName, wopiUrl, allowedEditors, allowedViewers);
        assertEquals(id, dto2.getId());
        assertEquals(version, dto2.getVersion());
        assertEquals(isActive, dto2.getIsActive());
        assertEquals(documentTemplateCode, dto2.getDocumentTemplateCode());
        assertEquals(documentTemplateName, dto2.getDocumentTemplateName());
        assertEquals(documentTemplateDescription, dto2.getDocumentTemplateDescription());

        // Test equals and hashCode
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());

        // Test toString contains field values
        String str = dto.toString();
        assertTrue(str.contains(id.toString()));
        assertTrue(str.contains(documentTemplateCode));
        assertTrue(str.contains(documentTemplateName));
        assertTrue(str.contains(documentTemplateDescription));
    }
}
