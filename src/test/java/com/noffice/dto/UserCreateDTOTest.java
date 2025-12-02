package com.noffice.dto;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserCreateDTOTest {

    @Test
    void testNoArgsConstructor_DefaultValues() {
        UserCreateDTO dto = new UserCreateDTO();

        // default
        assertThat(dto.getIsAdmin()).isFalse();

        // others = null
        assertThat(dto.getUsername()).isNull();
        assertThat(dto.getEmail()).isNull();
    }

    @Test
    void testAllArgsConstructor_ShouldSetCorrectValues() {
        MockMultipartFile profile = new MockMultipartFile("profile", "p.jpg", "image/jpeg", "xx".getBytes());
        MockMultipartFile signature = new MockMultipartFile("signature", "s.jpg", "image/jpeg", "yy".getBytes());

        UUID partnerId = UUID.randomUUID();
        Date birthday = new Date();
        Date issue = new Date();

        UserCreateDTO dto = new UserCreateDTO(
                "user",
                "Full Name",
                "01234",
                "test@mail.com",
                "123456",
                "pass",
                partnerId,
                "U001",
                birthday,
                1,
                issue,
                "HN",
                profile,
                signature,
                true
        );

        assertThat(dto.getUsername()).isEqualTo("user");
        assertThat(dto.getFullname()).isEqualTo("Full Name");
        assertThat(dto.getPhone()).isEqualTo("01234");
        assertThat(dto.getEmail()).isEqualTo("test@mail.com");
        assertThat(dto.getIdentifyCode()).isEqualTo("123456");
        assertThat(dto.getPassword()).isEqualTo("pass");
        assertThat(dto.getPartnerId()).isEqualTo(partnerId);
        assertThat(dto.getUserCode()).isEqualTo("U001");
        assertThat(dto.getBirthDay()).isEqualTo(birthday);
        assertThat(dto.getGender()).isEqualTo(1);
        assertThat(dto.getIssueDate()).isEqualTo(issue);
        assertThat(dto.getIssuePlace()).isEqualTo("HN");
        assertThat(dto.getProfileImage()).isEqualTo(profile);
        assertThat(dto.getSignatureImage()).isEqualTo(signature);
        assertThat(dto.getIsAdmin()).isTrue();
    }

    @Test
    void testSetters() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("abc");
        dto.setEmail("a@b.com");
        dto.setIsAdmin(true);

        assertThat(dto.getUsername()).isEqualTo("abc");
        assertThat(dto.getEmail()).isEqualTo("a@b.com");
        assertThat(dto.getIsAdmin()).isTrue();
    }


    @Test
    void testToString() {
        UserCreateDTO dto = new UserCreateDTO();
        String result = dto.toString();

        assertThat(result).contains("UserCreateDTO");
    }
}
