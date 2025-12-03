package com.noffice.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import java.io.IOException;
import java.io.OutputStream;

import com.noffice.dto.*;
import com.noffice.service.LogService;
import com.noffice.ultils.Constants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.httpclient.HttpClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.noffice.entity.RefreshToken;
import com.noffice.entity.User;
import com.noffice.reponse.AuthenticationResponse;
import com.noffice.reponse.ResponseAPI;
import com.noffice.reponse.TokenResponse;
import com.noffice.repository.UserRepository;
import com.noffice.service.AuthenticationService;
import com.noffice.service.JwtService;
import com.noffice.service.RefreshTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;

import com.github.cage.Cage;
import com.github.cage.GCage;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {
	private final UserRepository userRepository;
	private final AuthenticationService authenticationService;
	private final RefreshTokenService refreshTokenService;
	private final JwtService jwtService;
	private final LogService logService;

	private HttpClient client;

//	private void initConnection() {
//		if (client == null) {
//			client = new HttpClient();
//			HttpConnectionManager conMgr = client.getHttpConnectionManager();
//			HttpConnectionManagerParams conPars = conMgr.getParams();
//			conPars.setConnectionTimeout(10000);
//			conPars.setSoTimeout(10000);
//			conPars.setConnectionTimeout(10000);
//			conPars.setSoTimeout(10000);
//		}
//		if (smsService == null) {
//			smsService = new SmsService();
//		}
//
//	}

	@PostMapping("/logout")
	public ResponseEntity<ResponseAPI> logout(HttpServletRequest request) {
	    try {
	        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
	        if (authentication instanceof AnonymousAuthenticationToken || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
	            return ResponseEntity.status(HttpStatus.OK)
	                    .body(new ResponseAPI(null, Constants.message.NO_TOKEN_INFO, 401));
	        }

	        
	        if (!(authentication.getPrincipal() instanceof User)) {
	            return ResponseEntity.status(HttpStatus.OK)
	                    .body(new ResponseAPI(null, Constants.message.NO_USER_INFO, 401));
	        }

//			logService.createLog(ActionType.LOGOUT.getAction(),
//					Map.of("actor", userDetails.getUsername(),"action", FunctionType.LOGOUT.getFunction(), "object", userDetails.getUsername()),
//					userDetails.getId(), null, userDetails.getPartnerId());

	        return ResponseEntity.status(HttpStatus.OK)
	                .body(new ResponseAPI(null, Constants.message.SUCCESS, 200));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.OK)
	                .body(new ResponseAPI(null, Constants.message.SYSTEM_ERROR_2 + e.getMessage(), 500));
	    }
	}

	 @PostMapping("/login")
	    public ResponseEntity<ResponseAPI> loginNoReturnToken(@RequestBody UserLoginDTO user,
	                                                         HttpServletRequest request) {
	        try {
	            AuthenticationResponse response = authenticationService.authenticate(user);
	            
	            // Kiểm tra thời hạn dùng thử nếu có partner_id
//	            if (response.getPartner_id() != null) {
//	                Long partnerId = Long.parseLong(response.getPartner_id());
//	                LocalDateTime expiredAt = trialRegistrationRepository.getTrialExpiredAtByPartnerId(partnerId);
//	                if (expiredAt != null && expiredAt.isBefore(LocalDateTime.now())) {
//	                    return ResponseEntity.ok(new ResponseAPI(null, "Gói dùng thử của bạn đã hết hạn. Vui lòng liên hệ đội ngũ để được hổ trợ .", 401));
//	                }
//	            }

	            // Kiểm tra 2FA
	            User dbUser = userRepository.findByUsername(user.getUsername())
	                    .orElseThrow(() -> new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng!"));

//				logService.createLog(ActionType.LOGIN.getAction(),
//						Map.of("actor", response.getUsername(),"action", FunctionType.LOGIN.getFunction(), "object", response.getUsername()),
//						response.getUser_id(), null, response.getPartner_id() != null ? Long.parseLong(response.getPartner_id()) : null);

	            return ResponseEntity.ok(new ResponseAPI(response, "Đăng nhập thành công", 200));
	        } catch (IllegalArgumentException e) {
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                    .body(new ResponseAPI(null, e.getMessage(), 400));
	        } catch (Exception e) {
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new ResponseAPI(null, Constants.message.SYSTEM_ERROR_2 + e.getMessage(), 500));
	        }
	    }

	
	@GetMapping("/checkAPI")
	public ResponseAPI checkapi() {
		return new ResponseAPI(Constants.message.SUCCESS, Constants.message.SUCCESS, 200);
	}
	
	@PostMapping("/signin")
	public ResponseAPI signin(@RequestBody UserLoginDTO user) {
		try {
			AuthenticationResponse response = authenticationService.authenticate(user);
			return new ResponseAPI(response, Constants.message.SUCCESS, 200);
		} catch (Exception e) {
			return new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 400);
		}
	}

	@PostMapping("/refreshToken")
	public ResponseEntity<ResponseAPI> refreshToken(@RequestBody RefreshTokenReqDTO refreshTokenReqDTO) {
		String refreshToken = refreshTokenReqDTO.getRefreshToken();
		Optional<RefreshToken> optionalRefreshToken = refreshTokenService.findByRefreshToken(refreshToken);
		if (optionalRefreshToken.isPresent()) {
			try {
				RefreshToken verifiedToken = refreshTokenService.verifyExpiration(optionalRefreshToken.get());
				String username = verifiedToken.getUsername();
				User userOpt = userRepository.findByUsername(username)
				            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
				String token = jwtService.generateToken(userOpt,null,null);
				return ResponseEntity.ok(new ResponseAPI(new TokenResponse(token), Constants.message.SUCCESS, 200));
			} catch (Exception e) {
				return ResponseEntity
						.status(HttpStatus.UNAUTHORIZED)
						.body(new ResponseAPI(null, "Refresh token is expired", 401));
			}
		} else {
			return ResponseEntity.ok(new ResponseAPI(null, "token not found", 401));
		}
	}
	
	@PostMapping("/validateOtpAuthenticator")
    public ResponseEntity<ResponseAPI> validateOtpAuthenticator(@RequestBody Map<String, String> request) {
        try {
            // Kiểm tra xác thực
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            if (authentication == null ||
//                    authentication instanceof AnonymousAuthenticationToken ||
//                    !authentication.isAuthenticated() ||
//                    authentication.getPrincipal() == null) {
//                return ResponseEntity.status(HttpStatus.OK)
//                        .body(new ResponseAPI(null, Constants.message.NO_TOKEN_INFO, 401));
//            }
//
//            if (!(authentication.getPrincipal() instanceof User)) {
//                return ResponseEntity.status(HttpStatus.OK)
//                        .body(new ResponseAPI(null, Constants.message.NO_USER_INFO, 401));
//            }

            // Kiểm tra quyền
//            User user = (User) authentication.getPrincipal();
//            String authenticatedUsername = user.getUsername();
            String username = request.get("username");
            if (username == null || username.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseAPI(null, Constants.message.USER_NAME_MUST_NOT_NULL, 400));
            }
//            if (!authenticatedUsername.equals(username)) {
//                return ResponseEntity.status(HttpStatus.OK)
//                        .body(new ResponseAPI(null, "Không có quyền xác thực OTP cho người dùng này", 403));
//            }

            // Kiểm tra đầu vào
            String code = request.get("otp");
            if (code == null || code.isEmpty()) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseAPI(null, "Mã OTP không được để trống", 400));
            }

            String secret = request.get("secret");
            boolean isInitialSetup = secret != null && !secret.isEmpty();

            // Xác thực OTP
            Object response = authenticationService.validateOtpAuthenticator(secret, code, username);

            if (response instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseData = (Map<String, Object>) response;
                boolean validated = (boolean) responseData.get("validated");


                // Xác thực lần đầu
                return ResponseEntity.ok(new ResponseAPI(responseData, "Xác thực OTP thành công", 200));
            } else if (response instanceof AuthenticationResponse) {
                // Xác thực đăng nhập
                AuthenticationResponse authResponse = (AuthenticationResponse) response;
                return ResponseEntity.ok(new ResponseAPI(authResponse, "Xác thực OTP thành công", 200));
            }

            // Trường hợp không xác định
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, "Lỗi xử lý response OTP", 500));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, Constants.message.SYSTEM_ERROR_2 + e.getMessage(), 500));
        }
    }
	
	@PostMapping("/update2FA")
    public ResponseAPI update2FA(@RequestBody Map<String, Object> request) {
        try {
            // Kiểm tra xác thực
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || 
                authentication instanceof AnonymousAuthenticationToken || 
                !authentication.isAuthenticated() || 
                authentication.getPrincipal() == null) {
                return new ResponseAPI(null, Constants.message.NO_TOKEN_INFO, 401);
            }

            if (!(authentication.getPrincipal() instanceof User)) {
                return new ResponseAPI(null, Constants.message.NO_USER_INFO, 401);
            }

            // Kiểm tra quyền
            User user = (User) authentication.getPrincipal();
            String authenticatedUsername = user.getUsername();
            String username = (String) request.get("username");
            if (username == null || username.isEmpty()) {
                return new ResponseAPI(null, Constants.message.USER_NAME_MUST_NOT_NULL, 400);
            }
            if (!authenticatedUsername.equals(username)) {
                return new ResponseAPI(null, "Không có quyền cập nhật 2FA cho người dùng này", 403);
            }

            // Kiểm tra twoFAType
            if (!request.containsKey("twoFAType")) {
                return new ResponseAPI(null, "twoFAType không được để trống", 400);
            }
            int twofaType;
            try {
                twofaType = Integer.parseInt(request.get("twoFAType").toString());
            } catch (NumberFormatException e) {
                return new ResponseAPI(null, "twoFAType phải là số nguyên", 400);
            }

            // Cập nhật 2FA
            authenticationService.update2FA(username, twofaType);
            return new ResponseAPI(Map.of("twoFAType", twofaType), Constants.message.SUCCESS, 200);
        } catch (Exception e) {
            return new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 400);
        }
    }

	@GetMapping("/check2FA")
	public ResponseAPI check2FA(@RequestParam String username) {
		try {
			int type = authenticationService.check2FA(username);
			Map<String, Integer> result = new HashMap<>();
			result.put("twofaType", type);
			return new ResponseAPI(result, Constants.message.SUCCESS, 200);
		} catch (Exception e) {
			return new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 400);
		}
	}
	
	private final Cage cage = new GCage();
	@Value("${captcha.rate-limit.max-requests}")
	private Integer captchaLimitRequest;
	@Value("${captcha.rate-limit.cooldown-ms}")
	private Long captchaLimitCooldown;
	@GetMapping("/generate-captcha")
	public void generateCaptcha(HttpServletRequest request, HttpSession session, OutputStream outputStream) throws IOException {
	    String clientIp = getClientIp(request);
	    long currentTime = System.currentTimeMillis();
	    
	    // Lấy thông tin yêu cầu CAPTCHA trước đó từ session (hoặc tạo mới nếu không có)
	    Map<String, Object> captchaInfo = (Map<String, Object>) session.getAttribute("captchaInfo");
	    if (captchaInfo == null) {
	        captchaInfo = new HashMap<>();
	    }
	    long lastRequestTime = (Long) captchaInfo.getOrDefault("lastRequestTime", 0L);
	    int requestCount = (Integer) captchaInfo.getOrDefault("requestCount", 0);

	    if (currentTime - lastRequestTime < captchaLimitCooldown && requestCount >= captchaLimitRequest) {
	        // Nếu quá 5 lần trong 1 phút, không cho phép yêu cầu CAPTCHA mới
	        throw new RateLimitException("Too many requests. Please try again later.");
	    }

	    // Tạo CAPTCHA mới
	    String captcha = cage.getTokenGenerator().next();
	    session.setAttribute("captcha", captcha); // Lưu CAPTCHA vào session

	    // Cập nhật thông tin yêu cầu CAPTCHA vào session
	    captchaInfo.put("lastRequestTime", currentTime);
	    captchaInfo.put("requestCount", requestCount + 1);
	    session.setAttribute("captchaInfo", captchaInfo);

	    cage.draw(captcha, outputStream);
	}

	@PostMapping("/verify-captcha")
	public Map<String, Object> verifyCaptcha(@RequestBody Map<String, String> request, HttpSession session) {
	    String userInput = request.get("captchaText");
	    String sessionCaptcha = (String) session.getAttribute("captcha");

	    Map<String, Object> result = new HashMap<>();
	    if (sessionCaptcha != null && sessionCaptcha.equalsIgnoreCase(userInput)) {
	        result.put("success", true);
	    } else {
	        result.put("success", false);
	    }

	    // Cập nhật lại số lần yêu cầu CAPTCHA trong session
	    Map<String, Object> captchaInfo = (Map<String, Object>) session.getAttribute("captchaInfo");
	    if (captchaInfo != null) {
	        int requestCount = (Integer) captchaInfo.get("requestCount");
	        if (requestCount > 0) {
	            captchaInfo.put("requestCount", requestCount - 1); // Giảm số lần yêu cầu
	            session.setAttribute("captchaInfo", captchaInfo);
	        }
	    }

	    return result;
	}

	private String getClientIp(HttpServletRequest request) {
	    String clientIp = request.getHeader("X-Forwarded-For");
	    if (clientIp == null || clientIp.isEmpty()) {
	        clientIp = request.getRemoteAddr();
	    }
	    return clientIp;
	}

	// Lớp exception cho lỗi rate limit
	public class RateLimitException extends RuntimeException {
	    public RateLimitException(String message) {
	        super(message);
	    }
	}

	@PostMapping("/checkPassword")
	public boolean checkPassword(@RequestBody UserLoginDTO user) {
		return authenticationService.checkPassword(user);
	}

	@PostMapping("/checkUsernameAndPassword")
	public boolean checkUsernameAndPassword(@RequestBody UserLoginDTO user) {
		return authenticationService.checkUsernameAndPassword(user);
	}

	@PostMapping("/changePassword")
	public ResponseEntity<ResponseAPI> changePassword(@Valid @RequestBody ChangePasswordDTO changePasswordDTO, 
            HttpServletRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User currentUser = (User) authentication.getPrincipal();
		
		try {
		// Thực hiện đổi mật khẩu
		authenticationService.changePassword(changePasswordDTO, currentUser);
		return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, Constants.message.SUCCESS, 200));
		} catch (IllegalArgumentException e) {
		// Bắt lỗi từ service và trả về message cụ thể
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, e.getMessage(), 400));
		} catch (Exception e) {
		// Bắt các lỗi khác (nếu có)
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, e.getMessage(), 500));
		}
	}

	@GetMapping("/generateQR")
    public ResponseAPI generateQR(@RequestParam String username, @RequestParam String secret) {
        try {
            // Kiểm tra xác thực
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || 
                authentication instanceof AnonymousAuthenticationToken || 
                !authentication.isAuthenticated() || 
                authentication.getPrincipal() == null) {
                return new ResponseAPI(null, Constants.message.NO_TOKEN_INFO, 401);
            }

            if (!(authentication.getPrincipal() instanceof User)) {
                return new ResponseAPI(null, Constants.message.NO_USER_INFO, 401);
            }

            // Kiểm tra đầu vào
            if (username == null || username.isEmpty()) {
                return new ResponseAPI(null, Constants.message.USER_NAME_MUST_NOT_NULL, 400);
            }
            if (secret == null || secret.isEmpty()) {
                return new ResponseAPI(null, "Secret không được để trống", 400);
            }

            // Gọi service để tạo QR code
            String qrCode = authenticationService.createQR(username, secret);
            return new ResponseAPI(qrCode, Constants.message.SUCCESS, 200);
        } catch (Exception e) {
            return new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 400);
        }
    }

	@GetMapping("/generateSecret")
    public ResponseAPI generateSecret() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || 
                authentication instanceof AnonymousAuthenticationToken || 
                !authentication.isAuthenticated() || 
                authentication.getPrincipal() == null) {
                return new ResponseAPI(null, Constants.message.NO_TOKEN_INFO, 401);
            }

            if (!(authentication.getPrincipal() instanceof User)) {
                return new ResponseAPI(null, Constants.message.NO_USER_INFO, 401);
            }

            String secret = authenticationService.generateSecret();
            return new ResponseAPI(secret, Constants.message.SUCCESS, 200);
        } catch (Exception e) {
            return new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 400);
        }
    }


	@PostMapping("/changePasswordCheckOldPwd")
    public ResponseEntity<ResponseAPI> changePasswordCheckOldPwd(
            @Valid @RequestBody UserChangePwDTO userDTO,
            HttpServletRequest request) {

        // Lấy thông tin user từ token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
        String username = userDetails.getUsername();

        try {
            // Thực hiện đổi mật khẩu
            authenticationService.changePasswordCheckOldPwd(userDTO, username);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, Constants.message.SUCCESS, 200));
        } catch (IllegalArgumentException e) {
            // Bắt lỗi từ service và trả về message cụ thể
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseAPI(null, e.getMessage(), 400));
        } catch (Exception e) {
            // Bắt các lỗi khác (nếu có)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseAPI(null, Constants.message.SYSTEM_ERROR_2 + e.getMessage(), 500));
        }
    }
	
	 @PostMapping("/changePasswordAfterLogin")
	    public ResponseEntity<ResponseAPI> changePasswordAfterLogin(
	            @Valid @RequestBody ChangePasswordAfterLoginDTO userDTO,
	            HttpServletRequest request) {
	        try {
	            // Thực hiện đổi mật khẩu
	            authenticationService.changePasswordAfterLogin(userDTO);
	            return ResponseEntity.status(HttpStatus.OK)
	                    .body(new ResponseAPI(null, Constants.message.SUCCESS, 200));
	        } catch (IllegalArgumentException e) {
	            // Bắt lỗi từ service và trả về message cụ thể
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                    .body(new ResponseAPI(null, e.getMessage(), 400));
	        } catch (Exception e) {
	            // Bắt các lỗi khác (nếu có)
	            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                    .body(new ResponseAPI(null, Constants.message.SYSTEM_ERROR_2 + e.getMessage(), 500));
	        }
	    }

	@GetMapping("/ping")
	public ResponseEntity<ResponseAPI> ping(HttpServletRequest request) {
		String token = jwtService.extractTokenFromHeader(request);
		if (token == null)
			return ResponseEntity.ok(new ResponseAPI(null, "Thiếu token", 401));

		boolean valid = jwtService.validateWithTimeout(token);
		if (!valid)
			return ResponseEntity.ok(new ResponseAPI(null, "TIMEOUT", 401));

		Map<String, Object> updateResult = jwtService.updateClaim(token, "lastActive", System.currentTimeMillis());

		return ResponseEntity.ok(new ResponseAPI(updateResult, "OK", 200));
	}


	@PostMapping("/forgetPassword")
	public ResponseEntity<ResponseAPI> forgetPassword(@RequestBody ForgotPasswordRequest request) {
		try {
			String response = authenticationService.processForgotPassword(request.getUsername(), request.getPhone());
			return ResponseEntity.status(HttpStatus.OK)
					.body(new ResponseAPI(response, Constants.message.SUCCESS, 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ResponseAPI(null, Constants.message.SYSTEM_ERROR_2 + e.getMessage(), 500));
		}
	}


}
