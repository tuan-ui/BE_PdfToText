package com.noffice.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangePasswordDTOTest {

    @Test
    void testGetterSetterEqualsHashCodeToString() {
        UUID id = UUID.randomUUID();
        String oldPass = "old";
        String newPass = "new";

        // Test NoArgsConstructor + Setter
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setUserId(id);
        dto.setStaffPassword("old");
        dto.setUserNewPassword("new");

        // Test Getter
        assertEquals(id, dto.getUserId());
        assertEquals("old", dto.getStaffPassword());
        assertEquals("new", dto.getUserNewPassword());

        // Test AllArgsConstructor
        ChangePasswordDTO dto2 = new ChangePasswordDTO(oldPass, id, newPass);
        assertEquals(id, dto2.getUserId());
        assertEquals("old", dto2.getStaffPassword());
        assertEquals("new", dto2.getUserNewPassword());

        // Test equals and hashCode
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());

        // Test toString contains field values
        String str = dto.toString();
        assertTrue(str.contains(id.toString()));
        assertTrue(str.contains(oldPass));
        assertTrue(str.contains(newPass));
    }
}
