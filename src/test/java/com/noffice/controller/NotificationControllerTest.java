package com.noffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noffice.config.TestSecurityConfig;
import com.noffice.entity.User;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.reponse.AuthenticationResponse;
import com.noffice.service.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

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

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setFullName("Test User");

        Authentication authentication = new TestingAuthenticationToken(
                mockUser,
                null,
                "ROLE_USER"
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        objectMapper.registerModule(new JavaTimeModule());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUser() {
        var auth = new org.springframework.security.core.userdetails.User(
                mockUser.getUsername(),
                "",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        var authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                mockUser, null, auth.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void toggleUpdateIsReadNotification_Success() throws Exception {
        // Given
        setAuthenticatedUser();
        Long notificationId = 123L;
        doNothing().when(notificationService).updateReadNotification(notificationId);

        // When & Then
        mockMvc.perform(get("/api/notifications/toggleUpdateIsReadNotification")
                        .param("notificationId", String.valueOf(notificationId))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Thành công"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object").isEmpty());

        verify(notificationService, times(1)).updateReadNotification(notificationId);
    }

    @Test
    void toggleUpdateIsReadNotification_NoAuth() throws Exception {
        // Given: Không có authentication
        SecurityContextHolder.clearContext();

        // When & Then
        mockMvc.perform(get("/api/notifications/toggleUpdateIsReadNotification")
                        .param("notificationId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Không có token hoặc phiên đăng nhập hợp lệ"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.object").isEmpty());

        verify(notificationService, never()).updateReadNotification(anyLong());
    }

    @Test
    void toggleUpdateIsReadNotification_AnonymousUser() throws Exception {
        // Given: Anonymous token
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key", "anonymous", Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        ));

        // When & Then
        mockMvc.perform(get("/api/notifications/toggleUpdateIsReadNotification")
                        .param("notificationId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Không có token hoặc phiên đăng nhập hợp lệ"))
                .andExpect(jsonPath("$.status").value(401));

        verify(notificationService, never()).updateReadNotification(anyLong());
    }

    @Test
    void toggleUpdateIsReadNotification_PrincipalNotUser() throws Exception {
        // Given: Principal là String (không phải User)
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "just-a-string", null, Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // When & Then
        mockMvc.perform(get("/api/notifications/toggleUpdateIsReadNotification")
                        .param("notificationId", "999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Thông tin người dùng không hợp lệ"))
                .andExpect(jsonPath("$.status").value(401));

        verify(notificationService, never()).updateReadNotification(anyLong());
    }

    @Test
    void toggleUpdateIsReadNotification_ServiceThrowsException() throws Exception {
        // Given
        setAuthenticatedUser();
        Long notificationId = 999L;
        doThrow(new RuntimeException("Thông báo không tồn tại"))
                .when(notificationService).updateReadNotification(notificationId);

        // When & Then
        mockMvc.perform(get("/api/notifications/toggleUpdateIsReadNotification")
                        .param("notificationId", String.valueOf(notificationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Lỗi hệ thống: Thông báo không tồn tại"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.object").isEmpty());

        verify(notificationService, times(1)).updateReadNotification(notificationId);
    }

    @Test
    void toggleUpdateIsReadNotification_MissingParam() throws Exception {
        setAuthenticatedUser();

        mockMvc.perform(get("/api/notifications/toggleUpdateIsReadNotification"))
                .andExpect(status().isBadRequest());
    }
}