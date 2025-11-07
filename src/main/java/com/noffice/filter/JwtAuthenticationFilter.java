package com.noffice.filter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noffice.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.noffice.service.JwtService;
import com.noffice.service.UserDetailsServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authorizationHeader = request.getHeader("Authorization");
            String path = request.getRequestURI();
            if (path.contains("/api/auth/refreshToken") || path.contains("/api/auth/login")) {
                filterChain.doFilter(request, response);
                return;
            }
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authorizationHeader.substring(7);
            String username = jwtService.extractUsername(token);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//                if (userDetails instanceof User) {
//                	User userToken=(User)userDetails;
//                	if(userToken!=null&&userToken.getPartnerId()!=null){
//                		LocalDateTime expiredAt=trialRegistrationRepository.getTrialExpiredAtByPartnerId(userToken.getPartnerId());
//                		if (expiredAt!=null&&expiredAt.isBefore(LocalDateTime.now())) {
//                		    response.setStatus(401);
//                	        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//                	        Map<String, Object> body = new HashMap<>();
//                	        body.put("status", 401);
//                	        body.put("message", "Gói dùng thử đã hết hạn");
//                	        body.put("code", "TRIAL_EXPIRED");
//                	        new ObjectMapper().writeValue(response.getOutputStream(), body);
//                	        return;
//                		}
//                	}
//                }
                if (jwtService.isValidateToken(token, userDetails)) {
//                    String roleUserDeptId = jwtService.extractRoleUserDeptId(token);
//                    if (roleUserDeptId != null && !roleUserDeptId.isEmpty()) {
//                        if (userDetails instanceof User) {
//                            ((User) userDetails).setRoleUserDeptId(Long.parseLong(roleUserDeptId));
//                        }
//                    }
//                    if (userDetails instanceof User user) {
//                        if (user.getIsAdmin() == 1 && user.getIsAccessPartner() == 1) {
//                            LocalDateTime currentTime = LocalDateTime.now();
//                            Long partnerIdTemp = user.getPartnerIdTemp();
//                            List<PartnerConnection> listPartner = partnerConnectionRepository.findAllByIsActiveAndNotExpiredAndPartnerId(1,currentTime,partnerIdTemp);
//                            PartnerConnection partnerConnnect = null;
//                            if(listPartner!=null && !listPartner.isEmpty())
//                            {
//                            	partnerConnnect = listPartner.get(0);
//                            }
//                            LocalDateTime expireAt = user.getConnectionExpirationDate();
//                            if(partnerConnnect!=null)
//                            	expireAt = partnerConnnect.getConnectionExpirationDate();
//                            if (expireAt == null || currentTime.isAfter(expireAt)) {
//                                // Hết hạn → thu hồi quyền
//                                user.setIsAccessPartner(0);
//                                user.setPermissionIdsTemp(null);
//                                user.setPartnerIdTemp(null);
//                                user.setConnectionExpirationDate(null);
//                                userRepository.save(user);
//                            } else {
//                                // Còn hạn → gán partner tạm thời
//                                user.setPartnerId(partnerIdTemp);
//                            }
//                        }
//                    }

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException ex) {
            response.setStatus(200);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            Map<String, Object> body = new HashMap<>();
            body.put("status", 498);
            body.put("message", "Phiên đăng nhập hết hạn");
            body.put("code", "JWT_EXPIRED");
            new ObjectMapper().writeValue(response.getOutputStream(), body);
        }
    }


}
