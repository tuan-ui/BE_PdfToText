package com.noffice.config;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.service.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final UserDetailsServiceImpl userDetailsService;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomAccessDeniedHandler accessDeniedHandler;

	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

//	@Bean
//	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//
//		http.authorizeHttpRequests(configure -> configure
//				.requestMatchers("/api/auth/login/**", "/api/auth/loginNoReturnToken/**", "/api/auth/sendOtp2FA/**",
//						"/api/auth/validateOtp/**", "/api/auth/sendSms2FA/**", "/api/auth/validateSms2FA/**",
//						"/api/auth/checkUsernameAndPassword/**", "/api/auth/changePassword/**",
//						"/api/auth/validateSmsResetPassword/**", "/api/auth/generateQR/**", "/api/auth/refreshToken/**",
//						"/api/auth/checkRefreshToken/**", "/api/auth/checkPattern/**", "/api/auth/signin/**")
//				.permitAll().requestMatchers("/api/user/getByUsername/**", "/api/user/updateSelf/**")
//				.hasAnyRole("ADMIN", "USER")
//				.requestMatchers("/api/user/**", "/api/role/**", "/api/log/**")
//				.hasRole("ADMIN").anyRequest().authenticated()).userDetailsService(userDetailsService)
//				.exceptionHandling(e -> e.accessDeniedHandler(accessDeniedHandler)
//						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
//				.sessionManagement(
//						sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//		http.csrf(AbstractHttpConfigurer::disable);
//		return http.build();
//	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(configure -> configure.anyRequest().permitAll())
				.userDetailsService(userDetailsService)
				.exceptionHandling(e -> e.accessDeniedHandler(accessDeniedHandler)
						.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
				.sessionManagement(
						sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		// Safe to disable CSRF: this is a stateless REST API using JWT (no cookies, no sessions)
		http.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
		return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

}
