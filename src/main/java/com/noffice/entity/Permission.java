package com.noffice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permission")
public class Permission {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "permission_name")
    private String permissionName;

    @Column(name = "permission_code")
    private String permissionCode;

    @Column(name = "permission_url")
    private String permissionUrl;

    @Column(name = "permission_parent")
    private UUID permissionParent;

    @Column(name = "position")
    private Integer position;

    @Column(name = "is_admin")
    private Boolean isAdmin;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Column(name = "is_menus")
    private Boolean isMenus;
}
