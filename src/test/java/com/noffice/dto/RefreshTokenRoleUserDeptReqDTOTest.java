package com.noffice.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class RefreshTokenRoleUserDeptReqDTOTest {

    @Test
    void noArgsConstructor_ShouldCreateInstance() {
        RefreshTokenRoleUserDeptReqDTO dto = new RefreshTokenRoleUserDeptReqDTO();
        assertThat(dto.getRefreshToken()).isNull();
        assertThat(dto.getRoleUserDeptId()).isNull();
        assertThat(dto.getRoleUserDeptIds()).isNull();
    }

    @Test
    void allArgsConstructor_ShouldSetAllFields() {
        List<String> roleIds = Arrays.asList("role_dept_1", "role_dept_2");

        RefreshTokenRoleUserDeptReqDTO dto = new RefreshTokenRoleUserDeptReqDTO(
                "refresh-token-123",
                999L,
                roleIds
        );

        assertThat(dto.getRefreshToken()).isEqualTo("refresh-token-123");
        assertThat(dto.getRoleUserDeptId()).isEqualTo(999L);
        assertThat(dto.getRoleUserDeptIds()).containsExactly("role_dept_1", "role_dept_2");
    }

    @Test
    void settersAndGetters_ShouldWorkCorrectly() {
        RefreshTokenRoleUserDeptReqDTO dto = new RefreshTokenRoleUserDeptReqDTO();

        dto.setRefreshToken("my-refresh-token");
        dto.setRoleUserDeptId(123L);
        dto.setRoleUserDeptIds(Collections.singletonList("role_dept_x"));

        assertThat(dto.getRefreshToken()).isEqualTo("my-refresh-token");
        assertThat(dto.getRoleUserDeptId()).isEqualTo(123L);
        assertThat(dto.getRoleUserDeptIds()).containsOnly("role_dept_x");
    }

    @Test
    void equalsAndHashCode_ShouldBeBasedOnAllFields() {
        List<String> roles = Arrays.asList("r1", "r2");

        RefreshTokenRoleUserDeptReqDTO dto1 = new RefreshTokenRoleUserDeptReqDTO("token", 1L, roles);
        RefreshTokenRoleUserDeptReqDTO dto2 = new RefreshTokenRoleUserDeptReqDTO("token", 1L, roles);
        RefreshTokenRoleUserDeptReqDTO dto3 = new RefreshTokenRoleUserDeptReqDTO("other", 1L, roles);

        assertThat(dto1)
                .isEqualTo(dto2)
                .hasSameHashCodeAs(dto2)
                .isNotEqualTo(dto3);
    }

    @Test
    void toString_ShouldContainAllFieldValues() {
        RefreshTokenRoleUserDeptReqDTO dto = new RefreshTokenRoleUserDeptReqDTO(
                "abc123",
                555L,
                Arrays.asList("a", "b")
        );

        String str = dto.toString();
        assertThat(str)
                .contains("abc123")
                .contains("555")
                .contains("a")
                .contains("b");
    }
}