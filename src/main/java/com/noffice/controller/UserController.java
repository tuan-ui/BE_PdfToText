package com.noffice.controller;

import com.noffice.entity.User;
import com.noffice.dto.*;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.ResponseAPI;
import com.noffice.repository.UserRepository;
import com.noffice.service.*;
import com.noffice.ultils.Constants.UPLOAD;
import com.noffice.ultils.DateUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;

    private ResponseEntity<ResponseAPI> validateToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || 
            authentication instanceof AnonymousAuthenticationToken || 
            !authentication.isAuthenticated() || 
            !(authentication.getPrincipal() instanceof User)) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, "Không có token hoặc phiên đăng nhập hợp lệ", 401));
        }
        return null;
    }
	
	@GetMapping("/list")
    public ResponseAPI listUsers(
            @RequestParam(value = "searchString", required = false, defaultValue = "") String searchString,
            @RequestParam(value = "userName", required = false, defaultValue = "") String userName,
            @RequestParam(value = "fullName", required = false, defaultValue = "") String fullName,
            @RequestParam(value = "phone", required = false, defaultValue = "") String phone,
            @RequestParam(value = "birthday", required = false, defaultValue = "") String birthday,
            @RequestParam(value = "birthdayStr", required = false, defaultValue = "") String birthdayStr,
            @RequestParam(value = "userCode", required = false, defaultValue = "") String userCode,
            @RequestParam(value = "roleId", required = false) Long roleId,
            @RequestParam(value = "departmentId", required = false) Long departmentId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();

            if (size == -1) {
                Pageable pageable = Pageable.unpaged();
                Page<User> users = userService.listUsers(
                        searchString, userName, fullName, phone, birthdayStr,
                        userCode, userDetails,
                        pageable
                );
                return new ResponseAPI(users, "success", 200);
            }

            Pageable pageable = PageRequest.of(page, size);
            Page<User> users = userService.listUsers(searchString, userName, fullName, phone, birthdayStr,
                    userCode, userDetails, pageable);
            return new ResponseAPI(users, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, e.getMessage(), 400);
        }
    }
	
	
	
	@GetMapping("/delete")
	public ResponseEntity<ResponseAPI> deleteUser(@RequestParam(value = "id") UUID id,
                                  @RequestParam(value = "version") Long version) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			String message = userService.deleteUser(id, userDetails, version);
            if (message != null && !message.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseAPI(null, message, 400));
            }
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }

    @PostMapping("/deleteMuti")
    public ResponseAPI deleteMuti(@RequestBody List<DeleteMultiDTO> ids) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            String message = userService.deleteMultiUser(ids, userDetails);
            if (message != null && !message.trim().isEmpty()) {
                return new ResponseAPI(null, message, 400);
            }
            return new ResponseAPI(null, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }
	
	@GetMapping("/resetPassword")
	public ResponseAPI resetPassword(@RequestParam(value = "id") UUID id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			userService.resetPassword(id, userDetails);
			return new ResponseAPI(null, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}


    @GetMapping("/lock")
    public ResponseEntity<ResponseAPI> lock(
            @RequestParam(value = "id") UUID id,
            @RequestParam(value = "version") Long version) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            String message = userService.lockUser(id,userDetails, version );
            if (message != null && !message.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseAPI(null, message, 400));
            }
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }
	
	private void deleteFile(String relativePath) throws IOException {
        if (relativePath != null && !relativePath.isEmpty()) {
            Path filePath = Paths.get(UPLOAD.IMAGE_DIRECTORY + relativePath);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        }
    }

    @PostMapping(value = "/update", consumes = "multipart/form-data")
    public ResponseAPI updateUser(
            @RequestParam("userName") String username,
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam("identifyCode") String identifyCode,
            @RequestParam("userCode") String userCode,
            @RequestParam("birthday") String birthDayStr,
            @RequestParam(value = "gender", required = false) Integer gender,
            @RequestParam("issueDate") String issueDateStr,
            @RequestParam("issuePlace") String issuePlace,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "signatureImage", required = false) MultipartFile signatureImage,
            @RequestParam(value = "roleIds", required = false) List<UUID> roleIds,
            @RequestParam(value = "version", required = false) Long version,
            HttpServletRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();

        UserCreateDTO user = new UserCreateDTO();
        user.setUsername(username);
        user.setFullname(fullName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setIdentifyCode(identifyCode);
        user.setPartnerId(userDetails.getPartnerId());
        user.setUserCode(userCode);
        try {
            if (birthDayStr != null && !birthDayStr.trim().isEmpty()) {
                Date birthDay = DateUtil.parseFlexibleDate2(birthDayStr);
                user.setBirthDay(birthDay);
            }
        } catch (ParseException e) {
            return new ResponseAPI(null, "error.DateParseError", 400);
        }
        user.setGender(gender);
        try {
            if (issueDateStr != null && !issueDateStr.trim().isEmpty()) {
                Date issueDate = DateUtil.parseFlexibleDate2(issueDateStr);
                user.setIssueDate(issueDate);
            }
        } catch (ParseException e) {
            return new ResponseAPI(null, "error.DateParseError", 400);
        }
        user.setIssuePlace(issuePlace);
        user.setProfileImage(profileImage);
        user.setSignatureImage(signatureImage);
        try {
            // Required field validation
            if (isEmpty(user.getUsername()) || isEmpty(user.getFullname()) ||
                isEmpty(user.getUserCode())) {
                return new ResponseAPI(null, "error.EnterrequiredInformation", 400);
            }

            // Validation for non-mandatory fields only if provided
            if (!isEmpty(user.getEmail()) && !isValidEmail(user.getEmail())) {
                return new ResponseAPI(null, "error.InvalidEmail", 400);
            }
            if (!isEmpty(user.getPhone()) && !isValidPhoneNumber(user.getPhone())) {
                return new ResponseAPI(null, "error.InvalidPhoneNumber", 400);
            }
            if (!isEmpty(birthDayStr) && !isValidBirthday(user.getBirthDay())) {
                return new ResponseAPI(null, "error.DateOfBirthCannotBeGreaterThanCurrentDate", 400);
            }
            if (!isEmpty(user.getIdentifyCode()) && !isValidIdentifyCode(user.getIdentifyCode())) {
                return new ResponseAPI(null, "error.InvalidID", 400);
            }
            if (!user.getUsername().matches("[A-Za-z0-9_]+")) {
                return new ResponseAPI(null, "error.LoginNameCannotContainSpecialCharacters", 400);
            }
            if (!user.getUserCode().matches("[A-Za-z0-9]+")) {
                return new ResponseAPI(null, "error.EmployeeCodeCannotContainSpecialCharacters", 400);
            }

            // Get existing user data to retrieve old file paths
            User existingUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("error.UserDoesNotExist"));
            String oldProfileImagePath = existingUser != null ? existingUser.getProfileImage() : null;
            String oldSignatureImagePath = existingUser != null ? existingUser.getSignatureImage() : null;

            // Save files and get paths (only if files are provided)
            String profileImagePath = null;
            String signatureImagePath = null;
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                // Delete old profile image if it exists
                deleteFile(oldProfileImagePath);
                profileImagePath = saveFile(user.getProfileImage(), userDetails.getPartnerId(), "profile");
            } else {
                profileImagePath = oldProfileImagePath; // Retain old path if no new file
            }
            if (user.getSignatureImage() != null && !user.getSignatureImage().isEmpty()) {
                // Delete old signature image if it exists
                deleteFile(oldSignatureImagePath);
                signatureImagePath = saveFile(user.getSignatureImage(), userDetails.getPartnerId(), "signature");
            } else {
                signatureImagePath = oldSignatureImagePath; // Retain old path if no new file
            }

            String message = userService.updateUser(user, userDetails.getPartnerId(), profileImagePath, signatureImagePath, userDetails, roleIds, version);
            if (message != null && !message.trim().isEmpty()) {
                return new ResponseAPI(null, message, 400);
            }
            return new ResponseAPI(null, "success", 200);
        } catch (IOException e) {
            return new ResponseAPI(null, "error.ErrorProcessingFile", 400);
        } catch (RuntimeException e) {
            return new ResponseAPI(null, e.getMessage(), 400);
        }
    }
	
    @PostMapping(value = "/updateProfile", consumes = "multipart/form-data")
    public ResponseAPI updateUserProfile(
            @RequestParam("userId") String userIdStr,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "signatureImage", required = false) MultipartFile signatureImage,
            @RequestParam(value = "digitalCertificateName", required = false) String digitalCertificateName,
            @RequestParam(value = "simCa", required = false) String simCa,
            HttpServletRequest request) {

        try {
            // Parse userId
            long userId;
            try {
                userId = Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                return new ResponseAPI(null, "userId phải là số hợp lệ", 400);
            }

            // Validation for non-mandatory fields only if provided
            if (email != null && !email.trim().isEmpty() && !isValidEmail(email)) {
                return new ResponseAPI(null, "Email không hợp lệ", 400);
            }
            if (phoneNumber != null && !phoneNumber.trim().isEmpty() && !isValidPhoneNumber(phoneNumber)) {
                return new ResponseAPI(null, "Số điện thoại không hợp lệ", 400);
            }

            // Get authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();

            // Find existing user
            User existingUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

            // Get old file paths
            String oldProfileImagePath = existingUser.getProfileImage();
            String oldSignatureImagePath = existingUser.getSignatureImage();

            // Save files and get paths
            String profileImagePath = null;
            String signatureImagePath = null;
            if (profileImage != null && !profileImage.isEmpty()) {
                // Delete old profile image if it exists
                deleteFile(oldProfileImagePath);
                profileImagePath = saveFile(profileImage, userDetails.getPartnerId(), "profile");
            } else {
                profileImagePath = oldProfileImagePath; // Retain old path if no new file
            }
            if (signatureImage != null && !signatureImage.isEmpty()) {
                // Delete old signature image if it exists
                deleteFile(oldSignatureImagePath);
                signatureImagePath = saveFile(signatureImage, userDetails.getPartnerId(), "signature");
            } else {
                signatureImagePath = oldSignatureImagePath; // Retain old path if no new file
            }

            // Update user with new values
            userService.updateUserProfile(existingUser, phoneNumber, email, profileImagePath, signatureImagePath, userDetails.getPartnerId(), digitalCertificateName, simCa);
            return new ResponseAPI(null, "Cập nhật thành công", 200);

        } catch (IOException e) {
            return new ResponseAPI(null, "Lỗi khi xử lý file: " + e.getMessage(), 400);
        } catch (RuntimeException e) {
            return new ResponseAPI(null, e.getMessage(), 400);
        } catch (Exception e) {
            return new ResponseAPI(null, "Cập nhật thất bại", 400);
        }
    }
    
    @PostMapping(value = "/add", consumes = "multipart/form-data")
    public ResponseAPI createUser(
            @RequestParam("userName") String username,
            @RequestParam("fullName") String fullname,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam("identifyCode") String identifyCode,
            @RequestParam("password") String password,
            @RequestParam("userCode") String userCode,
            @RequestParam("birthday") String birthDayStr,
            @RequestParam(value = "gender", required = false) Integer gender,
            @RequestParam("issueDate") String issueDateStr,
            @RequestParam("issuePlace") String issuePlace,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "signatureImage", required = false) MultipartFile signatureImage,
            @RequestParam(value = "roleIds", required = false) List<UUID> roleIds,
            HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User userDetails = (User) authentication.getPrincipal();
    	UserCreateDTO user = new UserCreateDTO();
        user.setUsername(username);
        user.setFullname(fullname);
        user.setPhone(phone);
        user.setEmail(email);
        user.setIdentifyCode(identifyCode);
        user.setPassword(password);
        user.setPartnerId(userDetails.getPartnerId());
        user.setUserCode(userCode);
        try {
            if (birthDayStr != null && !birthDayStr.trim().isEmpty()) {
                Date birthDay = DateUtil.parseFlexibleDate2(birthDayStr);
                user.setBirthDay(birthDay);
            }
        } catch (ParseException e) {
            return new ResponseAPI(null, "error.DateParseError", 400);
        }
        user.setGender(gender);
        try {
            if (issueDateStr != null && !issueDateStr.trim().isEmpty()) {
                Date issueDate = DateUtil.parseFlexibleDate2(issueDateStr);
                user.setIssueDate(issueDate);
            }
        } catch (ParseException e) {
            return new ResponseAPI(null, "error.DateParseError", 400);
        }
        user.setIssuePlace(issuePlace);
        user.setProfileImage(profileImage);
        user.setSignatureImage(signatureImage);
        try {
            // Required field validation
            if (isEmpty(user.getUsername()) || isEmpty(user.getFullname()) ||
                    isEmpty(user.getUserCode())) {
                return new ResponseAPI(null, "error.EnterrequiredInformation", 400);
            }

            // Validation for non-mandatory fields only if provided
            if (!isEmpty(user.getEmail()) && !isValidEmail(user.getEmail())) {
                return new ResponseAPI(null, "error.InvalidEmail", 400);
            }
            if (!isEmpty(user.getPhone()) && !isValidPhoneNumber(user.getPhone())) {
                return new ResponseAPI(null, "error.InvalidPhoneNumber", 400);
            }
            if (!isEmpty(birthDayStr) && !isValidBirthday(user.getBirthDay())) {
                return new ResponseAPI(null, "error.DateOfBirthCannotBeGreaterThanCurrentDate", 400);
            }
            if (!isEmpty(user.getIdentifyCode()) && !isValidIdentifyCode(user.getIdentifyCode())) {
                return new ResponseAPI(null, "error.InvalidID", 400);
            }
            if (!user.getUsername().matches("[A-Za-z0-9_]+")) {
                return new ResponseAPI(null, "error.LoginNameCannotContainSpecialCharacters", 400);
            }
            if (!user.getUserCode().matches("[A-Za-z0-9]+")) {
                return new ResponseAPI(null, "error.EmployeeCodeCannotContainSpecialCharacters", 400);
            }
           
            // Save files and get paths (only if files are provided)
            String profileImagePath = null;
            String signatureImagePath = null;
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                profileImagePath = saveFile(user.getProfileImage(), userDetails.getPartnerId(), "profile");
            }
            if (user.getSignatureImage() != null && !user.getSignatureImage().isEmpty()) {
                signatureImagePath = saveFile(user.getSignatureImage(), userDetails.getPartnerId(), "signature");
            }

            String message = userService.createUser(user, userDetails.getPartnerId(), profileImagePath, signatureImagePath, userDetails,roleIds);
            if (message != null && !message.trim().isEmpty()) {
                return new ResponseAPI(null, message, 400);
            }
            return new ResponseAPI(null, "success", 200);
        } catch (IOException e) {
            return new ResponseAPI(null, "error.ErrorProcessingFile", 400);
        } catch (RuntimeException e) {
            return new ResponseAPI(null, e.getMessage(), 400);
        }
    }
	

	@GetMapping("/getUserById")
    public ResponseAPI getUserById(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || 
                authentication instanceof AnonymousAuthenticationToken || 
                !authentication.isAuthenticated() || 
                authentication.getPrincipal() == null) {
                return new ResponseAPI(null, "Không có token hoặc phiên đăng nhập hợp lệ", 401);
            }

            if (!(authentication.getPrincipal() instanceof User)) {
                return new ResponseAPI(null, "Thông tin người dùng không hợp lệ", 401);
            }

            User user = (User) authentication.getPrincipal();
            UUID userId = user.getId();

            UserDetailDTO userDetail = userService.getByUserId(userId);
            return new ResponseAPI(userDetail, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail: " + e.getMessage(), 400);
        }
    }
	
	@GetMapping("/getImage")
    public ResponseEntity<?> getImageFile(
            @RequestParam(value = "id", required = false) UUID id,
            @RequestParam("type") String type) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || 
                authentication instanceof AnonymousAuthenticationToken || 
                !authentication.isAuthenticated() || 
                authentication.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseAPI(null, "Không có token hoặc phiên đăng nhập hợp lệ", 401));
            }

            if (!(authentication.getPrincipal() instanceof User)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseAPI(null, "Thông tin người dùng không hợp lệ", 401));
            }

            UserDetailDTO user = userService.getByUserId(id);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseAPI(null, "Người dùng không tồn tại", 404));
            }

            Resource resource;
            if ("profile".equals(type)) { 
                resource = userService.downFile(user.getProfileImage());
            } else if ("signature".equals(type)) {
                resource = userService.downFile(user.getSignatureImage());
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseAPI(null, "Loại hình ảnh không hợp lệ", 400));
            }

            if (resource != null && resource.exists() && resource.isReadable()) {
                String filename = resource.getFilename();
                MediaType mediaType = getMediaTypeFromFileName(filename);
                
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                        .contentType(mediaType != null ? mediaType : MediaType.APPLICATION_OCTET_STREAM)
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseAPI(null, "Hình ảnh không tồn tại", 404));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, "Lỗi hệ thống: " + e.getMessage(), 500));
        }
    }
	
	@PostMapping(value = "/updateImage", consumes = "multipart/form-data")
    public ResponseEntity<ResponseAPI> updateUserImage(
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestParam(value = "signatureImage", required = false) MultipartFile signatureImage
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null ||
                authentication instanceof AnonymousAuthenticationToken ||
                !authentication.isAuthenticated() ||
                authentication.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseAPI(null, "Không có token hoặc phiên đăng nhập hợp lệ", 401));
            }

            if (!(authentication.getPrincipal() instanceof User)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseAPI(null, "Thông tin người dùng không hợp lệ", 401));
            }

            User currentUser = (User) authentication.getPrincipal();

            // Get existing user data to retrieve old file paths
            User existingUser = userRepository.getUserByUserId(currentUser.getId());
            String oldProfileImagePath = existingUser != null ? existingUser.getProfileImage() : null;
            String oldSignatureImagePath = existingUser != null ? existingUser.getSignatureImage() : null;

            // Save files and get paths (only if files are provided)
            String profileImagePath = null;
            String signatureImagePath = null;
            if (profileImage != null && !profileImage.isEmpty()) {
                // Delete old profile image if it exists
                deleteFile(oldProfileImagePath);
                profileImagePath = saveFile(profileImage, currentUser.getPartnerId(), "profile");
            } else {
                profileImagePath = oldProfileImagePath; // Retain old path if no new file
            }
            if (signatureImage != null && !signatureImage.isEmpty()) {
                // Delete old signature image if it exists
                deleteFile(oldSignatureImagePath);
                signatureImagePath = saveFile(signatureImage, currentUser.getPartnerId(), "signature");
            } else {
                signatureImagePath = oldSignatureImagePath; // Retain old path if no new file
            }

            // Update user with new image paths
            userService.updateUserImages( currentUser.getId(), profileImagePath, signatureImagePath);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, "Cập nhật thành công", 200));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, "Lỗi khi xử lý file: " + e.getMessage(), 400));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, "Cập nhật thất bại: " + e.getMessage(), 500));
        }
    }
	
	private MediaType getMediaTypeFromFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        switch (extension) {
            case "jpg":
            case "jpeg":
                return MediaType.IMAGE_JPEG;
            case "png":
                return MediaType.IMAGE_PNG;
            case "gif":
                return MediaType.IMAGE_GIF;
            case "bmp":
                return MediaType.valueOf("image/bmp");
            case "webp":
                return MediaType.valueOf("image/webp");
            default:
                return null; // Trả về null nếu không xác định được, sẽ dùng fallback
        }
    }

    private String saveFile(MultipartFile file, UUID partnerId, String type) throws IOException {
        // Thư mục gốc
        Path baseDir = Paths.get(UPLOAD.IMAGE_DIRECTORY,
                String.valueOf(partnerId),
                type).normalize();

        // Đảm bảo thư mục tồn tại
        Files.createDirectories(baseDir);

        // Sanitize filename
        String originalName = Paths.get(file.getOriginalFilename())
                .getFileName()
                .toString()
                .replaceAll("[\\\\/]", "_");
        String safeFileName = System.currentTimeMillis() + "_" + originalName;

        // Build file path an toàn
        Path filePath = baseDir.resolve(safeFileName).normalize();

        // Check path không thoát khỏi baseDir
        if (!filePath.startsWith(baseDir)) {
            throw new SecurityException("Invalid file path: " + filePath);
        }

        // Lưu file
        Files.write(filePath, file.getBytes());

        // Trả về relative path
        return File.separator + partnerId + File.separator + type + File.separator + safeFileName;
    }


    private boolean isEmpty(String str) {
	    return str == null || str.trim().isEmpty();
	}

	private boolean isValidEmail(String email) {
	    String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
	    return email.matches(emailRegex);
	}

	private boolean isValidPhoneNumber(String phone) {
	    String phoneRegex = "^[0-9]{10}$";
	    return phone.matches(phoneRegex);
	}

	private boolean isValidBirthday(Date birthday) {
	    Date currentDate = new Date(); // Current date and time
	    return birthday != null && !birthday.after(currentDate);
	}

	private boolean isValidIdentifyCode(String identifyCode) {
	    String idRegex = "^[0-9]{9,12}$";
	    return identifyCode.matches(idRegex);
	}

