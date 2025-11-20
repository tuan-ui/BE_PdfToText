package com.noffice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.noffice.entity.OTPCode;

public interface OTPCodeRepository extends JpaRepository<OTPCode, String> {

	// count log by action and today
	@Query(value = "SELECT code FROM otp_code WHERE type_send = ?1 AND userid = ?2 "
	        + "AND created_at >= NOW() - INTERVAL '120 seconds' "
	        + "ORDER BY created_at DESC "
	        + "LIMIT 1", nativeQuery = true)
	String getOTPCodebyTypeSend(int typeSend, String userId);

}
