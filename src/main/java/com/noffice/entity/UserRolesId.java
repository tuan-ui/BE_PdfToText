package com.noffice.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class UserRolesId implements Serializable {

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "role_id")
    private UUID roleId;
}
