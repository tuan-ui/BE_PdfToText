package com.noffice.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RoleTest {

    @Test
    void testDefaultValues_andGettersSetters() {
        Role p = new Role();

        UUID id = UUID.randomUUID();

        p.setId(id);
        p.setRoleName("P001");
        p.setRoleCode("Partner One");
        p.setRoleDescription("123 Street");

        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.getRoleName()).isEqualTo("P001");
        assertThat(p.getRoleCode()).isEqualTo("Partner One");
        assertThat(p.getRoleDescription()).isEqualTo("123 Street");
    }

    @Test
    void testAllArgsConstructor() {
        UUID id = UUID.randomUUID();

        Role p = new Role();
        p.setId(id);
        p.setRoleName("P001");
        p.setRoleCode("Partner One");
        p.setRoleDescription("123 Street");

        assertThat(p.getId()).isEqualTo(id);
        assertThat(p.getRoleName()).isEqualTo("P001");
        assertThat(p.getRoleCode()).isEqualTo("Partner One");
        assertThat(p.getRoleDescription()).isEqualTo("123 Street");
    }

    @Test
    void testToString_containsFields() {
        Role p = new Role();
        p.setRoleName("P001");
        p.setRoleCode("Partner One");
        p.setRoleDescription("123 Street");

        String str = p.toString();
        assertThat(str).contains("P001");
        assertThat(str).contains("Partner One");
        assertThat(str).contains("123 Street");
    }

}
