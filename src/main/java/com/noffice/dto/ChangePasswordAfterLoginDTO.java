package com.noffice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordAfterLoginDTO {
	private UUID userId;
	private String oldPassword;
	private String newPassword;
}
