package com.noffice.entity;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PermissionRoleId implements Serializable {

    @Column(name = "permission_id")
    private UUID permissionId;

    @Column(name = "role_id")
    private UUID roleId;

    @Column(name = "is_half")
    private Boolean isHalf;
}
