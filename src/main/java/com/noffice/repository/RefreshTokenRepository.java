package com.noffice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.noffice.entity.RefreshToken;

public interface RefreshTokenRepository  extends JpaRepository<RefreshToken, Integer>{
	Optional<RefreshToken> findByRefreshToken(String freshToken);
	Optional<RefreshToken> findByUsername(String username);
}