//	@GetMapping("/getOptionUsers")
//	public ResponseAPI getOptionUsers() {
//		try {
//			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//			User userDetails = (User) authentication.getPrincipal();
//			Long partnerId = userDetails.getPartnerId();
//			List<RoleUserDepResponse> list = userService.findOptionRoleUserDepByPartnerId(partnerId);
//			return new ResponseAPI(list, "success", 200);
//		} catch (Exception e) {
//			return new ResponseAPI(null, "fail", 400);
//		}
//	}

    @GetMapping("/detailLog")
    public ResponseAPI logDetailAccess(@RequestParam("userId") Long userId) {
        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            User userDetails = (User) authentication.getPrincipal();
//
//            userService.detailLog(userId, userDetails);

            return new ResponseAPI(null, "Đã ghi nhận truy cập", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "Thất bại", 400);
        }
    }

//    @GetMapping("/getOptionUsersByUserId")
//    public ResponseAPI getOptionUsersByUserId() {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            User userDetails = (User) authentication.getPrincipal();
//            Long userId = userDetails.getUserId();
//            List<RoleUserDepResponse> list = userService.findOptionRoleUserDepByUserId(userId);
//            return new ResponseAPI(list, "success", 200);
//        } catch (Exception e) {
//            return new ResponseAPI(null, "fail", 400);
//        }
//    }


