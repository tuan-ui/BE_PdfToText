package com.noffice.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserLoginDTOTest {
    @Test
    void testGetterSetterEqualsHashCodeToString() {
        String username = "old";
        String password = "new";

        // Test NoArgsConstructor + Setter
        UserLoginDTO dto = new  UserLoginDTO();
        dto.setUsername(username);
        dto.setPassword(password);

        // Test Getter
        assertEquals(username, dto.getUsername());
        assertEquals(password, dto.getPassword());

        // Test AllArgsConstructor
        UserLoginDTO dto2 = new  UserLoginDTO(username,password);
        assertEquals(username, dto2.getUsername());
        assertEquals(password, dto2.getPassword());


        // Test AllArgsConstructor
        UserLoginDTO dto3 = UserLoginDTO.builder()
                .username(username)
                .password(password)
                .build();
        assertEquals(username, dto3.getUsername());
        assertEquals(password, dto3.getPassword());

        // Test equals and hashCode
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());

        // Test toString contains field values
        String str = dto.toString();
        assertTrue(str.contains(username));
        assertTrue(str.contains(password));
    }
}
