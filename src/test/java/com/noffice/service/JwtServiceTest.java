package com.noffice.service;

import com.noffice.entity.User;
import com.noffice.repository.RolePermissionsRepository;
import io.jsonwebtoken.Claims;
import org.assertj.core.data.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock private RolePermissionsRepository rolePermissionsRepository;
    @InjectMocks private JwtService jwtService;

    private User user;
    private String token;
    private UUID userId ;

    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();
        org.springframework.test.util.ReflectionTestUtils.setField(jwtService, "secretKey",
                "c29tZV9sb25nX3NlY3JldF9rZXlfd2l0aF9hdF9sZWFzdF8zMl9ieXRlc19mb3JfdGVzdGluZw=="); // 32+ bytes

        user = new User();
        user.setId(userId);
        user.setUsername("testuser");
        user.setFullName("Test User");
        user.setEmail("test@example.com");
        user.setPhone("0901234567");
        user.setIsAdmin(0);
        user.setPartnerId(UUID.randomUUID());


    }

    @Test
    void generateToken_ShouldContainAllClaims() {
        when(rolePermissionsRepository.findPermissionsByRoleIds(userId))
                .thenReturn(List.of("READ", "WRITE"));
        token = jwtService.generateToken(user, "dept1", List.of("dept1", "dept2"));
        Claims claims = jwtService.extractAllClaims(token);

        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.get("fullName", String.class)).isEqualTo("Test User");
        assertThat(claims.get("role", String.class)).isEqualTo("user");
        assertThat(claims.get("isAdmin", String.class)).isEqualTo("false");
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.get("absoluteExp", Long.class)).isGreaterThan(System.currentTimeMillis());
    }

    @Test
    void generateToken_AdminUser_ShouldHaveAdminRole() {
        user.setIsAdmin(1);
        when(rolePermissionsRepository.findPermissionsByRoleIds(userId))
                .thenReturn(List.of("READ", "WRITE"));
        token = jwtService.generateToken(user, "dept1", List.of());

        Claims claims = jwtService.extractAllClaims(token);
        assertThat(claims.get("role", String.class)).isEqualTo("admin");
        assertThat(claims.get("isAdmin", String.class)).isEqualTo("true");
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        when(rolePermissionsRepository.findPermissionsByRoleIds(userId))
                .thenReturn(List.of("READ", "WRITE"));
        token = jwtService.generateToken(user, "dept1", List.of());
        assertThat(jwtService.extractUsername(token)).isEqualTo("testuser");
    }

    @Test
    void isValidateToken_ShouldReturnTrue_WhenValid() {
        when(rolePermissionsRepository.findPermissionsByRoleIds(userId))
                .thenReturn(List.of("READ", "WRITE"));
        token = jwtService.generateToken(user, "dept1", List.of());
        org.springframework.security.core.userdetails.UserDetails userDetails =
                org.springframework.security.core.userdetails.User.withUsername("testuser").password("pass").roles("USER").build();

        assertThat(jwtService.isValidateToken(token, userDetails)).isTrue();
    }

    @Test
    void validateWithTimeout_ShouldReturnTrue_Within12Hours() {
        when(rolePermissionsRepository.findPermissionsByRoleIds(userId))
                .thenReturn(List.of("READ", "WRITE"));
        token = jwtService.generateToken(user, "dept1", List.of());
        assertThat(jwtService.validateWithTimeout(token)).isTrue();
    }

    @Test
    void updateClaim_ShouldReturnNewTokenWithUpdatedClaim() {
        when(rolePermissionsRepository.findPermissionsByRoleIds(userId))
                .thenReturn(List.of("READ", "WRITE"));
        token = jwtService.generateToken(user, "dept1", List.of());

        Map<String, Object> result = jwtService.updateClaim(token, "lastActive", System.currentTimeMillis());

        String newToken = (String) result.get("token");
        Claims newClaims = jwtService.extractAllClaims(newToken);

        assertThat(newClaims.get("lastActive", Long.class)).isGreaterThan(0);
        assertThat(newClaims.getExpiration().getTime()).isGreaterThan(new Date().getTime() + 25 * 60 * 1000);
    }

    @Test
    void extractTokenFromHeader_ShouldReturnToken() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer my.jwt.token.here");

        assertThat(jwtService.extractTokenFromHeader(request)).isEqualTo("my.jwt.token.here");
    }

    @Test
    void generateWopiToken_ShouldCreateValidToken() {
        String wopiToken = jwtService.generateWopiToken(
                UUID.randomUUID(), UUID.randomUUID(), "view", "Nguyen Van A");

        Claims claims = jwtService.extractAllClaims(wopiToken);
        assertThat(claims.get("mode", String.class)).isEqualTo("view");
        assertThat(claims.getExpiration().getTime())
                .isCloseTo(System.currentTimeMillis() + 30 * 60 * 1000, Percentage.withPercentage(5000));
    }
}