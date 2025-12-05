package com.noffice.dto;

import com.noffice.entity.DocType;
import com.noffice.entity.FormSchema;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentTemplateDetailDTOTest {

    @Test
    void testGetterSetterEqualsHashCodeToString() {
        UUID id = UUID.randomUUID();
        Long version = 1L;
        Boolean isActive = true;
        String documentTemplateCode = "code";
        String documentTemplateName = "name";
        String documentTemplateDescription = "description";
        UUID attachFileId = UUID.randomUUID();
        String fileName = "name";
        String wopiUrl = "wopiUrl";
        List<DocType> documentTypes = List.of(new DocType());
        FormSchema formSchema = new FormSchema();
        formSchema.setId(id);
        formSchema.setFormCode("code");
        formSchema.setFormName("name");
        formSchema.setFormContent("description");
        formSchema.setDocTemplateId(attachFileId);


        // Test NoArgsConstructor + Setter
        DocumentTemplateDetailDTO dto = new DocumentTemplateDetailDTO();
        dto.setId(id);
        dto.setVersion(version);
        dto.setIsActive(isActive);
        dto.setDocumentTemplateCode(documentTemplateCode);
        dto.setDocumentTemplateName(documentTemplateName);
        dto.setDocumentTemplateDescription(documentTemplateDescription);
        dto.setAttachFileId(attachFileId);
        dto.setFileName(fileName);
        dto.setWopiUrl(wopiUrl);
        dto.setDocumentTypes(documentTypes);
        dto.setFormSchema(formSchema);

        // Test Getter
        assertEquals(id, dto.getId());
        assertEquals(version, dto.getVersion());
        assertEquals(isActive, dto.getIsActive());
        assertEquals(documentTemplateCode, dto.getDocumentTemplateCode());
        assertEquals(documentTemplateName, dto.getDocumentTemplateName());
        assertEquals(documentTemplateDescription, dto.getDocumentTemplateDescription());

        // Test AllArgsConstructor
        DocumentTemplateDetailDTO dto2 = new DocumentTemplateDetailDTO(id, version,isActive, documentTemplateCode,documentTemplateName, documentTemplateDescription,
                attachFileId, fileName, wopiUrl,documentTypes,formSchema );
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
