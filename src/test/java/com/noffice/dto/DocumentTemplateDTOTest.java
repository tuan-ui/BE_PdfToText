package com.noffice.dto;

import com.noffice.entity.DocumentFiles;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentTemplateDTOTest {

    @Test
    void testGetterSetterEqualsHashCodeToString() {
        UUID id = UUID.randomUUID();
        Integer version = 1;
        UUID parentId = UUID.randomUUID();
        Boolean isActive = true;
        String documentTemplateCode = "code";
        String documentTemplateName = "name";
        String documentTemplateDescription = "description";
        List<UUID> documentTypes = List.of(UUID.randomUUID());
        DocumentFiles attachFile = new DocumentFiles();


        // Test NoArgsConstructor + Setter
        DocumentTemplateDTO dto = new DocumentTemplateDTO();
        dto.setId(id);
        dto.setVersion(version);
        dto.setIsActive(isActive);
        dto.setPartnerId(parentId);
        dto.setDocumentTemplateCode(documentTemplateCode);
        dto.setDocumentTemplateName(documentTemplateName);
        dto.setDocumentTemplateDescription(documentTemplateDescription);
        dto.setDocumentTypeIds(documentTypes);
        dto.setAttachFile(attachFile);

        // Test Getter
        assertEquals(id, dto.getId());
        assertEquals(version, dto.getVersion());
        assertEquals(isActive, dto.getIsActive());
        assertEquals(documentTemplateCode, dto.getDocumentTemplateCode());
        assertEquals(documentTemplateName, dto.getDocumentTemplateName());
        assertEquals(documentTemplateDescription, dto.getDocumentTemplateDescription());
        assertEquals(parentId, dto.getPartnerId());
        assertEquals(documentTypes, dto.getDocumentTypeIds());
        assertEquals(attachFile, dto.getAttachFile());

        // Test AllArgsConstructor
        DocumentTemplateDTO dto2 = new DocumentTemplateDTO(id, version,parentId,isActive, documentTemplateCode,documentTemplateName, documentTemplateDescription,
                documentTypes, attachFile);
        assertEquals(id, dto2.getId());
        assertEquals(version, dto2.getVersion());
        assertEquals(isActive, dto2.getIsActive());
        assertEquals(documentTemplateCode, dto2.getDocumentTemplateCode());
        assertEquals(documentTemplateName, dto2.getDocumentTemplateName());
        assertEquals(documentTemplateDescription, dto2.getDocumentTemplateDescription());
        assertEquals(parentId, dto2.getPartnerId());
        assertEquals(documentTypes, dto2.getDocumentTypeIds());
        assertEquals(attachFile, dto2.getAttachFile());

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
