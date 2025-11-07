package com.noffice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refresh_token")
public class RefreshToken {

	@Id
	@Column(name = "id", nullable = false)
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "refresh_token", length = 60)
	private String refreshToken;

	@Column(name = "expire_time")
	private Date expireTime;

	@Column(name = "user_name", length = 50, unique = true)
	private String username;

}
