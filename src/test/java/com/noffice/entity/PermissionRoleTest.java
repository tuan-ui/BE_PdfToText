package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionRoleTest {

    @Test
    void testPermissionRoleId_GettersAndSetters() {
        UUID permissionId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        Boolean isHalf = true;

        PermissionRoleId id = new PermissionRoleId();
        id.setPermissionId(permissionId);
        id.setRoleId(roleId);
        id.setIsHalf(isHalf);

        assertThat(id.getPermissionId()).isEqualTo(permissionId);
        assertThat(id.getRoleId()).isEqualTo(roleId);
        assertThat(id.getIsHalf()).isTrue();
    }

    @Test
    void testPermissionRoleId_AllArgsConstructor() {
        UUID permissionId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        Boolean isHalf = false;

        PermissionRoleId id = new PermissionRoleId(permissionId, roleId, isHalf);

        assertThat(id.getPermissionId()).isEqualTo(permissionId);
        assertThat(id.getRoleId()).isEqualTo(roleId);
        assertThat(id.getIsHalf()).isFalse();
    }

    @Test
    void testPermissionRoleId_EqualsAndHashCode() {
        UUID permissionId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        PermissionRoleId id1 = new PermissionRoleId(permissionId, roleId, true);
        PermissionRoleId id2 = new PermissionRoleId(permissionId, roleId, true);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void testPermissionRole_GettersAndSetters() {
        UUID permissionId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        Boolean isHalf = true;

        PermissionRoleId id = new PermissionRoleId(permissionId, roleId, isHalf);
        PermissionRole pr = new PermissionRole();
        pr.setId(id);

        assertThat(pr.getId()).isEqualTo(id);
    }

    @Test
    void testPermissionRole_AllArgsConstructor() {
        UUID permissionId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();
        Boolean isHalf = false;

        PermissionRoleId id = new PermissionRoleId(permissionId, roleId, isHalf);
        PermissionRole pr = new PermissionRole(id);

        assertThat(pr.getId()).isEqualTo(id);
    }

    @Test
    void testToStringIncludesFields() {
        UUID permissionId = UUID.randomUUID();
        UUID roleId = UUID.randomUUID();

        PermissionRoleId id = new PermissionRoleId(permissionId, roleId, true);
        PermissionRole pr = new PermissionRole(id);

        String str = pr.toString();
        assertThat(str).contains(permissionId.toString());
        assertThat(str).contains(roleId.toString());
        assertThat(str).contains("true");
    }
}
