package com.noffice.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "otp_code")
public class OTPCode  {

	@Id
	@Column(name = "userid", nullable = false)
	private String userId;

	@Column(name = "code", length = 10, nullable = false)
	@JsonIgnore
	private String code;
	
	@Column(name = "type_send")
	private int typeSend;

	@Column(name = "created_at")
	@JsonIgnore
	private Date createdAt;
}
