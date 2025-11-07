package com.noffice.entity;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PERMISSIONS_ROLES")
public class PermissionRole {

    @EmbeddedId
    private PermissionRoleId id;
}