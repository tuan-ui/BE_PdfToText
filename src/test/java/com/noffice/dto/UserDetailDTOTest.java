package com.noffice.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserDetailDTOTest {

    @Test
    void testNoArgsConstructor_ShouldHaveAllNullFields() {
        UserDetailDTO dto = new UserDetailDTO();

        assertThat(dto.getId()).isNull();
        assertThat(dto.getFullName()).isNull();
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getUsername()).isNull();
        assertThat(dto.getTwofaType()).isNull();
    }

    @Test
    void testAllArgsConstructor_ShouldAssignValues() {
        UserDetailDTO dto = new UserDetailDTO(
                "1",
                "User",
                "mail@test.com",
                "username",
                "U001",
                "01-01-2000",
                "0999",
                "123456",
                "02-02-2010",
                "HN",
                "Male",
                "Partner",
                "p.jpg",
                "s.jpg",
                1,
                "ADMIN",
                0
        );

        assertThat(dto.getId()).isEqualTo("1");
        assertThat(dto.getFullName()).isEqualTo("User");
        assertThat(dto.getEmail()).isEqualTo("mail@test.com");
        assertThat(dto.getUserCode()).isEqualTo("U001");
        assertThat(dto.getPhone()).isEqualTo("0999");
        assertThat(dto.getGender()).isEqualTo("Male");
        assertThat(dto.getRole()).isEqualTo("ADMIN");
        assertThat(dto.getStatus()).isZero();
    }

    @Test
    void testBuilder_ShouldBuildCorrectObject() {
        UserDetailDTO dto = UserDetailDTO.builder()
                .id("10")
                .fullName("Test User")
                .email("t@mail.com")
                .username("testuser")
                .role("USER")
                .status(1)
                .build();

        assertThat(dto.getId()).isEqualTo("10");
        assertThat(dto.getFullName()).isEqualTo("Test User");
        assertThat(dto.getEmail()).isEqualTo("t@mail.com");
        assertThat(dto.getUsername()).isEqualTo("testuser");
        assertThat(dto.getRole()).isEqualTo("USER");
        assertThat(dto.getStatus()).isEqualTo(1);
    }

    @Test
    void testEqualsAndHashCode() {
        UserDetailDTO dto1 = new UserDetailDTO("1","A","a","b","c","d","p","i","id","pl","g","pn","pi","si",1,"r",1);
        UserDetailDTO dto2 = new UserDetailDTO("1","A","a","b","c","d","p","i","id","pl","g","pn","pi","si",1,"r",1);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).hasSameHashCodeAs(dto2.hashCode());
    }

    @Test
    void testNotEquals() {
        UserDetailDTO dto1 = new UserDetailDTO();
        dto1.setId("A");

        UserDetailDTO dto2 = new UserDetailDTO();
        dto2.setId("B");

        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void testToString() {
        UserDetailDTO dto = new UserDetailDTO();
        String str = dto.toString();

        assertThat(str).contains("UserDetailDTO");
    }
}

