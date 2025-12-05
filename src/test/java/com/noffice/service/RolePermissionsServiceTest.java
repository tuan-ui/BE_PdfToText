// File: RolePermissionsServiceTest.java
package com.noffice.service;

import com.noffice.entity.*;
import com.noffice.repository.RolePermissionsRepository;
import com.noffice.repository.RoleRepository;
import com.noffice.ultils.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RolePermissionsServiceTest {

    @Mock private RolePermissionsRepository rolePermissionsRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private LogService logService;

    @InjectMocks private RolePermissionsService service;

    private final UUID roleId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final UUID perm1 = UUID.fromString("a0000000-0000-0000-0000-000000000001");
    private final UUID perm2 = UUID.fromString("a0000000-0000-0000-0000-000000000002");
    private final User user = new User();

    @Test
    void getRolePermissions() {
        when(rolePermissionsRepository.findPermissionsByRoleId(roleId))
                .thenReturn(List.of(new Permission(), new Permission()));

        assertEquals(2, service.getRolePermissions(roleId).size());
        verify(rolePermissionsRepository).findPermissionsByRoleId(roleId);
    }

    @Test
    void getRolePermissionsHalf() {
        when(rolePermissionsRepository.findPermissionsHalfByRoleId(roleId))
                .thenReturn(List.of(new Permission()));

        assertEquals(1, service.getRolePermissionsHalf(roleId).size());
    }

    @Test
    void updatePermissionsForRole_RoleNotFound_ReturnsError() {
        when(roleRepository.findByRoleIdIncluideDeleted(roleId)).thenReturn(null);

        String result = service.updatePermissionsForRole(roleId, List.of(perm1), List.of());

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
        verify(rolePermissionsRepository, never()).saveAll(any());
    }

    @Test
    void updatePermissionsForRole_Success_DeleteOldAndSaveNew() {
        Role role = new Role();
        when(roleRepository.findByRoleIdIncluideDeleted(roleId)).thenReturn(role);

        UUID halfPerm = UUID.randomUUID();
        String result = service.updatePermissionsForRole(
                roleId,
                List.of(perm1, perm2),
                List.of(halfPerm)
        );

        assertEquals("", result);

        verify(rolePermissionsRepository).deleteByRoleId(roleId);

    }

    @Test
    void updatePermissionsForRole_EmptyLists_OnlyDeleteOld() {
        when(roleRepository.findByRoleIdIncluideDeleted(roleId)).thenReturn(new Role());

        service.updatePermissionsForRole(roleId, List.of(), List.of());

        verify(rolePermissionsRepository).deleteByRoleId(roleId);

    }

    @Test
    void getUserPermissions() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        user.setId(id);
        when(rolePermissionsRepository.findPermissionsByUserID(id))
                .thenReturn(List.of(new Permission()));

        assertEquals(1, service.getUserPermissions(user).size());
    }

    @Test
    void getUserOriginDataPermissions() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        user.setId(id);
        when(rolePermissionsRepository.findChildrenByParentCodeAndUserId("MENU_001", id))
                .thenReturn(List.of(new Permission()));

        assertEquals(1, service.getUserOriginDataPermissions("MENU_001", user).size());
    }

    @Test
    void getPermissionsCurrent() {
        UUID id = UUID.fromString("11111111-1111-1111-1111-111111111111");
        user.setId(id);
        when(rolePermissionsRepository.getPermissionsCurrent("DATA_001", id))
                .thenReturn(List.of("READ", "WRITE"));

        List<String> result = service.getPermissionsCurrent("DATA_001", user);
        assertEquals(2, result.size());
        assertTrue(result.contains("READ"));
    }
}