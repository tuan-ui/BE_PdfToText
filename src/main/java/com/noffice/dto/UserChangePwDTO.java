package com.noffice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserChangePwDTO {
	private String oldPassword;
	private String newPassword;
}
