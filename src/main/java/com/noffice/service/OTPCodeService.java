package com.noffice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.noffice.entity.OTPCode;
import com.noffice.repository.OTPCodeRepository;
@Service
@RequiredArgsConstructor
public class OTPCodeService {
	private final OTPCodeRepository otpCodeRepository;

	public void save(OTPCode log) {
		otpCodeRepository.save(log);
	}

	public String getOTP(String userId, int type_send) {
		return otpCodeRepository.getOTPCodebyTypeSend(type_send, userId);
	}
}
