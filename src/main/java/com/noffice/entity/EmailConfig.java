package com.noffice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table(name = "smtp_config")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailConfig {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private int id;
	
	@Column(name = "host")
	private String host;
	
	@Column(name = "port")
	private String port;
	
	@Column(name = "username")
	private String username;
	
	@Column(name = "password")
	private String password;
	
	@Column(name = "from_email")
	private String fromEmail;
	
	@Column(name = "from_name")
	private String fromName;
	
	@Column(name = "subject")
    private String subject;
	
	@Column(name = "body")
    private String body;	
	
	@Column(name = "smtp_auth")
	private boolean smtpAuth;
	
	@Column(name = "smtp_secure")
	private boolean smtpSecure;

}
