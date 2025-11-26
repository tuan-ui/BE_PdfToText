package com.noffice.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeleteMultiDTOTest {
    @Test
    void testGetterSetterEqualsHashCodeToString() {
        UUID id = UUID.randomUUID();
        String name = "Test Name";
        String code = "TEST";
        Long version = 5L;

        // Test NoArgsConstructor + Setter
        DeleteMultiDTO dto = new DeleteMultiDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setCode(code);
        dto.setVersion(version);

        // Test Getter
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
        assertEquals(code, dto.getCode());
        assertEquals(version, dto.getVersion());

        // Test AllArgsConstructor
        DeleteMultiDTO dto2 = new DeleteMultiDTO(id, name, code, version);
        assertEquals(id, dto2.getId());
        assertEquals(name, dto2.getName());
        assertEquals(code, dto2.getCode());
        assertEquals(version, dto2.getVersion());

        // Test equals and hashCode
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());

        // Test toString contains field values
        String str = dto.toString();
        assertTrue(str.contains(id.toString()));
        assertTrue(str.contains(name));
        assertTrue(str.contains(code));
        assertTrue(str.contains(version.toString()));
    }
}
