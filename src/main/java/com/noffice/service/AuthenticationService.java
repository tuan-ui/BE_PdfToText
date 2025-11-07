package com.noffice.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import com.noffice.dto.*;
import com.noffice.entity.Partners;
import com.noffice.repository.PartnerRepository;
import com.noffice.ultils.Constants;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.noffice.entity.User;
import com.noffice.reponse.AuthenticationResponse;
import com.noffice.repository.RolePermissionsRepository;
import com.noffice.repository.RoleRepository;
import com.noffice.repository.UserRepository;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import jakarta.transaction.Transactional;

@Service
public class AuthenticationService {
	private Base64 base64Codec = new Base64();

	@Autowired
	private UserRepository usersRepository;

	@Autowired
	private RoleRepository rolesRepository;

	@Autowired
	private RolePermissionsRepository permissionsRolesRepository;

	@Autowired
	private JwtService jwtService;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	private RefreshTokenService refreshTokenService;

	@Autowired
	private LogService logService;
    @Autowired
    private PartnerRepository partnerRepository;

    public AuthenticationResponse authenticate(UserLoginDTO user) {
        if (!checkUsernameAndPassword(user)) {
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng!");
        }

        User u = usersRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng!"));

			Partners partners = partnerRepository.getPartnerById(u.getPartnerId());
			if(partners == null)
				throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng!");
			if(!partners.getIsActive())
				throw new IllegalArgumentException("Đơn vị đang bị khóa!");
        User userOpt = new User();
        BeanUtils.copyProperties(u, userOpt);

        List<AuthenticationResponse.UserRoleDTO> userRoles;

        if (userOpt.getIsAdmin() == 1) {
//                List<String> allPermissions = permissionsRepository.findAllActivePermissionNames();
//
//                String permissionsStr = String.join(",", allPermissions);
				String permissionsStr = "1,3,31,28";
				AuthenticationResponse.UserRoleDTO superAdminRole = new AuthenticationResponse.UserRoleDTO(
                        "-1", "-1", "Admin tổng", "admin", "-1", "Tất cả", permissionsStr
                );
                userRoles = List.of(superAdminRole);
        } else {
            // Lấy quyền từ vai trò
			String permissionsStr = "1,3,31,28";
			AuthenticationResponse.UserRoleDTO superAdminRole = new AuthenticationResponse.UserRoleDTO(
					"1", "1", "Quanr trị nst", "admin", "1", "Tất cả", permissionsStr
			);
            userRoles = List.of(superAdminRole);;
        }

        List<String> roleUserDeptIds = userRoles.stream()
                .map(AuthenticationResponse.UserRoleDTO::getRole_user_dept_id)
                .collect(Collectors.toList());

        String token = jwtService.generateToken(userOpt, null, roleUserDeptIds);
        String refreshToken = refreshTokenService.createRefreshToken(user.getUsername()).getRefreshToken();
		long absoluteExp = jwtService.getClaim(token, "absoluteExp", Long.class);

        return new AuthenticationResponse(
                token,
                refreshToken,
                userOpt.getUsername(),
                userRoles,
                userOpt.getFullName(),
                userOpt.getEmail(),
                userOpt.getPhone(),
				userOpt.getIsActive(),
                userOpt.getTwofaType(),
                userOpt.getPartnerId(),
                userOpt.getId(),
                userOpt.getIsChangePassword(),
				absoluteExp
        );
    }

