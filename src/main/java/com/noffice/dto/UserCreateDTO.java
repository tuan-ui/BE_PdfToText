package com.noffice.dto;


import java.util.Date;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.noffice.entity.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserCreateDTO {
    private String username;
    private String fullname;
    private String phone;
    private String email;
    private String identifyCode;
    private String password;
    private UUID partnerId;
    private String userCode;
    private Date birthDay;
    private Integer gender;
    private Date issueDate;
    private String issuePlace;
    private MultipartFile profileImage;
    private MultipartFile signatureImage;
    private Boolean isAdmin = false;

}
