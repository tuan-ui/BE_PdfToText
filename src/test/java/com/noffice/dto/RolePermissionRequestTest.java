package com.noffice.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RolePermissionRequestTest {
    @Test
    void testGetterSetterEqualsHashCodeToString() {
        UUID roleId = UUID.randomUUID();
        List<UUID> checkedKeys = List.of(UUID.randomUUID());
        List<UUID> checkedHalfKeys = List.of(UUID.randomUUID());

        // Test NoArgsConstructor + Setter
         RolePermissionRequest dto = new  RolePermissionRequest();
        dto.setRoleId(roleId);
        dto.setCheckedKeys(checkedKeys);
        dto.setCheckedHalfKeys(checkedHalfKeys);

        // Test Getter
        assertEquals(roleId, dto.getRoleId());
        assertEquals(checkedKeys, dto.getCheckedKeys());
        assertEquals(checkedHalfKeys, dto.getCheckedHalfKeys());

        // Test AllArgsConstructor
         RolePermissionRequest dto2 = new  RolePermissionRequest(roleId,checkedKeys, checkedHalfKeys);
        assertEquals(roleId, dto2.getRoleId());
        assertEquals(checkedKeys, dto2.getCheckedKeys());
        assertEquals(checkedHalfKeys, dto2.getCheckedHalfKeys());

        // Test equals and hashCode
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());

        // Test toString contains field values
        String str = dto.toString();
        assertTrue(str.contains(roleId.toString()));
        assertTrue(str.contains(checkedKeys.toString()));
    }
}
