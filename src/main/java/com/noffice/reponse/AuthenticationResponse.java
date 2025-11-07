package com.noffice.reponse;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class AuthenticationResponse {
	 	private String token;
	    private String refreshToken;
	    private String username;
	    private List<UserRoleDTO> roles;
	    private String fullName;
	    private String email;
	    private String phone;
		private Boolean isActive;
	    private Integer twofaType;
	    private UUID partnerId;
	    private UUID userId;
	    private Integer isChangePassword;
		private long absoluteExp;
	    @Data
	    @NoArgsConstructor
	    @AllArgsConstructor
	    public static class UserRoleDTO {
	    	private String role_user_dept_id;
	        private String role_id;
	        private String role_name;
	        private String role_code;
	        private String department_id;
	        private String department_name;
	        private String permissions;
	    }

}
