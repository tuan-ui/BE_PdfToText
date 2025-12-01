package com.noffice.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void testUser_GettersAndSetters() {
        UUID commonId = UUID.randomUUID();
        Date now = new Date();

        Role role = new Role();
        role.setId(commonId);

        User user = new User();
        user.setId(commonId);
        user.setUsername("john.doe");
        user.setUserCode("U001");
        user.setPassword("password");
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setIsAdmin(1);
        user.setBirthday(now);

        user.setIdentifyCode("code");
        user.setSignatureImage("image");
        user.setProfileImage("image");
        user.setPhone("phone");
        user.setIssuePlace("place");
        user.setIssueDate(now);
        user.setGender(true);
        user.setIsChangePassword(1);
        user.setTwofaType(1);
        user.setLstRole(List.of(role));

        assertThat(user.getId()).isEqualTo(commonId);
        assertThat(user.getUsername()).isEqualTo("john.doe");
        assertThat(user.getUserCode()).isEqualTo("U001");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getFullName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getIsAdmin()).isEqualTo(1);
        assertThat(user.getBirthday()).isEqualTo(now);

        assertThat(user.getIdentifyCode()).isEqualTo("code");
        assertThat(user.getSignatureImage()).isEqualTo("image");
        assertThat(user.getProfileImage()).isEqualTo("image");
        assertThat(user.getPhone()).isEqualTo("phone");
        assertThat(user.getIssuePlace()).isEqualTo("place");
        assertThat(user.getIssueDate()).isEqualTo(now);
        assertThat(user.getGender()).isEqualTo(true);
        assertThat(user.getIsChangePassword()).isEqualTo(1);
        assertThat(user.getTwofaType()).isEqualTo(1);
        assertThat(user.getLstRole()).isEqualTo(List.of(role));
    }

    @Test
    void testUser_AllArgsConstructor() {
        Date now = new Date();
        User user = new User(
                "john.doe", "U001", "ID123", null, null,
                "0123456789", "password", "HN", now, 1,
                true, "John Doe", "john@example.com", now,
                0, 0, null, null, null, null
        );

        assertThat(user.getUsername()).isEqualTo("john.doe");
        assertThat(user.getUserCode()).isEqualTo("U001");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getFullName()).isEqualTo("John Doe");
        assertThat(user.getEmail()).isEqualTo("john@example.com");
        assertThat(user.getIsAdmin()).isEqualTo(1);
    }

    @Test
    void testGetAuthorities_Admin() {
        User admin = new User();
        admin.setIsAdmin(1);

        Collection<? extends GrantedAuthority> authorities = admin.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void testGetAuthorities_User() {
        User user = new User();
        user.setIsAdmin(0);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    void testTransientFields() {
        User user = new User();
        user.setIssueDateStr("01-01-2000");
        user.setBirthdayStr("02-02-2000");
        user.setRoleIds("ROLE1,ROLE2");

        assertThat(user.getIssueDateStr()).isEqualTo("01-01-2000");
        assertThat(user.getBirthdayStr()).isEqualTo("02-02-2000");
        assertThat(user.getRoleIds()).isEqualTo("ROLE1,ROLE2");
    }
}
