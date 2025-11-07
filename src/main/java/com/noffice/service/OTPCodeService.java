package com.noffice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.noffice.entity.OTPCode;
import com.noffice.repository.OTPCodeRepository;
@Service
public class OTPCodeService {
	@Autowired
	private OTPCodeRepository otpCodeRepository;

	public void save(OTPCode log) {
		otpCodeRepository.save(log);
	}

	public String getOTP(String userId, int type_send) {
		return otpCodeRepository.getOTPCodebyTypeSend(type_send, userId);
	}
}
