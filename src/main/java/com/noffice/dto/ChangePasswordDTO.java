package com.noffice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordDTO {
	private String staffPassword;
	private UUID userId;
	private String userNewPassword;

}
