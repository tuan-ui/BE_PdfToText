package com.noffice.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.*;
import com.noffice.enumtype.ActionType;
import com.noffice.enumtype.FunctionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.*;
import com.noffice.ultils.Constants;
import com.noffice.ultils.DateUtil;
import com.noffice.ultils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.noffice.dto.UserCreateDTO;
import com.noffice.dto.UserDetailDTO;
import com.noffice.ultils.Constants.upload;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ConfigRepository configRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserGroupsRepository userGroupsRepository;
    private final LogService logService;
    private final UserRolesRepository userRolesRepository;
    private final ModelMapper mapper;
    private final PartnerRepository partnerRepository;
    private final UserRolesService userRolesService;


    public Page<User> listUsers(String searchString, String userName, String fullName, String phone,
                                String birth, String userCode, User user, Pageable pageable) {
        String searchStringUnaccented = StringUtils.removeAccents(searchString);
        String fullNameUnaccented = StringUtils.removeAccents(fullName);

        // Nếu lấy tất cả dữ liệu → pageable = unpaged
        if (pageable == null || pageable.isUnpaged()) {
            pageable = Pageable.unpaged();
        }

        Page<User> users = userRepository.listUsersNative(searchStringUnaccented, userName, fullNameUnaccented, phone, birth,
                userCode, user.getPartnerId(), pageable);
        for (User u : users.getContent()) {
            List<UUID> lstRoles = userRolesRepository.getRolesByUserId(u.getId());
            if (!lstRoles.isEmpty()) {
                u.setRoleIds(lstRoles.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(",")));
            }
            if (u.getIssueDate() != null) {
                u.setIssueDateStr(DateUtil.convertStringDateFormat(u.getIssueDate().toString()));
            }
            if (u.getBirthday() != null) {
                u.setBirthdayStr(DateUtil.convertStringDateFormat(u.getBirthday().toString()));
            }
        }

        return users;
    }

    public Optional<User> findByUserId(Long userId) {
        return userRepository.findById(userId);
    }

    public UserDetailDTO getByUserId(UUID userId) {
        User result = userRepository.getUserByUserId(userId);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        UserDetailDTO userDetailDTO = mapper.map(result, UserDetailDTO.class);
        Partners partner = partnerRepository.getPartnerById(result.getPartnerId());
        userDetailDTO.setPartnerName(partner != null ? partner.getPartnerName() : null);
        return userDetailDTO;
    }

    @Transactional
    public String deleteUser(UUID id, User user, Long version) {
        User deletedUser = userRepository.getUserByUserIdIncluideDeleted(id);
        if (deletedUser == null || !Objects.equals(deletedUser.getVersion(), version)) {
            return  "error.DataChangedReload";
        } else {
            if (userGroupsRepository.existsByUserId(deletedUser.getId())) {
                return "error.UserGroupUsed";
            }
            userRepository.deleteUserByUserId(id);
            userRolesRepository.deleteByUserId(deletedUser.getId());
            logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_USER.getFunction(), "object", deletedUser.getUsername()),
                    user.getId(), deletedUser.getId(), user.getPartnerId());
        }
            return "";
    }

    @Transactional
    public String deleteMultiUser(List<DeleteMultiDTO> ids, User userDetails) {
        for (DeleteMultiDTO id : ids) {
            User user = userRepository.getUserByUserIdIncluideDeleted(id.getId());
            if (user == null || !Objects.equals(user.getVersion(), id.getVersion())) {
                return  "error.DataChangedReload";
            } else {
                if (userGroupsRepository.existsByUserId(user.getId())) {
                    return "error.UserGroupUsed";
                }
                userRepository.deleteUserByUserId(id.getId());
                userRolesRepository.deleteByUserId(id.getId());
                logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", userDetails.getFullName(), "action", FunctionType.DELETE_USER.getFunction(), "object", user.getUsername()),
                        userDetails.getId(), user.getId(), userDetails.getPartnerId());
            }
        }
        return "";
    }

    @Transactional
    public void resetPassword(UUID userId, User userDetails) {
        String newPassword = configRepository.findByKey("default_password").getValue();
        String newPasswordEncode = passwordEncoder.encode(newPassword);
        userRepository.resetPassword(userId, newPasswordEncode);
        userRepository.getUserByUserId(userId);
    }

    @Transactional
    public String updateUser(UserCreateDTO userCreateDTO, UUID partnerId, String profileImagePath, String signatureImagePath, User user, List<UUID> roleIds, Long version) {
        User existingUser = userRepository.findByUsernameIncluideDeleted(userCreateDTO.getUsername());
        if (existingUser == null || !Objects.equals(existingUser.getVersion(), version)) {
            return "error.DataChangedReload";
        } else {
            if (userCreateDTO.getUserCode() != null && !userCreateDTO.getUserCode().equals(existingUser.getUserCode())) {
               return "error.UserCodeError";
            }
            User updatedUser = mapToUserTest(userCreateDTO, existingUser, profileImagePath, signatureImagePath);
            updatedUser.setUpdateAt(LocalDateTime.now());
            updatedUser.setUpdateBy(user.getId());
            User savedUser = userRepository.save(updatedUser);
            userRolesService.saveUserRoles(savedUser.getId(), roleIds);
            logService.createLog(ActionType.UPDATE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.EDIT_USER.getFunction(), "object", savedUser.getUsername()),
                    user.getId(), savedUser.getId(), user.getPartnerId());

        }
        return "";

    }

    @Transactional
    public String createUser(UserCreateDTO userCreateDTO, UUID partnerId, String profileImagePath, String signatureImagePath, User userDetails, List<UUID> roleIds) {
        if (userRepository.existsByUsername(userCreateDTO.getUsername()) > 0) {
            return "error.DataChangedReload";
        }
        if (userRepository.existsByUserCode(userCreateDTO.getUserCode(), partnerId) > 0) {
            return "error.UserCodeDoesExist";
        }

        User user = mapToUserTest(userCreateDTO, profileImagePath, signatureImagePath);
        user.setPartnerId(partnerId);
        user.setCreateAt(LocalDateTime.now());
        user.setCreateBy(userDetails.getId());
        user.setIsActive(true);
        user.setIsDeleted(Constants.isDeleted.ACTIVE);
        user.setIsChangePassword(0);
        String newPasswordEncode = passwordEncoder.encode(userCreateDTO.getPassword());
        user.setPassword(newPasswordEncode);
        User savedUser = userRepository.save(user);
        userRolesService.saveUserRoles(savedUser.getId(), roleIds);
        logService.createLog(ActionType.CREATE.getAction(), Map.of("actor", userDetails.getFullName(), "action", FunctionType.CREATE_USER.getFunction(), "object", savedUser.getUsername()),
                userDetails.getId(), savedUser.getId(), userDetails.getPartnerId());
        return "";
    }

    private User mapToUserTest(UserCreateDTO userCreateDTO, String profileImagePath, String signatureImagePath) {
        User user = new User();
        user.setFullName(userCreateDTO.getFullname());
        user.setPhone(userCreateDTO.getPhone() != null && !userCreateDTO.getPhone().isEmpty() ? userCreateDTO.getPhone() : null);
        user.setEmail(userCreateDTO.getEmail() != null && !userCreateDTO.getEmail().isEmpty() ? userCreateDTO.getEmail() : null);
        user.setIdentifyCode(userCreateDTO.getIdentifyCode());
        user.setUsername(userCreateDTO.getUsername());
        user.setIsAdmin(userCreateDTO.getIsAdmin() ? 1 : 0);
        user.setUserCode(userCreateDTO.getUserCode());
        user.setBirthday(userCreateDTO.getBirthDay());
        user.setGender(userCreateDTO.getGender() == null ? null : userCreateDTO.getGender() == 1);
        user.setIssueDate(userCreateDTO.getIssueDate());
        user.setIssuePlace(userCreateDTO.getIssuePlace());
        user.setProfileImage(profileImagePath != null && !profileImagePath.isEmpty() ? profileImagePath : null);    // Set path if provided
        user.setSignatureImage(signatureImagePath); // Keep as is, assuming it can be null
        user.setPassword(userCreateDTO.getPassword());
        return user;
    }

    private User mapToUserTest(UserCreateDTO userCreateDTO, User existingUser, String profileImagePath, String signatureImagePath) {
        existingUser.setFullName(userCreateDTO.getFullname());
        existingUser.setPhone(userCreateDTO.getPhone() != null && !userCreateDTO.getPhone().isEmpty()
                ? userCreateDTO.getPhone() : null);
        existingUser.setEmail(userCreateDTO.getEmail() != null && !userCreateDTO.getEmail().isEmpty()
                ? userCreateDTO.getEmail() : null);
        existingUser.setIdentifyCode(userCreateDTO.getIdentifyCode());
        existingUser.setUsername(userCreateDTO.getUsername());
        existingUser.setIsAdmin(userCreateDTO.getIsAdmin() ? 1 : 0);
        existingUser.setUserCode(userCreateDTO.getUserCode());
        existingUser.setBirthday(userCreateDTO.getBirthDay());
        existingUser.setGender(userCreateDTO.getGender() == null ? null : userCreateDTO.getGender() == 1);
        existingUser.setIssueDate(userCreateDTO.getIssueDate());
        existingUser.setIssuePlace(userCreateDTO.getIssuePlace());

        // Only update images if new files are provided and not empty
        if (profileImagePath != null && !profileImagePath.isEmpty()) {
            existingUser.setProfileImage(profileImagePath);
        }
        if (signatureImagePath != null) {
            existingUser.setSignatureImage(signatureImagePath);
        }

        // Only update password if provided
        if (userCreateDTO.getPassword() != null && !userCreateDTO.getPassword().isEmpty()) {
            String newPasswordEncode = passwordEncoder.encode(userCreateDTO.getPassword());
            existingUser.setPassword(newPasswordEncode);
        }
        return existingUser;
    }

    public Resource downFile(String fileName) throws IOException {
        String uploadDir = upload.IMAGE_DIRECTORY;

        String cleanedFileName = fileName.replaceFirst("^/+", "");

        Path filePath;

        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            if (cleanedFileName.matches("^[A-Za-z]:.*")) {
                filePath = Paths.get(cleanedFileName);
            } else {
                filePath = Paths.get(uploadDir, cleanedFileName);
            }
        } else {
            filePath = Paths.get(uploadDir, cleanedFileName);
        }

        filePath = filePath.normalize();

        if (!Files.exists(filePath)) {
            throw new FileNotFoundException("Không tìm thấy file: " + filePath.toAbsolutePath());
        }

        return new FileSystemResource(filePath.toFile());
    }

    @Transactional
    public void updateUserProfile(User existingUser, String phoneNumber, String email, String profileImagePath, String signatureImagePath, UUID partnerId, String digitalCertificateName, String simCa) {
        // Update only the specified fields
        if (phoneNumber != null) {
            existingUser.setPhone(phoneNumber);
        }
        if (email != null) {
            existingUser.setEmail(email);
        }
        if (profileImagePath != null) {
            existingUser.setProfileImage(profileImagePath);
        }
        if (signatureImagePath != null) {
            existingUser.setSignatureImage(signatureImagePath);
        }
        User user = userRepository.save(existingUser);

    }


    @Transactional
    public String lockUser(UUID id, User userDetails,Long version) {
        User user = userRepository.getUserByUserIdIncluideDeleted(id);
        if (user == null || !Objects.equals(user.getVersion(), version)) {
            return  "error.DataChangedReload";
        } else {
            user.setIsActive(!user.getIsActive());
            user.setUpdateAt(LocalDateTime.now());
            user.setUpdateBy(userDetails.getId());
            User savedUser = userRepository.save(user);
            logService.createLog(savedUser.getIsActive() ? ActionType.UNLOCK.getAction() : ActionType.LOCK.getAction(),
                    Map.of("actor", userDetails.getFullName(), "action", savedUser.getIsActive() ? FunctionType.UNLOCK_USER.getFunction() : FunctionType.LOCK_USER.getFunction(), "object", savedUser.getFullName()),
                    userDetails.getId(), savedUser.getId(), userDetails.getPartnerId());
        }
        return "";
    }

    public void updateUserImages(UUID userId, String profileImagePath, String signatureImagePath) {
        User user = userRepository.getUserByUserId(userId);
        if (user != null) {
            if (profileImagePath != null) {
                user.setProfileImage(profileImagePath);
            }
            if (signatureImagePath != null) {
                user.setSignatureImage(signatureImagePath);
            }
            userRepository.save(user);
        }
    }

    public List<User> getAllUser(UUID partnerId) {
        return userRepository.findUser(partnerId);
    }

    public void getLogDetailUser(UUID id, User user) {
        User userDetail = userRepository.getUserByUserId(id);
        logService.createLog(ActionType.VIEW.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.VIEW_DETAIL_USER.getFunction(), "object", userDetail.getFullName()),
                user.getId(), userDetail.getId(), user.getPartnerId());
    }

    @Transactional
    public ErrorListResponse checkDeleteMulti(List<DeleteMultiDTO> ids) {
        ErrorListResponse response = new ErrorListResponse();
        List<ErrorListResponse.ErrorResponse> lstObject = new ArrayList<>();
        for(DeleteMultiDTO id : ids) {
            ErrorListResponse.ErrorResponse object = new ErrorListResponse.ErrorResponse();
            object.setId(id.getId());
            User user = userRepository.getUserByUserIdIncluideDeleted(id.getId());
            if(user == null) {
                object.setErrorMessage("error.DataChangedReload");
                object.setCode(id.getCode());
                object.setName(id.getName());
            } else if (userGroupsRepository.existsByUserId(user.getId())) {
                object.setErrorMessage("error.UserGroupUsed");
                object.setCode(user.getUserCode());
                object.setName(user.getFullName());
            }   else {
                object.setCode(user.getUserCode());
                object.setName(user.getFullName());
            }
            lstObject.add(object);
        }
        response.setErrors(lstObject);
        response.setTotal(ids.size());
        long countNum = response.getErrors().stream()
                .filter(item -> item.getErrorMessage()!=null)
                .count();
        response.setHasError(countNum != 0);
        if(!response.getHasError())
        {
            return null;
        }
        return response;
    }
}
