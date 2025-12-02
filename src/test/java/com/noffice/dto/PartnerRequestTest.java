package com.noffice.dto;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PartnerRequestTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private PartnerRequest createValidDTO() {
        return new PartnerRequest(
                UUID.randomUUID(),
                "Valid Partner",
                "example@mail.com",
                "0123456789",
                "P001",
                "12345678901",
                "https://example.com",
                "123 Street",
                "12345",
                "Y",
                "ABC",
                1,
                10,
                0,
                "search",
                1L
        );
    }

    @Test
    void testAllArgsConstructor_ShouldCreateObjectCorrectly() {
        UUID id = UUID.randomUUID();

        PartnerRequest dto = new PartnerRequest(
                id,
                "Partner",
                "email@test.com",
                "0123456789",
                "P01",
                "12345678901",
                "https://abc.com",
                "HCM",
                "1234",
                "Y",
                "img",
                1,
                20,
                0,
                "keyword",
                99L
        );

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getPartnerName()).isEqualTo("Partner");
        assertThat(dto.getEmail()).isEqualTo("email@test.com");
        assertThat(dto.getPhone()).isEqualTo("0123456789");
        assertThat(dto.getPartnerCode()).isEqualTo("P01");
        assertThat(dto.getTaxCode()).isEqualTo("12345678901");
        assertThat(dto.getWebsite()).isEqualTo("https://abc.com");
        assertThat(dto.getAddress()).isEqualTo("HCM");
        assertThat(dto.getFax()).isEqualTo("1234");
        assertThat(dto.getIsActive()).isEqualTo("Y");
        assertThat(dto.getBase64Image()).isEqualTo("img");
        assertThat(dto.getPage()).isEqualTo(1);
        assertThat(dto.getSize()).isEqualTo(20);
        assertThat(dto.getOffset()).isEqualTo(0);
        assertThat(dto.getSearchString()).isEqualTo("keyword");
        assertThat(dto.getVersion()).isEqualTo(99L);
    }

    @Test
    void testNoArgsConstructor_AndSetters_ShouldWork() {
        PartnerRequest dto = new PartnerRequest();

        dto.setPartnerName("Test");
        dto.setEmail("mail@test.com");

        assertThat(dto.getPartnerName()).isEqualTo("Test");
        assertThat(dto.getEmail()).isEqualTo("mail@test.com");
    }

    @Test
    void testEqualsAndHashCode() {
        PartnerRequest dto1 = createValidDTO();
        PartnerRequest dto2 = createValidDTO();

        // Không bằng vì UUID khác
        assertThat(dto1).isNotEqualTo(dto2);

        UUID id = UUID.randomUUID();

        dto1.setId(id);
        dto2.setId(id);

        dto1.setPartnerName("ABC");
        dto2.setPartnerName("ABC");

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void testToString_ShouldContainFields() {
        PartnerRequest dto = createValidDTO();

        String str = dto.toString();

        assertThat(str).contains("partnerName")
                .contains("email")
                .contains("website");
    }

    @Test
    void testValidDTO_ShouldHaveNoViolations() {
        PartnerRequest dto = createValidDTO();
        Set<ConstraintViolation<PartnerRequest>> violations = validator.validate(dto);
        assertThat(violations).isEmpty();
    }

    @Test
    void testPartnerName_Blank_ShouldFail() {
        PartnerRequest dto = createValidDTO();
        dto.setPartnerName("");

        var violations = validator.validate(dto);
        assertThat(violations)
                .anyMatch(v -> v.getMessage().equals("error.PartnerNameCannotBeBlank"));
    }

    @Test
    void testEmail_Invalid_ShouldFail() {
        PartnerRequest dto = createValidDTO();
        dto.setEmail("invalid");

        var violations = validator.validate(dto);
        assertThat(violations)
                .anyMatch(v -> v.getMessage().equals("error.EmailIsNotInTheCorrectFormat"));
    }

    @Test
    void testPhone_Invalid_ShouldFail() {
        PartnerRequest dto = createValidDTO();
        dto.setPhone("123");

        var violations = validator.validate(dto);
        assertThat(violations)
                .anyMatch(v -> v.getMessage().equals("error.PhoneNumberIsNotValid"));
    }

    @Test
    void testTaxCode_InvalidLength_ShouldFail() {
        PartnerRequest dto = createValidDTO();
        dto.setTaxCode("123"); // < 10

        var violations = validator.validate(dto);
        assertThat(violations)
                .anyMatch(v -> v.getMessage().equals("error.TaxCodeMustBeAtLeastCharacters"));
    }

    @Test
    void testWebsite_InvalidURL_ShouldFail() {
        PartnerRequest dto = createValidDTO();
        dto.setWebsite("illegal");

        var violations = validator.validate(dto);
        assertThat(violations)
                .anyMatch(v -> v.getMessage().equals("error.WebsiteURLIsNotInTheCorrectFormat"));
    }
}


