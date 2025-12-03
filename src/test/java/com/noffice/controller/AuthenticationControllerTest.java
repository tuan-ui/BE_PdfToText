package com.noffice.controller;

import com.github.cage.IGenerator;
import com.noffice.config.TestSecurityConfig;
import com.noffice.dto.*;
import com.noffice.entity.RefreshToken;
import com.noffice.reponse.AuthenticationResponse;
import com.noffice.repository.UserRepository;
import com.noffice.service.*;
import com.noffice.ultils.Constants;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noffice.entity.User;
import com.noffice.filter.JwtAuthenticationFilter;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.OutputStream;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.github.cage.Cage;

@WebMvcTest(AuthenticationController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private RefreshTokenService refreshTokenService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private LogService logService;
    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private AuthenticationResponse authenticationResponse;

    @Autowired
    private ObjectMapper objectMapper;

    private User mockUser;
    @MockBean
    private Cage cage;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");

        Authentication authentication = new TestingAuthenticationToken(
                mockUser,
                null,
                "ROLE_USER"
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        objectMapper.registerModule(new JavaTimeModule());
        List<AuthenticationResponse.UserRoleDTO> roles = Arrays.asList(
                new AuthenticationResponse.UserRoleDTO(
                        "role_user_dept_1",
                        "role_1",
                        "Administrator",
                        "ADMIN",
                        "dept_1",
                        "IT Department",
                        "READ,WRITE,DELETE"
                ),
                new AuthenticationResponse.UserRoleDTO(
                        "role_user_dept_2",
                        "role_2",
                        "User",
                        "USER",
                        "dept_2",
                        "HR Department",
                        "READ"
                )
        );

        authenticationResponse = new AuthenticationResponse(
                "mock-token",
                "mock-refresh-token",
                "test_user",
                roles,
                "Test User",
                "test@example.com",
                "0123456789",
                true,
                1,
                UUID.randomUUID(),
                UUID.randomUUID(),
                0,
                999999999L
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_Success() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("test_user");
        loginDTO.setPassword("123456");


        Mockito.when(authenticationService.authenticate(any(UserLoginDTO.class)))
                .thenReturn(authenticationResponse);

        Mockito.when(userRepository.findByUsername("test_user"))
                .thenReturn(Optional.of(mockUser));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object.username").value("test_user"));
    }

    @Test
    void logout_Success() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS));
    }

    @Test
    void refreshToken_Success() throws Exception {
        RefreshTokenReqDTO dto = new RefreshTokenReqDTO();
        dto.setRefreshToken("refresh-token");

        RefreshToken token = new RefreshToken();
        token.setRefreshToken("refresh-token");
        token.setUsername("testuser");

        Mockito.when(refreshTokenService.findByRefreshToken("refresh-token"))
                .thenReturn(Optional.of(token));

        Mockito.when(refreshTokenService.verifyExpiration(any()))
                .thenReturn(token);

        Mockito.when(userRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(mockUser));

        Mockito.when(jwtService.generateToken(any(User.class), any(), any()))
                .thenReturn("jwt-token");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/refreshToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object.accessToken").value("jwt-token"));
    }

    @Test
    void check2FA_Success() throws Exception {
        Mockito.when(authenticationService.check2FA("testuser")).thenReturn(1);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/check2FA")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object.twofaType").value(1))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void checkPassword_ReturnsTrue() throws Exception {
        UserLoginDTO dto = new UserLoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("123");

        Mockito.when(authenticationService.checkPassword(any())).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/checkPassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void generateSecret_Success() throws Exception {
        Mockito.when(authenticationService.generateSecret())
                .thenReturn("ABCSECRET");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/generateSecret"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("ABCSECRET"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void signIn_Success() throws Exception {
        UserLoginDTO loginDTO = new UserLoginDTO();
        loginDTO.setUsername("test_user");
        loginDTO.setPassword("123456");


        Mockito.when(authenticationService.authenticate(any(UserLoginDTO.class)))
                .thenReturn(authenticationResponse);


        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object.username").value("test_user"));
    }

    @Test
    void testValidateOtpAuthenticator_SuccessWithMap() throws Exception {
        Map<String, Object> otpResult = Map.of(
                "validated", true,
                "secret", "ABCDEF"
        );

        Mockito.when(authenticationService.validateOtpAuthenticator(any(), any(), any()))
                .thenReturn(otpResult);

        String body = """
                    {
                        "username":"testuser",
                        "otp":"123456",
                        "secret":"ABCDEF"
                    }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/validateOtpAuthenticator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xác thực OTP thành công"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object.validated").value(true));
    }

    @Test
    void testValidateOtpAuthenticator_SuccessWithAuthResponse() throws Exception {
        Mockito.when(authenticationService.validateOtpAuthenticator(any(), any(), any()))
                .thenReturn(authenticationResponse);

        String body = """
                    {
                        "username":"test_user",
                        "otp":"123456"
                    }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/validateOtpAuthenticator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Xác thực OTP thành công"))
                .andExpect(jsonPath("$.object.username").value("test_user"));
    }

    @Test
    void testValidateOtpAuthenticator_EmptyUsername() throws Exception {
        String body = """
                    {
                        "username":"",
                        "otp":"123456"
                    }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/validateOtpAuthenticator")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Username không được để trống"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void testUpdate2FA_Success() throws Exception {

        String body = """
                    {
                        "username":"testuser",
                        "twoFAType":1
                    }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/update2FA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS))
                .andExpect(jsonPath("$.object.twoFAType").value(1));
    }

    @Test
    void testUpdate2FA_Unauthenticated() throws Exception {
        SecurityContextHolder.clearContext();

        String body = """
                    {
                        "username":"testuser",
                        "twoFAType":1
                    }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/update2FA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Không có token hoặc phiên đăng nhập hợp lệ"));
    }

    @Test
    void testUpdate2FA_AccessDenied() throws Exception {

        String body = """
                    {
                        "username":"test_user",
                        "twoFAType":1
                    }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/update2FA")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Không có quyền cập nhật 2FA cho người dùng này"));
    }

    @Test
    void testGenerateCaptcha_Success() throws Exception {
        MockHttpSession session = new MockHttpSession();

        // mock token trả về
        IGenerator<String> tokenGenerator = Mockito.mock(IGenerator.class);
        Mockito.when(tokenGenerator.next()).thenReturn("ABC123");

        // mock vẽ ảnh (không crash)
        Mockito.doNothing().when(cage).draw(Mockito.anyString(), Mockito.any(OutputStream.class));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/generate-captcha")
                        .session(session))
                .andExpect(status().isOk());

        // kiểm tra session đã lưu token
        Map<String, Object> captchaInfo = (Map<String, Object>) session.getAttribute("captchaInfo");
        Integer requestCount = (Integer) captchaInfo.get("requestCount");
        assertEquals(1, requestCount.intValue());
    }

    /* =========================================================================
            TEST: verify CAPTCHA
       ========================================================================= */

    @Test
    void testVerifyCaptcha_Success() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("captcha", "ABC123");

        Map<String, Object> captchaInfo = new HashMap<>();
        captchaInfo.put("requestCount", 1);
        session.setAttribute("captchaInfo", captchaInfo);

        String body = """
                    {"captchaText": "ABC123"}
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/verify-captcha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }


    @Test
    void testChangePassword_Success() throws Exception {

        ChangePasswordDTO dto = new ChangePasswordDTO("old", UUID.fromString("411f2d97-96ed-4bd2-a480-cec1afba65e8"), "new");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/changePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS));
    }

    @Test
    void testGenerateQR_Success() throws Exception {

        Mockito.when(authenticationService.createQR("testuser", "SECRET"))
                .thenReturn("QR_IMAGE");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/generateQR")
                        .param("username", "testuser")
                        .param("secret", "SECRET"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("QR_IMAGE"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void testChangePasswordCheckOldPwd_Success() throws Exception {

        UserChangePwDTO dto = new UserChangePwDTO("oldpwd", "newpwd");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/changePasswordCheckOldPwd")
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS));
    }

    @Test
    void testChangePasswordAfterLogin_Success() throws Exception {
        ChangePasswordAfterLoginDTO dto = new ChangePasswordAfterLoginDTO(UUID.fromString("411f2d97-96ed-4bd2-a480-cec1afba65e8"), "old", "new");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/changePasswordAfterLogin")
                        .content(new ObjectMapper().writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS));
    }

    @Test
    void testPing_Success() throws Exception {
        Mockito.when(jwtService.extractTokenFromHeader(any())).thenReturn("token123");
        Mockito.when(jwtService.validateWithTimeout("token123")).thenReturn(true);
        Mockito.when(jwtService.updateClaim(any(), any(), any()))
                .thenReturn(Map.of("lastActive", 123456));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/auth/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("OK"))
                .andExpect(jsonPath("$.object.lastActive").value(123456));
    }

    @Test
    void testForgetPassword_Success() throws Exception {
        Mockito.when(authenticationService.processForgotPassword("user1", "0909"))
                .thenReturn("OTP_SENT");

        String body = """
                    { "username": "user1", "phone": "0909" }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/forgetPassword")
                        .content(body)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("OTP_SENT"))
                .andExpect(jsonPath("$.status").value(200));
    }


}

