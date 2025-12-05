package com.noffice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormSchemaSearchDTOTest {

    @Test
    void testGetterSetterEqualsHashCodeToString() {
        String formName = "formName";
        String formCode = "formCode";
        Boolean status = true;
        Integer page = 0;
        Integer size = 10;

        // Test NoArgsConstructor + Setter
        FormSchemaSearchDTO dto = new FormSchemaSearchDTO();
        dto.setFormName(formName);
        dto.setFormCode(formCode);
        dto.setStatus(status);

        // Test Getter
        assertEquals(formName, dto.getFormName());
        assertEquals(formCode, dto.getFormCode());
        assertEquals(status, dto.getStatus());
        assertEquals(page, dto.getPage());
        assertEquals(size, dto.getSize());

        // Test AllArgsConstructor
        FormSchemaSearchDTO dto2 = new FormSchemaSearchDTO(formName,formCode, status, page, size);
        assertEquals(formName, dto2.getFormName());
        assertEquals(formCode, dto2.getFormCode());
        assertEquals(status, dto2.getStatus());
        assertEquals(page, dto2.getPage());
        assertEquals(size, dto2.getSize());

        // Test equals and hashCode
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());

        // Test toString contains field values
        String str = dto.toString();
        assertTrue(str.contains(formName));
        assertTrue(str.contains(formCode));
    }
}
