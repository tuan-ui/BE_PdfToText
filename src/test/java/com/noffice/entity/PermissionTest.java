package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PermissionTest {

    @Test
    void testGettersAndSetters() {
        Permission p = new Permission();

        UUID id = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        p.setId(id);
        p.setPermissionName("Read");
        p.setPermissionCode("READ");
        p.setPermissionUrl("/api/read");
        p.setPermissionParent(parentId);
        p.setPosition(1);
        p.setIsAdmin(true);
        p.setIsDeleted(false);
        p.setIsMenus(true);

        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.getPermissionName()).isEqualTo("Read");
        assertThat(p.getPermissionCode()).isEqualTo("READ");
        assertThat(p.getPermissionUrl()).isEqualTo("/api/read");
        assertThat(p.getPermissionParent()).isEqualTo(parentId);
        assertThat(p.getPosition()).isEqualTo(1);
        assertThat(p.getIsAdmin()).isTrue();
        assertThat(p.getIsDeleted()).isFalse();
        assertThat(p.getIsMenus()).isTrue();
    }

    @Test
    void testAllArgsConstructor() {
        UUID id = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();

        Permission p = new Permission(
                id,
                "Read",
                "READ",
                "/api/read",
                parentId,
                1,
                true,
                false,
                true
        );

        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.getPermissionName()).isEqualTo("Read");
        assertThat(p.getPermissionCode()).isEqualTo("READ");
        assertThat(p.getPermissionUrl()).isEqualTo("/api/read");
        assertThat(p.getPermissionParent()).isEqualTo(parentId);
        assertThat(p.getPosition()).isEqualTo(1);
        assertThat(p.getIsAdmin()).isTrue();
        assertThat(p.getIsDeleted()).isFalse();
        assertThat(p.getIsMenus()).isTrue();
    }

    @Test
    void testEqualsAndHashCode() {
        UUID id = UUID.randomUUID();
        Permission p1 = new Permission();
        p1.setId(id);
        p1.setPermissionName("Read");
        p1.setPermissionCode("READ");

        Permission p2 = new Permission();
        p2.setId(id);
        p2.setPermissionName("Read");
        p2.setPermissionCode("READ");

        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    void testToStringContainsFields() {
        Permission p = new Permission();
        p.setPermissionName("Read");
        p.setPermissionCode("READ");

        String str = p.toString();
        assertThat(str).contains("Read");
        assertThat(str).contains("READ");
    }
}
