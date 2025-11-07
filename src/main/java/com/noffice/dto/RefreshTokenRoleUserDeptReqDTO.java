package com.noffice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRoleUserDeptReqDTO {
	private String refreshToken;
	private Long roleUserDeptId;
	List<String> roleUserDeptIds;
}
