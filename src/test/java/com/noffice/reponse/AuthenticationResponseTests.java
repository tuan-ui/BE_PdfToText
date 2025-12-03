package com.noffice.reponse;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthenticationResponseTests {

    @Test
    void test_AuthenticationResponse_AllArgsConstructor_And_Getters() {
        List<AuthenticationResponse.UserRoleDTO> roles = List.of(
                new AuthenticationResponse.UserRoleDTO("1", "R1", "Admin", "ADMIN", "D1", "IT", "READ,WRITE")
        );
        UUID id = UUID.randomUUID();

        AuthenticationResponse response = new AuthenticationResponse(
                "jwt-token-123",
                "refresh-abc",
                "admin",
                roles,
                "Nguyen Van A",
                "admin@example.com",
                "0901234567",
                true,
                1,
                id,
                id,
                0,
                1735920000000L
        );

        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getRefreshToken()).isEqualTo("refresh-abc");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getFullName()).isEqualTo("Nguyen Van A");
        assertThat(response.getEmail()).isEqualTo("admin@example.com");
        assertThat(response.getPhone()).isEqualTo("0901234567");
        assertThat(response.getTwofaType()).isEqualTo(1);
        assertThat(response.getPartnerId()).isEqualTo(id);
        assertThat(response.getUserId()).isEqualTo(id);
        assertThat(response.getIsChangePassword()).isEqualTo(0);
        assertThat(response.getIsActive()).isTrue();
        assertThat(response.getRoles()).hasSize(1);
        assertThat(response.getRoles().get(0).getRole_name()).isEqualTo("Admin");
        assertThat(response.getAbsoluteExp()).isEqualTo(1735920000000L);
    }

    @Test
    void test_UserRoleDTO_Lombok_Annotations() {
        AuthenticationResponse.UserRoleDTO dto = new AuthenticationResponse.UserRoleDTO();
        dto.setRole_id("10");
        dto.setRole_name("Manager");
        dto.setPermissions("READ");
        dto.setRole_user_dept_id("10");
        dto.setRole_code("Manager");
        dto.setDepartment_id("READ");
        dto.setDepartment_name("READ");

        assertThat(dto.getRole_id()).isEqualTo("10");
        assertThat(dto.getRole_name()).isEqualTo("Manager");
        assertThat(dto.getPermissions()).isEqualTo("READ");
        assertThat(dto.getRole_user_dept_id()).isEqualTo("10");
        assertThat(dto.getRole_code()).isEqualTo("Manager");
        assertThat(dto.getDepartment_id()).isEqualTo("READ");
        assertThat(dto.getDepartment_name()).isEqualTo("READ");

        AuthenticationResponse.UserRoleDTO dto2 = new AuthenticationResponse.UserRoleDTO(
                "1", "10", "Manager", "MGR", "D2", "HR", "READ"
        );

        assertThat(dto2).isNotNull();
    }
}