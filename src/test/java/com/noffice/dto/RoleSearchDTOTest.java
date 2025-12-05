package com.noffice.dto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleSearchDTOTest {

    @Test
    void testNoArgsConstructor_ShouldSetDefaultValues() {
        RoleSearchDTO dto = new RoleSearchDTO();

        // Default values
        assertThat(dto.getPartnerId()).isEqualTo(-1L);
        assertThat(dto.getPage()).isZero();
        assertThat(dto.getSize()).isEqualTo(10);

        // Other fields should be null
        assertThat(dto.getSearchString()).isNull();
        assertThat(dto.getRoleName()).isNull();
        assertThat(dto.getRoleCode()).isNull();
        assertThat(dto.getRoleDescription()).isNull();
        assertThat(dto.getStatus()).isNull();
    }

    @Test
    void testAllArgsConstructor_ShouldAssignValuesCorrectly() {
        RoleSearchDTO dto = new RoleSearchDTO(
                "keyword",
                "ADMIN",
                "ROLE_ADMIN",
                "System admin",
                99L,
                true,
                2,
                20
        );

        assertThat(dto.getSearchString()).isEqualTo("keyword");
        assertThat(dto.getRoleName()).isEqualTo("ADMIN");
        assertThat(dto.getRoleCode()).isEqualTo("ROLE_ADMIN");
        assertThat(dto.getRoleDescription()).isEqualTo("System admin");
        assertThat(dto.getPartnerId()).isEqualTo(99L);
        assertThat(dto.getStatus()).isTrue();
        assertThat(dto.getPage()).isEqualTo(2);
        assertThat(dto.getSize()).isEqualTo(20);
    }

    @Test
    void testSetters_ShouldUpdateValues() {
        RoleSearchDTO dto = new RoleSearchDTO();

        dto.setSearchString("test");
        dto.setRoleName("User");
        dto.setRoleCode("ROLE_USER");
        dto.setRoleDescription("Người dùng");
        dto.setPartnerId(5L);
        dto.setStatus(false);
        dto.setPage(3);
        dto.setSize(50);

        assertThat(dto.getSearchString()).isEqualTo("test");
        assertThat(dto.getRoleName()).isEqualTo("User");
        assertThat(dto.getRoleCode()).isEqualTo("ROLE_USER");
        assertThat(dto.getRoleDescription()).isEqualTo("Người dùng");
        assertThat(dto.getPartnerId()).isEqualTo(5L);
        assertThat(dto.getStatus()).isFalse();
        assertThat(dto.getPage()).isEqualTo(3);
        assertThat(dto.getSize()).isEqualTo(50);
    }

    @Test
    void testEqualsAndHashCode() {
        RoleSearchDTO dto1 = new RoleSearchDTO(
                "A", "B", "C", "D",
                1L, true, 0, 10
        );

        RoleSearchDTO dto2 = new RoleSearchDTO(
                "A", "B", "C", "D",
                1L, true, 0, 10
        );

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).hasSameHashCodeAs(dto2.hashCode());
    }

    @Test
    void testNotEquals() {
        RoleSearchDTO dto1 = new RoleSearchDTO();
        RoleSearchDTO dto2 = new RoleSearchDTO();

        dto1.setRoleName("A");
        dto2.setRoleName("B");

        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void testToString_ShouldContainFieldNames() {
        RoleSearchDTO dto = new RoleSearchDTO();
        String str = dto.toString();

        assertThat(str)
                .contains("searchString")
                .contains("roleName")
                .contains("roleCode")
                .contains("roleDescription")
                .contains("partnerId")
                .contains("status")
                .contains("page")
                .contains("size");
    }
}

