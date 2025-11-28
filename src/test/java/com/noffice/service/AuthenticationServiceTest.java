package com.noffice.service;

import com.noffice.dto.*;
import com.noffice.entity.Partners;
import com.noffice.entity.RefreshToken;
import com.noffice.entity.User;
import com.noffice.reponse.AuthenticationResponse;
import com.noffice.repository.PartnerRepository;
import com.noffice.repository.UserRepository;
import com.noffice.ultils.Constants;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import jakarta.mail.internet.MimeMessage;

import org.apache.commons.codec.binary.Base64;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PartnerRepository partnerRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private LogService logService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User mockUser;
    private Partners mockPartner;
    private UUID partnerId;
    private UUID mockId;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ZxingPngQrGenerator mockedQrGenerator;
    private static final Base64 mockBase64 = mock(Base64.class);

    @BeforeEach
    void setUp() {
        partnerId = UUID.randomUUID();
        mockId = UUID.randomUUID();
        mockUser = new User();
        mockUser.setId(mockId);
        mockUser.setUsername("admin");
        mockUser.setPassword("$2a$10$hashedpassword");
        mockUser.setFullName("Administrator");
        mockUser.setEmail("admin@natcom.vn");
        mockUser.setPhone("0901234567");
        mockUser.setIsActive(true);
        mockUser.setTwofaType(0);
        mockUser.setPartnerId(partnerId);
        mockUser.setIsAdmin(1);
        mockUser.setIsChangePassword(0);

        mockPartner = new Partners();
        mockPartner.setId(partnerId);
        mockPartner.setIsActive(true);
    }

    @Test
    void authenticate_Success_Admin() {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("password123");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(mockUser));
        when(partnerRepository.getPartnerById(eq(partnerId))).thenReturn(mockPartner);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateToken(any(User.class), isNull(), anyList())).thenReturn("mock-jwt-token");
        when(refreshTokenService.createRefreshToken("admin")).thenReturn(new RefreshToken(1, "refreshToken", new Date(), "admin"));
        when(jwtService.getClaim(anyString(), eq("absoluteExp"), eq(Long.class))).thenReturn(1738000000000L);

        AuthenticationResponse response = authenticationService.authenticate(loginDTO);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("admin", response.getUsername());
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("Admin tổng", response.getRoles().get(0).getRole_name());
    }

    @Test
    void authenticate_Fail_WrongPassword() {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("wrongpass");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongpass", mockUser.getPassword())).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate(loginDTO));

        assertEquals("Tên đăng nhập hoặc mật khẩu không đúng!", exception.getMessage());
    }

    @Test
    void authenticate_Fail_PartnerInactive() {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("password123");

        mockPartner.setIsActive(false);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(mockUser));
        when(partnerRepository.getPartnerById(eq(partnerId))).thenReturn(mockPartner);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.authenticate(loginDTO));

        assertEquals("Đơn vị đang bị khóa!", exception.getMessage());
    }

    @Test
    void changePassword_Success() {
        ChangePasswordDTO dto = new ChangePasswordDTO();
        dto.setStaffPassword("currentPass123");
        dto.setUserId(mockId);
        dto.setUserNewPassword("NewStrongPass123");

        User currentUser = new User();
        currentUser.setUsername("admin");
        currentUser.setPassword("$2a$10$hashedpassword");

        User targetUser = new User();
        targetUser.setId(mockId);
        targetUser.setUsername("user1");

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(currentUser));
        when(userRepository.getUserByUserId(eq(mockId))).thenReturn(targetUser);
        when(passwordEncoder.matches("currentPass123", currentUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("NewStrongPass123")).thenReturn("encodedNewPass");

        assertDoesNotThrow(() -> authenticationService.changePassword(dto, currentUser));

        assertEquals("encodedNewPass", targetUser.getPassword());
        assertEquals(1, targetUser.getIsChangePassword());
        verify(userRepository).save(targetUser);
    }

    @Test
    void processForgotPassword_Success() throws Exception {
        when(userRepository.findByUsernameandPhone("user1", "0901234567")).thenReturn(mockUser);
        when(mailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        // Mock gửi mail không ném lỗi
        doNothing().when(mailSender).send(any(MimeMessage.class));

        String result = authenticationService.processForgotPassword("user1", "0901234567");

        assertNull(result); // null nghĩa là thành công
        assertNotEquals("$2a$10$hashedpassword", mockUser.getPassword()); // mật khẩu đã thay đổi
        verify(userRepository).save(mockUser);
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void processForgotPassword_UserNotFound() throws Exception {
        when(userRepository.findByUsernameandPhone("unknown", "0909999999")).thenReturn(null);

        String result = authenticationService.processForgotPassword("unknown", "0909999999");

        assertEquals("authentication.NoUserFound", result);
    }

    @Test
    void validateOtpAuthenticator_InitialSetup_Success() {
        String secret = "JBSWY3DPEHPK3PXP";
        String code = "123456";

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(mockUser));

        try (MockedStatic<dev.samstevens.totp.code.DefaultCodeVerifier> verifierMock = mockStatic(dev.samstevens.totp.code.DefaultCodeVerifier.class)) {
            assertDoesNotThrow(() -> authenticationService.validateOtpAuthenticator(secret, "123456", "admin"));
        }
    }

    @Test
    void changePasswordAfterLogin_Success() {
        ChangePasswordAfterLoginDTO dto = new ChangePasswordAfterLoginDTO();
        dto.setUserId(mockId);
        dto.setOldPassword("oldPass123");
        dto.setNewPassword("NewPass123456");

        when(userRepository.getUserByUserId(eq(mockId))).thenReturn(mockUser);
        when(passwordEncoder.matches("oldPass123", mockUser.getPassword())).thenReturn(true);

        assertDoesNotThrow(() -> authenticationService.changePasswordAfterLogin(dto));

        verify(userRepository).save(mockUser);
        assertEquals(0, mockUser.getIsChangePassword());
    }


    @Test
    void createQR_Success_ReturnsDataUri() throws Exception {
        String username = "admin123";
        String secret = "JBSWY3DPEHPK3PXP";

        byte[] fakePngBytes = new byte[]{1, 2, 3, 4};

        when(mockBase64.encode(fakePngBytes)).thenReturn("AQIDBA==".getBytes());

        // Mock constructor của ZxingPngQrGenerator
        try (MockedConstruction<ZxingPngQrGenerator> mockedConstruction =
                     mockConstruction(ZxingPngQrGenerator.class, (mock, context) -> {
                         // Khi new ZxingPngQrGenerator() → trả về mockedQrGenerator
                         when(mock.generate(any(QrData.class))).thenReturn(fakePngBytes);
                         when(mock.getImageMimeType()).thenReturn("image/png");
                     })) {

            // Act
            String dataUri = authenticationService.createQR(username, secret);

            // Assert
            assertNotNull(dataUri);
            assertTrue(dataUri.startsWith("data:image/png;base64,"));
            assertEquals("data:image/png;base64,AQIDBA==", dataUri);
        }
    }

    @Test
    void createQR_Exception_ReturnsNull() {
        String username = "admin";
        String secret = "INVALIDSECRET";

        // Làm cho generator.generate() ném exception
        try (MockedConstruction<ZxingPngQrGenerator> mocked = mockConstruction(ZxingPngQrGenerator.class,
                (mock, ctx) -> when(mock.generate(any(QrData.class))).thenThrow(new RuntimeException("QR error")))) {

            String result = authenticationService.createQR(username, secret);

            assertNull(result);
        }
    }

    @Test
    void getDataUriForImage_ValidInput_ReturnsCorrectDataUri() {
        byte[] imageBytes = new byte[]{71, 73, 70, 56, 57, 97}; // GIF89a
        String mimeType = "image/gif";

        when(mockBase64.encode(imageBytes)).thenReturn("R0lGODlh".getBytes());

        String result = authenticationService.getDataUriForImage(imageBytes, mimeType);

        assertEquals("data:image/gif;base64,R0lGODlh", result);
    }

    @Test
    void update2FA_Enable2FA_Success() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(mockUser));

        authenticationService.update2FA("admin", 3);

        assertEquals(3, mockUser.getTwofaType());
        verify(userRepository).save(mockUser);
    }

    @Test
    void update2FA_Disable2FA_Success() {
        mockUser.setTwofaType(3);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(mockUser));

        authenticationService.update2FA("admin", 0);

        assertEquals(0, mockUser.getTwofaType());
        verify(userRepository).save(mockUser);
    }

    @Test
    void update2FA_UserNotFound_DoNothing() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        authenticationService.update2FA("unknown", 3);

        verify(userRepository, never()).save(any());
    }

    @Test
    void check2FA_Enabled_ReturnsCorrectType() {
        mockUser.setTwofaType(3); // Google Authenticator
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(mockUser));

        int result = authenticationService.check2FA("user1");

        assertEquals(3, result);
    }

    @Test
    void check2FA_NotEnabled_ReturnsZero() {
        mockUser.setTwofaType(null); // hoặc 0
        when(userRepository.findByUsername("user2")).thenReturn(Optional.of(mockUser));

        int result = authenticationService.check2FA("user2");

        assertEquals(0, result);
    }

    @Test
    void check2FA_UserNotFound_ReturnsZero() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        int result = authenticationService.check2FA("ghost");

        assertEquals(0, result);
    }


    @Test
    void changePasswordCheckOldPwd_Success() {
        String username = "admin";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("oldPass123", mockUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("NewStrongPass123")).thenReturn("encodedNewPass");

        UserChangePwDTO dto = new UserChangePwDTO();
        dto.setOldPassword("oldPass123");
        dto.setNewPassword("NewStrongPass123");

        assertDoesNotThrow(() -> authenticationService.changePasswordCheckOldPwd(dto, username));

        assertEquals("encodedNewPass", mockUser.getPassword());
        assertEquals(Constants.isActive.ACTIVE, mockUser.getIsChangePassword());
        verify(userRepository).save(mockUser);
    }

    @Test
    void changePasswordCheckOldPwd_WrongOldPassword() {
        String username = "admin";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("wrongOldPass", mockUser.getPassword())).thenReturn(false);

        UserChangePwDTO dto = new UserChangePwDTO();
        dto.setOldPassword("wrongOldPass");
        dto.setNewPassword("NewPass123");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.changePasswordCheckOldPwd(dto, username));

        assertEquals("error.WrongPassword", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordCheckOldPwd_NewPasswordTooShort() {
        String username = "admin";
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        UserChangePwDTO dto = new UserChangePwDTO();
        dto.setOldPassword("correctOld");
        dto.setNewPassword("short");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.changePasswordCheckOldPwd(dto, username));

        assertEquals("error.atLeastCharacters", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordCheckOldPwd_UserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        UserChangePwDTO dto = new UserChangePwDTO();
        dto.setOldPassword("anything");
        dto.setNewPassword("NewPass123");

        UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> authenticationService.changePasswordCheckOldPwd(dto, "unknown"));

        assertTrue(ex.getMessage().contains("Không tìm thấy người dùng"));
    }
}