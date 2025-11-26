package com.noffice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDTO {
	private UUID Id;
	private String roleName;
	private String roleCode;
	private String roleDescription;
    private Long version;
    private UUID partnerId;
    private String partnerName;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private UUID createBy;
    private UUID updateBy;
}
