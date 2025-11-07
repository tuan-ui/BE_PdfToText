package com.noffice.entity;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static org.apache.catalina.realm.UserDatabaseRealm.getRoles;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {

    @Column(name = "user_name", nullable = false, length = 100)
    private String username;

    @Column(name = "user_code")
    private String userCode;

    @Column(name = "identify_code", length = 20)
    private String identifyCode;

    @Column(name = "signature_image")
    private String signatureImage;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "issue_place")
    private String issuePlace;

    @Column(name = "issue_date")
    private Date issueDate;

    @Column(name = "is_admin")
    private Integer isAdmin;

    @Column(name = "gender")
    private Boolean gender;

    @Column(name = "full_name", length = 255)
    private String fullName;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "birthday")
    private Date birthday;
    
    @Column(name = "is_change_password")
    private Integer isChangePassword;
    
    @Column(name = "twofa_type")
    private Integer twofaType;

    @Transient
    private String issueDateStr;
    @Transient
    private String birthdayStr;
    @Transient
    private String roleIds;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Nếu bạn có cờ isAdmin:
        if (isAdmin != null && isAdmin == 1) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return authorities;
    }

}