//    @GetMapping("/by-role")
//    public ResponseEntity<ResponseAPI> getUsersByRoleId(
//            @RequestParam("roleId") Long roleId) {
//
//        ResponseEntity<ResponseAPI> authError = validateToken();
//        if (authError != null) {
//            return authError;
//        }
//        try {
//            List<UserByRoleCodeDTO> users = userService.getUsersByRoleId(roleId);
//            return ResponseEntity.ok(new ResponseAPI(users, "Lấy danh sách user thành công", 200));
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseAPI(null, e.getMessage(), 400));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseAPI(null, "Lỗi hệ thống: " + e.getMessage(), 500));
//        }
//    }
    
    @PostMapping("/checkAndGenerateUserCode")
    public ResponseEntity<ResponseAPI> checkAndGenerateUserCode(@RequestBody Map<String, String> request) {
        ResponseEntity<ResponseAPI> authError = validateToken();
        if (authError != null) {
            return authError;
        }

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();

            String userCode = request.get("userCode");

            // Kiểm tra xem mã user đã tồn tại chưa
            boolean exists = userRepository.existsByUserCodeAndPartnerId(userCode, userDetails.getPartnerId());

            if (!exists) {
                return ResponseEntity.ok(new ResponseAPI(null, "Mã user hợp lệ", 200));
            }


            return ResponseEntity.ok(new ResponseAPI("newCode", "Mã user mới đã được sinh ra", 200));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, e.getMessage(), 400));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, "Lỗi hệ thống: " + e.getMessage(), 500));
        }
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseAPI> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Throwable rootCause = getRootCause(ex);
        String rootCauseMessage = rootCause.getMessage();
        if (rootCauseMessage != null && rootCauseMessage.contains("value too long")) {
            ResponseAPI errorResponse = new ResponseAPI(null, "Dữ liệu nhập vào quá dài, vui lòng kiểm tra lại.", 400);
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        ResponseAPI errorResponse = new ResponseAPI(null, "Lỗi toàn vẹn dữ liệu. Dữ liệu có thể bị trùng lặp hoặc không hợp lệ.", 400);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    private Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        while (cause != null && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause == null ? throwable : cause;
    }

        @GetMapping("/getAllUser")
    public ResponseAPI getAllUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            List<User> listAllUser = userService.getAllUser(userDetails.getPartnerId());
            return new ResponseAPI(listAllUser, "Thành công", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "Lỗi hệ thống", 500);
        }
    }
    @GetMapping("/LogDetailUser")
    public ResponseAPI LogDetailUser(@RequestParam UUID id) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            userService.LogDetailUser(id, userDetails);
            return new ResponseAPI(null, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }
    @PostMapping("/checkDeleteMulti")
    public ResponseAPI checkDeleteMulti(@RequestBody List<DeleteMultiDTO> ids) {
        try {
            ErrorListResponse message = userService.checkDeleteMulti(ids);
            return new ResponseAPI(message, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }



}
