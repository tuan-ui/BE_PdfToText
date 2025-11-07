package com.noffice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserGroupDTO {
    private UUID id;
    private String groupName;
    private String groupCode;
    private List<UUID> userIds;
}
