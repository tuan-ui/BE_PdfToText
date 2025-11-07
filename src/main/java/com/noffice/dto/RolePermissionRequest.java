package com.noffice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissionRequest {
    private UUID roleId;
    private List<UUID> checkedKeys;
    private List<UUID> checkedHalfKeys;
}
