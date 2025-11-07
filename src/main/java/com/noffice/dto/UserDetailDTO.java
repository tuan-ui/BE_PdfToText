package com.noffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailDTO {
    private String id;
    private String fullName;
    private String email;
    private String username;
    private String userCode;
    private String birthDay;
    private String phone;
    private String identifyCode;
    private String issueDate;
    private String issuePlace;
    private String gender;
    private String partnerName;
    private String profileImage;
    private String signatureImage;
    private Integer twofaType;
    private String role;
    private Integer status;
}