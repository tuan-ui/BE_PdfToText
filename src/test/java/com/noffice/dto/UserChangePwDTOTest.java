package com.noffice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserChangePwDTOTest {
    @Test
    void testGetterSetterEqualsHashCodeToString() {
        String oldPassword = "old";
        String newPassword = "new";

        // Test NoArgsConstructor + Setter
        UserChangePwDTO dto = new  UserChangePwDTO();
        dto.setOldPassword(oldPassword);
        dto.setNewPassword(newPassword);

        // Test Getter
        assertEquals(oldPassword, dto.getOldPassword());
        assertEquals(newPassword, dto.getNewPassword());

        // Test AllArgsConstructor
        UserChangePwDTO dto2 = new  UserChangePwDTO(oldPassword,newPassword);
        assertEquals(oldPassword, dto2.getOldPassword());
        assertEquals(newPassword, dto2.getNewPassword());

        // Test equals and hashCode
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());

        // Test toString contains field values
        String str = dto.toString();
        assertTrue(str.contains(oldPassword));
        assertTrue(str.contains(newPassword));
    }
}
