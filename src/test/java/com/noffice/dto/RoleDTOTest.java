package com.noffice.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleDTOTest {
    @Test
    void testGetterSetterEqualsHashCodeToString() {
        UUID id = UUID.randomUUID();
        String roleName = "name";
        String roleCode = "code";
        String roleDescription = "description";
        Long version = 1L;
        UUID partnerId = UUID.randomUUID();
        String partnerName = "name";
        Boolean isActive = Boolean.TRUE;
        Boolean isDeleted   = Boolean.FALSE;
        LocalDateTime createAt = LocalDateTime.now();
        LocalDateTime updateAt = LocalDateTime.now();
        UUID createBy = UUID.randomUUID();
        UUID updateBy = UUID.randomUUID();

        // Test NoArgsConstructor + Setter
        RoleDTO dto = new RoleDTO();
        dto.setId(id);
        dto.setRoleName(roleName);
        dto.setRoleCode(roleCode);
        dto.setRoleDescription(roleDescription);
        dto.setVersion(version);
        dto.setPartnerId(partnerId);
        dto.setPartnerName(partnerName);
        dto.setIsActive(isActive);
        dto.setIsDeleted(isDeleted);
        dto.setCreateAt(createAt);
        dto.setUpdateAt(updateAt);
        dto.setCreateBy(createBy);
        dto.setUpdateBy(updateBy);

        // Test Getter
        assertEquals(id, dto.getId());
        assertEquals(roleName, dto.getRoleName());
        assertEquals(roleCode, dto.getRoleCode());
        assertEquals(roleDescription, dto.getRoleDescription());
        assertEquals(version, dto.getVersion());
        assertEquals(partnerId, dto.getPartnerId());
        assertEquals(partnerName, dto.getPartnerName());
        assertEquals(isActive, dto.getIsActive());
        assertEquals(isDeleted, dto.getIsDeleted());

        // Test AllArgsConstructor
        RoleDTO dto2 = new RoleDTO(id,roleName, roleCode, roleDescription, version, partnerId,
                partnerName,isActive, isDeleted, createAt, updateAt, createBy, updateBy);
        assertEquals(id, dto2.getId());
        assertEquals(roleName, dto2.getRoleName());
        assertEquals(roleCode, dto2.getRoleCode());
        assertEquals(roleDescription, dto2.getRoleDescription());
        assertEquals(version, dto2.getVersion());
        assertEquals(partnerId, dto2.getPartnerId());
        assertEquals(partnerName, dto2.getPartnerName());
        assertEquals(isActive, dto2.getIsActive());
        assertEquals(isDeleted, dto2.getIsDeleted());

        // Test equals and hashCode
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());

        // Test toString contains field values
        String str = dto.toString();
        assertTrue(str.contains(roleName));
        assertTrue(str.contains(roleCode));
    }
}