	public boolean checkPassword(UserLoginDTO user) {
		User userOpt = usersRepository.findByUsername(user.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại!"));
		return passwordEncoder.matches(user.getPassword(), userOpt.getPassword());
	}

	public boolean checkUsernameAndPassword(UserLoginDTO user) {
		User userOpt = usersRepository.findByUsername(user.getUsername())
				.orElseThrow(() -> new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không đúng!"));
		return passwordEncoder.matches(user.getPassword(), userOpt.getPassword());
	}

	public boolean changePassword(UserLoginDTO user) {
		User userOpt = usersRepository.findByUsername(user.getUsername())
				.orElseThrow(() -> new UsernameNotFoundException("Người dùng không tồn tại!"));
		userOpt.setPassword(passwordEncoder.encode(user.getPassword()));
		usersRepository.save(userOpt);
		return true;
	}

	 @Transactional
    public void changePassword(ChangePasswordDTO changePasswordDTO, User currentUser) {
        // Tìm user hiện tại từ username trong token
        User currUser = usersRepository.findByUsername(currentUser.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng hiện tại"));

        // Kiểm tra mật khẩu của người thực hiện đổi
        if (!passwordEncoder.matches(changePasswordDTO.getStaffPassword(), currUser.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu người dùng hiện tại không đúng");
        }

        // Tìm user cần đổi mật khẩu theo userId
        User targetUser = usersRepository.getUserByUserId(changePasswordDTO.getUserId());
        if (targetUser == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + changePasswordDTO.getUserId());
        }

        // Kiểm tra mật khẩu mới có hợp lệ
        if (changePasswordDTO.getUserNewPassword() == null ||
            changePasswordDTO.getUserNewPassword().length() < 8) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 8 ký tự");
        }

        // Cập nhật mật khẩu mới cho targetUser
        targetUser.setPassword(passwordEncoder.encode(changePasswordDTO.getUserNewPassword()));
        targetUser.setIsChangePassword(1); // đổi isChangePassword là 1 để lần sau đăng nhập bắt người dùng đổi lại
        usersRepository.save(targetUser);
//		logService.save(currentUser.getUserId(), FunctionType.RESET_PASSWORD.toString(),
//				 ActionType.RESET_PASSWORD.toString(),
//				targetUser.getUsername(), ActionType.RESET_PASSWORD.getAction()+ " " + targetUser.getUsername(),currentUser.getPartnerId(), targetUser.getUserId());
    }

	public String generateSecret() {
		DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator(16);
		return secretGenerator.generate();
	}

	public String createQR(String username, String secret) {
		try {
			QrData data = new QrData.Builder().label("QTD" + "-" + username).secret(secret).issuer("")
					.algorithm(HashingAlgorithm.SHA1).digits(6).period(30).build();
			dev.samstevens.totp.qr.QrGenerator generator = new ZxingPngQrGenerator();
			byte[] imageData = generator.generate(data);
			String mimeType = generator.getImageMimeType();
			String dataUri = getDataUriForImage(imageData, mimeType);
			return dataUri;
		} catch (Exception e) {
			return null;
		}
	}

	public String getDataUriForImage(byte[] data, String mimeType) {
		String encodedData = new String(base64Codec.encode(data));
		return String.format("data:%s;base64,%s", mimeType, encodedData);
	}

	public Object validateOtpAuthenticator(String secret, String code, String username) {
	    User user = usersRepository.findByUsername(username)
	            .orElseThrow(() -> new IllegalArgumentException("Người dùng không tồn tại!"));

	    // Xác định loại xác thực
	    boolean isInitialSetup = secret != null && !secret.isEmpty() && !"null".equalsIgnoreCase(secret);
	    //String otpSecret = isInitialSetup ? secret : user.getSecretKey();
		String otpSecret = secret;
	    if (otpSecret == null || otpSecret.isEmpty()) {
	        throw new IllegalArgumentException("Không tìm thấy secret key để xác thực OTP!");
	    }

	    // Xác thực OTP
	    SystemTimeProvider timeProvider = new SystemTimeProvider();
	    DefaultCodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
	    DefaultCodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
	    verifier.setTimePeriod(30);
	    verifier.setAllowedTimePeriodDiscrepancy(1);
	    boolean successful = verifier.isValidCode(otpSecret, code);

	    // Chuẩn bị response cho xác thực lần đầu hoặc thất bại
	    Map<String, Object> responseData = new HashMap<>();
	    Date serverTime = new Date();
	    TimeZone timezone = TimeZone.getDefault();
	    long timestamp = serverTime.getTime();
	    long timeCounter = timestamp / 1000 / 30;

	    responseData.put("validated", successful);
	    responseData.put("serverTime", serverTime.toString());
	    responseData.put("timestamp", timestamp);
	    responseData.put("timeCounter", timeCounter);
	    responseData.put("timezone", timezone.getID());
	    responseData.put("timezoneOffset", timezone.getRawOffset() / 3600000);

	    if (!successful) {
	        return responseData; // Xác thực thất bại
	    }

	    // Nếu là xác thực lần đầu, cập nhật secretKey và twofa_type
	    if (isInitialSetup) {
	        //user.setSecretKey(secret);
	        user.setTwofaType(3);
	        usersRepository.save(user);
	        return responseData; // Trả về response OTP
	    }

	    // Xác thực đăng nhập, tạo AuthenticationResponse
	    User userOpt = new User();
	    BeanUtils.copyProperties(user, userOpt);

	    List<AuthenticationResponse.UserRoleDTO> userRoles;
	    if (userOpt.getIsAdmin() == 1 ) {

/*	            List<String> allPermissions = permissionsRepository.findAllActivePermissionNames();
	            String permissionsStr = String.join(",", allPermissions);*/
				String permissionsStr = "1,3,31,28";
	            AuthenticationResponse.UserRoleDTO superAdminRole = new AuthenticationResponse.UserRoleDTO(
	                    "-1", "-1", "Admin tổng", "admin", "-1", "Tất cả", permissionsStr
	            );
	            userRoles = List.of(superAdminRole);

	    } else {

			String permissionsStr = "1,3,31,28";
			AuthenticationResponse.UserRoleDTO superAdminRole = new AuthenticationResponse.UserRoleDTO(
					"1", "1", "Quanr trị nst", "admin", "1", "Tất cả", permissionsStr
			);
			userRoles = List.of(superAdminRole);;
	    }

	    String roleUserDeptId = null;
	    List<String> roleUserDeptIds = userRoles.stream()
	            .map(AuthenticationResponse.UserRoleDTO::getRole_user_dept_id)
	            .collect(Collectors.toList());

	    String token = jwtService.generateToken(userOpt, roleUserDeptId, roleUserDeptIds);
	    String refreshToken = refreshTokenService.createRefreshToken(username).getRefreshToken();
		long absoluteExp = jwtService.getClaim(token, "absoluteExp", Long.class);
	    return new AuthenticationResponse(
	            token,
	            refreshToken,
	            userOpt.getUsername(),
	            userRoles,
	            userOpt.getFullName(),
	            userOpt.getEmail(),
	            userOpt.getPhone(),
				userOpt.getIsActive(),
	            userOpt.getTwofaType(),
	            userOpt.getPartnerId(),
	            userOpt.getId(),
	            userOpt.getIsChangePassword(),
				absoluteExp
	    );
	}
	public void update2FA(String username, int twofaType) {
		User user = usersRepository.findByUsername(username).orElse(null);
		if (user != null) {
			user.setTwofaType(twofaType);
			if (twofaType == 0) {
				//user.setSecretKey(null);
			}
			usersRepository.save(user);
		}
	}
	public int check2FA(String username) {
		User user = usersRepository.findByUsername(username).orElse(null);
		if (user != null)
			return user != null && user.getTwofaType() != null ? user.getTwofaType() : 0;
		return 0;
	}

	// change password
	 @Transactional
	    public void changePasswordCheckOldPwd(UserChangePwDTO userDTO, String username) {
	        // Tìm user theo username
	        User userOpt = usersRepository.findByUsername(username)
	                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với username: " + username));

	        // Kiểm tra mật khẩu cũ
	        if (!passwordEncoder.matches(userDTO.getOldPassword(), userOpt.getPassword())) {
	            throw new IllegalArgumentException("error.WrongPassword");
	        }

	        // Kiểm tra mật khẩu mới có hợp lệ
	        if (userDTO.getNewPassword() == null || userDTO.getNewPassword().length() < 8) {
	            throw new IllegalArgumentException("error.atLeastCharacters");
	        }

	        // Cập nhật mật khẩu mới
	        userOpt.setPassword(passwordEncoder.encode(userDTO.getNewPassword()));
			userOpt.setIsChangePassword(Constants.IS_ACTIVE.ACTIVE);
	        usersRepository.save(userOpt);
	    }
	@Transactional
    public void changePasswordAfterLogin(ChangePasswordAfterLoginDTO userDTO) {
        // Tìm user theo userId
        User userOpt = usersRepository.getUserByUserId(userDTO.getUserId());
        if (userOpt == null) {
            throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userDTO.getUserId());
        }

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(userDTO.getOldPassword(), userOpt.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không đúng");
        }

        // Kiểm tra mật khẩu mới có hợp lệ
        if (userDTO.getNewPassword() == null || userDTO.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 8 ký tự");
        }

        // Cập nhật mật khẩu mới và đặt lại isChangePassword
        userOpt.setPassword(passwordEncoder.encode(userDTO.getNewPassword()));
        userOpt.setIsChangePassword(0); // Đặt lại về 0 sau khi đổi mật khẩu
        usersRepository.save(userOpt);
    }

}
