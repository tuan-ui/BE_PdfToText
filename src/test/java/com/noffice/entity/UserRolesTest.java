package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UserRolesTest {
    UUID userId = UUID.randomUUID();
    UUID roleId = UUID.randomUUID();

    @Test
    void testUserRolesId_GettersAndSetters() {

        UserRolesId id = new UserRolesId();
        id.setUserId(userId);
        id.setRoleId(roleId);

        assertThat(id.getUserId()).isEqualTo(userId);
        assertThat(id.getRoleId()).isEqualTo(roleId);
    }

    @Test
    void testUserRolesId_AllArgsConstructor() {

        UserRolesId id = new UserRolesId(userId, roleId);

        assertThat(id.getUserId()).isEqualTo(userId);
        assertThat(id.getRoleId()).isEqualTo(roleId);
    }

    @Test
    void testUserRolesId_EqualsAndHashCode() {

        UserRolesId id1 = new UserRolesId(userId, roleId);
        UserRolesId id2 = new UserRolesId(userId, roleId);

        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }

    @Test
    void testUserRoles_GettersAndSetters() {

        UserRolesId id = new UserRolesId(userId, roleId);
        UserRoles pr = new UserRoles();
        pr.setId(id);

        assertThat(pr.getId()).isEqualTo(id);
    }

    @Test
    void testUserRoles_AllArgsConstructor() {

        UserRolesId id = new UserRolesId(userId, roleId);
        UserRoles pr = new UserRoles(id);

        assertThat(pr.getId()).isEqualTo(id);
    }

    @Test
    void testToStringIncludesFields() {

        UserRolesId id = new UserRolesId(userId, roleId);
        UserRoles pr = new UserRoles(id);

        String str = pr.toString();
        assertThat(str).contains(userId.toString());
        assertThat(str).contains(roleId.toString());
    }
}
