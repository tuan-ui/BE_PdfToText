package com.noffice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForgotPasswordRequestTest {

    @Test
    void testGetterSetterEqualsHashCodeToString() {
        String username = "old";
        String phone = "new";

        // Test NoArgsConstructor + Setter
        ForgotPasswordRequest dto = new ForgotPasswordRequest();
        dto.setUsername(username);
        dto.setPhone(phone);

        // Test Getter
        assertEquals(username, dto.getUsername());
        assertEquals(phone, dto.getPhone());

        // Test AllArgsConstructor
        ForgotPasswordRequest dto2 = new ForgotPasswordRequest(username,phone);
        assertEquals(username, dto2.getUsername());
        assertEquals(phone, dto2.getPhone());

        // Test equals and hashCode
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());

        // Test toString contains field values
        String str = dto.toString();
        assertTrue(str.contains(username));
        assertTrue(str.contains(phone));
    }
}
