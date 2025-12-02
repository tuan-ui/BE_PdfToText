package com.noffice.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateUserGroupDTOTest {

    @Test
    void testGetterSetterEqualsHashCodeToString() {
        UUID id = UUID.randomUUID();
        String groupName = "old";
        String groupCode = "new";
        List<UUID> userIds = List.of(id);
        Long version = 1L;

        // Test NoArgsConstructor + Setter
        CreateUserGroupDTO dto = new CreateUserGroupDTO();
        dto.setId(id);
        dto.setGroupName(groupName);
        dto.setGroupCode(groupCode);
        dto.setUserIds(userIds);
        dto.setVersion(version);

        // Test Getter
        assertEquals(id, dto.getId());
        assertEquals(groupName, dto.getGroupName());
        assertEquals(groupCode, dto.getGroupCode());
        assertEquals(userIds, dto.getUserIds());
        assertEquals(version, dto.getVersion());

        // Test AllArgsConstructor
        CreateUserGroupDTO dto2 = new CreateUserGroupDTO(id,groupName,groupCode, userIds, version);
        assertEquals(id, dto2.getId());
        assertEquals(groupName, dto2.getGroupName());
        assertEquals(groupCode, dto2.getGroupCode());
        assertEquals(userIds, dto2.getUserIds());
        assertEquals(version, dto2.getVersion());

        // Test equals and hashCode
        assertEquals(dto, dto2);
        assertEquals(dto.hashCode(), dto2.hashCode());

        // Test toString contains field values
        String str = dto.toString();
        assertTrue(str.contains(id.toString()));
        assertTrue(str.contains(groupName));
        assertTrue(str.contains(groupCode));
        assertTrue(str.contains(version.toString()));
    }
}
