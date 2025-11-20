package com.noffice.service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.noffice.entity.RefreshToken;
import com.noffice.repository.RefreshTokenRepository;
import com.noffice.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
	private final RefreshTokenRepository refreshTokenRepository;
	private final UserRepository userRepository;

	public RefreshToken createRefreshToken(String username) {
		if (userRepository.findByUsername(username).isEmpty()) {
			return null;
		}
		long sevenDaysInMilliseconds = 7 * 24 * 60 * 60 * 1000L;
		Date expiryDate = new Date(System.currentTimeMillis() + sevenDaysInMilliseconds);
		RefreshToken refreshToken = new RefreshToken();
		Optional<RefreshToken> optionalRefreshToken=refreshTokenRepository.findByUsername(username);
		if (optionalRefreshToken.isPresent()) {
			refreshToken=optionalRefreshToken.get();
		}
		refreshToken.setUsername(username);
		refreshToken.setRefreshToken(UUID.randomUUID().toString());
		refreshToken.setExpireTime(expiryDate);
		return refreshTokenRepository.save(refreshToken);
	}

	public Optional<RefreshToken> findByRefreshToken(String refreshToken) {
		return refreshTokenRepository.findByRefreshToken(refreshToken);
	}

	public RefreshToken verifyExpiration(RefreshToken token) {
		if (token.getExpireTime().compareTo(new Date()) < 0) {
			refreshTokenRepository.delete(token);
			throw new RuntimeException("Refresh token was expired. Please make a new signin request");
		}
		return token;
	}

}
