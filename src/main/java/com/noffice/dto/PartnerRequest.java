package com.noffice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartnerRequest {
    private UUID id;

    @NotBlank(message = "error.PartnerNameCannotBeBlank")
    private String partnerName;

    @Email(message = "error.EmailIsNotInTheCorrectFormat")
    private String email;

    @Pattern(
            regexp = "^$|^\\d{10}$",
            message = "error.PhoneNumberIsNotValid"
    )
    private String phone;

    private String partnerCode;

    @Pattern(regexp = "^$|^.{10,50}$", message = "error.TaxCodeMustBeAtLeastCharacters")
    private String taxCode;

    @URL(message = "error.WebsiteURLIsNotInTheCorrectFormat")
    private String website;

    private String address;

    private String fax;

    private String isActive;
    private String base64Image;
    private int page;
    private int size;
    private int offset;
    private String searchString;
    private Long version;
}