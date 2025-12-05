package com.noffice.service;

import com.noffice.entity.RefreshToken;
import com.noffice.entity.User;
import com.noffice.repository.RefreshTokenRepository;
import com.noffice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private RefreshTokenService refreshTokenService;

    private static final String USERNAME = "testuser@example.com";

    @Test
    void createRefreshToken_UserNotFound_ShouldReturnNull() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        RefreshToken result = refreshTokenService.createRefreshToken(USERNAME);

        assertThat(result).isNull();
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void createRefreshToken_NewUser_ShouldCreateNewToken() {
        User user = new User();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        RefreshToken savedToken = new RefreshToken();
        savedToken.setRefreshToken("generated-uuid");
        savedToken.setUsername(USERNAME);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(savedToken);

        RefreshToken result = refreshTokenService.createRefreshToken(USERNAME);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(USERNAME);
        assertThat(result.getRefreshToken()).isNotBlank();
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void createRefreshToken_ExistingToken_ShouldUpdateExistingToken() {
        User user = new User();
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        RefreshToken existingToken = new RefreshToken();
        existingToken.setUsername(USERNAME);
        existingToken.setRefreshToken("old-token");
        existingToken.setExpireTime(new Date());

        when(refreshTokenRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        RefreshToken result = refreshTokenService.createRefreshToken(USERNAME);

        assertThat(result).isSameAs(existingToken);
        assertThat(result.getRefreshToken()).isNotEqualTo("old-token"); // đã được regenerate
        assertThat(result.getExpireTime()).isAfter(new Date());
        verify(refreshTokenRepository, times(1)).save(existingToken);
    }

    @Test
    void findByRefreshToken_ShouldReturnTokenIfExists() {
        RefreshToken token = new RefreshToken();
        token.setRefreshToken("valid-token");
        when(refreshTokenRepository.findByRefreshToken("valid-token")).thenReturn(Optional.of(token));

        Optional<RefreshToken> result = refreshTokenService.findByRefreshToken("valid-token");

        assertThat(result).isPresent();
        assertThat(result.get().getRefreshToken()).isEqualTo("valid-token");
    }

    @Test
    void findByRefreshToken_NotFound_ShouldReturnEmpty() {
        when(refreshTokenRepository.findByRefreshToken("invalid")).thenReturn(Optional.empty());

        Optional<RefreshToken> result = refreshTokenService.findByRefreshToken("invalid");

        assertThat(result).isEmpty();
    }

    @Test
    void verifyExpiration_ValidToken_ShouldReturnToken() {
        RefreshToken token = new RefreshToken();
        token.setExpireTime(new Date(System.currentTimeMillis() + 100000)); // tương lai

        RefreshToken result = refreshTokenService.verifyExpiration(token);

        assertThat(result).isSameAs(token);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    void verifyExpiration_ExpiredToken_ShouldDeleteAndThrowException() {
        RefreshToken token = new RefreshToken();
        token.setExpireTime(new Date(System.currentTimeMillis() - 100000)); // quá khứ

        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(token))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Refresh token was expired. Please make a new signin request");

        verify(refreshTokenRepository, times(1)).delete(token);
    }
}