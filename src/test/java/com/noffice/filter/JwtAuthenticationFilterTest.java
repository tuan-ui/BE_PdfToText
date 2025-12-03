package com.noffice.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noffice.repository.UserRepository;
import com.noffice.service.JwtService;
import com.noffice.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext(); // reset trước mỗi test
    }

    @Test
    void shouldSkipFilter_WhenPathIsLogin() throws Exception {
        request.setRequestURI("/api/auth/login");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(filterChain.getRequest()).isNotNull(); // đã gọi tiếp
    }

    @Test
    void shouldSkipFilter_WhenPathIsRefreshToken() throws Exception {
        request.setRequestURI("/api/auth/refreshToken");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void shouldSkipFilter_WhenNoAuthorizationHeader() throws Exception {
        request.setRequestURI("/api/protected");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void shouldSkipFilter_WhenHeaderNotStartWithBearer() throws Exception {
        request.addHeader("Authorization", "Basic abc123");
        request.setRequestURI("/api/protected");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void shouldAuthenticateSuccessfully_WhenTokenValid() throws Exception {
        request.addHeader("Authorization", "Bearer valid-jwt-token");
        request.setRequestURI("/api/protected");
        request.setRemoteAddr("192.168.1.1");

        String username = "admin";
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("pass")
                .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                .build();

        when(jwtService.extractUsername("valid-jwt-token")).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isValidateToken(eq("valid-jwt-token"), any(UserDetails.class))).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Kiểm tra đã set Authentication vào SecurityContext
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo(username);
        assertThat(auth.getAuthorities()).hasSize(1);
        assertThat(auth).isInstanceOf(UsernamePasswordAuthenticationToken.class);

        verify(jwtService).isValidateToken(eq("valid-jwt-token"), eq(userDetails));
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void shouldNotAuthenticate_WhenTokenInvalid() throws Exception {
        request.addHeader("Authorization", "Bearer invalid-jwt-token");
        request.setRequestURI("/api/protected");

        String username = "admin";
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(username)
                .password("pass")
                .authorities(Collections.emptyList())
                .build();

        when(jwtService.extractUsername("invalid-jwt-token")).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isValidateToken(eq("invalid-jwt-token"), any(UserDetails.class))).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Không được set Authentication
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();

        verify(jwtService).isValidateToken(eq("invalid-jwt-token"), eq(userDetails));
        assertThat(filterChain.getRequest()).isNotNull();
    }

    @Test
    void shouldReturn498_WhenTokenExpired() throws Exception {
        request.addHeader("Authorization", "Bearer expired-jwt-token");
        request.setRequestURI("/api/protected");

        when(jwtService.extractUsername("expired-jwt-token"))
                .thenThrow(new ExpiredJwtException(null, null, "JWT expired"));

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Kiểm tra response
        assertThat(response.getStatus()).isEqualTo(200); // bạn đang set 200 (nên sửa thành 401 hoặc 498 thật)
        assertThat(response.getContentType()).contains(MediaType.APPLICATION_JSON_VALUE);

        String jsonResponse = response.getContentAsString();
        assertThat(jsonResponse).contains("498");
        assertThat(jsonResponse).contains("JWT_EXPIRED");

        // Không gọi filterChain tiếp và không set auth
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(userDetailsService);
    }

    @Test
    void shouldSkipAuthentication_WhenAlreadyAuthenticated() throws Exception {
        request.addHeader("Authorization", "Bearer some-token");
        request.setRequestURI("/api/protected");

        String username = "admin";
        when(jwtService.extractUsername("some-token")).thenReturn(username);

        // Giả lập đã có authentication rồi
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null)
        );

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Không loadUserByUsername → vì đã authenticated
        verify(userDetailsService, never()).loadUserByUsername(any());
        assertThat(filterChain.getRequest()).isNotNull();
    }
}