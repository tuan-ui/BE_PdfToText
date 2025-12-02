package com.noffice.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ChangePasswordAfterLoginDTOTest {

    @Test
    void testGetterSetterEqualsHashCodeToString() {
        UUID id = UUID.randomUUID();
        String oldPass = "old";
        String newPass = "new";

        // Test NoArgsConstructor + Setter
        ChangePasswordAfterLoginDTO dto = new ChangePasswordAfterLoginDTO();
        dto.setUserId(id);
        dto.setOldPassword("old");
        dto.setNewPassword("new");

        // Test Getter
        assertEquals(id, dto.getUserId());
        assertEquals("old", dto.getOldPassword());
        assertEquals("new", dto.getNewPassword());

        // Test AllArgsConstructor
        ChangePasswordAfterLoginDTO dto2 = new ChangePasswordAfterLoginDTO(id, oldPass, newPass);
        assertEquals(id, dto2.getUserId());
        assertEquals("old", dto2.getOldPassword());
        assertEquals("new", dto2.getNewPassword());

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
