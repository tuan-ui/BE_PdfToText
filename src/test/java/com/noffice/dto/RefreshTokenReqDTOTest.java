package com.noffice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RefreshTokenReqDTOTest {

    @Test
    void testGetterSetterEqualsHashCodeToString() {
        String refreshToken = "refreshToken";

        // Test NoArgsConstructor + Setter
        RefreshTokenReqDTO dto = new RefreshTokenReqDTO();
        dto.setRefreshToken(refreshToken);

        // Test Getter
        assertEquals(refreshToken, dto.getRefreshToken());

        // Test AllArgsConstructor
        RefreshTokenReqDTO dto2 = new RefreshTokenReqDTO(refreshToken);
        assertEquals(refreshToken, dto2.getRefreshToken());

        // Test equals and hashCode
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());

        // Test toString contains field values
        String str = dto.toString();
        assertTrue(str.contains(refreshToken));
    }
}
