package com.noffice.service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.crypto.SecretKey;

import com.noffice.repository.RolePermissionsRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.noffice.entity.User;
import com.noffice.repository.ConfigRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
public class JwtService {
	@Value("${jwt.secret}")
	private String SECRET_KEY;
	private final ConfigRepository configRepository;
	private static ConfigRepository configRepositoryInstance;
    private final RolePermissionsRepository rolePermissionsRepository;
	private final long IDLE_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(30);
	@PostConstruct
	public void init() {
		configRepositoryInstance = configRepository;
	}

	public String generateToken(User user, String roleUserDeptId, List<String> roleUserDeptIds) {
	    String expired = configRepositoryInstance.findByKey("expired").getValue();
	    String role = user.getIsAdmin() == 1 ? "admin" : "user";
		long now = System.currentTimeMillis();
		long idleExp = now + IDLE_TIMEOUT_MS;
		long absoluteExp = now + 12 * 60 * 60 * 1000; // 12 giờ

	    JwtBuilder builder = Jwts.builder()
	        .subject(user.getUsername())
	        .claim("partner_id", user.getPartnerId())
	        .claim("fullName", user.getFullName())
	        .claim("email", user.getEmail())
	        .claim("phone", user.getPhone())
	        .claim("role", role)
	        .claim("user_id", user.getId())
			.claim("isAdmin", user.getIsAdmin() == 1 ? "true" : "false")
			.claim("permissions", rolePermissionsRepository.findPermissionsByRoleIds(user.getId()))
			.claim("loginAt", now)
			.claim("lastActive", now)
			.claim("absoluteExp", absoluteExp)
			.issuedAt(new Date(now))
			.expiration(new Date(idleExp))
			.signWith(getSignKey());

	    return builder.compact();
	}

	private SecretKey getSignKey() {
		byte[] secretBytes = Decoders.BASE64URL.decode(SECRET_KEY);
		return Keys.hmacShaKeyFor(secretBytes);
	}

	public Claims extractAllClaims(String token) {
		return Jwts.parser().verifyWith(getSignKey()).build().parseSignedClaims(token).getPayload();

	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public boolean isValidateToken(String token, UserDetails user) {
		String username = extractUsername(token);
		return (username.equals(user.getUsername())) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}
	public String extractRoleUserDeptId(String token) {
	    return extractClaim(token, claims -> claims.get("role_user_dept_id", String.class));
	}

	public String extractTokenFromHeader(jakarta.servlet.http.HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		if (header != null && header.startsWith("Bearer ")) {
			return header.substring(7);
		}
		return null;
	}

	public <T> T getClaim(String token, String key, Class<T> clazz) {
		Claims claims = extractAllClaims(token);
		return claims.get(key, clazz);
	}


	public boolean validateWithTimeout(String token) {
		try {
			Claims claims = extractAllClaims(token);
			long now = System.currentTimeMillis();
			long absoluteExp = claims.get("absoluteExp", Long.class);

			// Absolute timeout 12h
            return now <= absoluteExp; // absolute timeout
        } catch (Exception e) {
			return false;
		}
	}

	public Map<String, Object> updateClaim(String token, String key, Object value) {
		Claims oldClaims = extractAllClaims(token);
		long now = System.currentTimeMillis();
		long newIdleExp = now + IDLE_TIMEOUT_MS;

		String newToken = Jwts.builder()
				.claims(oldClaims)
				.claim(key, value)
				.issuedAt(new Date(now))
				.expiration(new Date(newIdleExp))
				.signWith(getSignKey())
				.compact();

		Map<String, Object> result = new HashMap<>();
		result.put("token", newToken);
		result.put("idleExpire", newIdleExp);
		return result;
	}

	public String generateWopiToken(UUID userId, UUID fileId, String mode, String fullName) {
		long now = System.currentTimeMillis();
		return Jwts.builder()
				.claim("user_id", userId.toString())
				.claim("fileId", fileId.toString())
				.claim("mode", mode)
				.claim("fullName", fullName)
				.issuedAt(new Date(now))
				.expiration(new Date(now + 30*60*1000))
				.signWith(getSignKey())
				.compact();
	}


}
