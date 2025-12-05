package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.UserCreateDTO;
import com.noffice.dto.UserDetailDTO;
import com.noffice.entity.Partners;
import com.noffice.entity.Role;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.*;
import com.noffice.ultils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private UserService userService;

    @Mock
    private UserGroupsRepository userGroupsRepository;
    @Mock
    private UserRolesRepository userRolesRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper mapper;

    @Mock
    private UserRolesService userRolesService;
    @Mock
    private PartnerRepository partnerRepository;
    @Mock
    private User mockUser;
    private User sampleUser;
    private UUID userId;
    private UUID partnerId;
    private UserCreateDTO dto;

    @BeforeEach
    void setUp() {
        partnerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        userId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setFullName("Nguyễn Văn Test");
        mockUser.setPartnerId(partnerId);

        sampleUser = new User();
        sampleUser.setId(userId);
        sampleUser.setFullName("Test User");
        sampleUser.setUsername("User name");
        sampleUser.setUserCode("TEST001");
        Date issueDate = Date.from(
                LocalDate.of(2000, 1, 1)
                        .atStartOfDay(ZoneId.of("Asia/Ho_Chi_Minh"))
                        .toInstant()
        );

        sampleUser.setIssueDate(issueDate);
        sampleUser.setBirthday(issueDate);
        sampleUser.setIsActive(true);
        sampleUser.setIsDeleted(Constants.isDeleted.ACTIVE);
        sampleUser.setVersion(1L);
        sampleUser.setPartnerId(partnerId);

        dto = new UserCreateDTO();
        dto.setUsername("newuser");
        dto.setUserCode("USER001");
        dto.setFullname("Nguyen Van A");
        dto.setPhone("0901234567");
        dto.setEmail("a@example.com");
        dto.setIdentifyCode("123456789");
        dto.setPassword("Pass@123");
        dto.setIsAdmin(false);
        dto.setBirthDay(java.sql.Date.valueOf("1990-01-01"));
        dto.setGender(1);
        dto.setIssueDate(java.sql.Date.valueOf("2010-05-20"));
        dto.setIssuePlace("Hà Nội");
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(sampleUser);
        Mockito.doNothing().when(userRepository).deleteUserByUserId(any(UUID.class));

        String result = userService.deleteUser(userId, mockUser, 1L);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.DELETE.getAction()), anyMap(), eq(mockUser.getId()), eq(userId), eq(partnerId));
    }

    @Test
    void deleteUser_VersionMismatch() {
        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(sampleUser);

        String result = userService.deleteUser(userId, mockUser, 999L);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteUser_existsByUserId() {
        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(sampleUser);
        when(userGroupsRepository.existsByUserId(any())).thenReturn(true);
        String result = userService.deleteUser(userId, mockUser, 1L);

        assertEquals("error.UserGroupUsed", result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteUser_existsUserByUserId() {
        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(sampleUser);
        when(userGroupsRepository.existsByUserId(any())).thenReturn(false);
        when(userRolesRepository.existsUserByUserId(any())).thenReturn(true);

        String result = userService.deleteUser(userId, mockUser, 1L);

        assertEquals("error.UserRolesUsed", result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteMultiUser_Success() {
        UUID id2 = UUID.randomUUID();
        User user2 = new User();
        user2.setId(id2);
        user2.setFullName("User 2");
        user2.setUserCode("TEST002");
        user2.setUsername("User 2");
        user2.setVersion(1L);
        user2.setIsDeleted(Constants.isDeleted.ACTIVE);
        user2.setPartnerId(partnerId);

        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(userId, "name", "code", 1L),
                new DeleteMultiDTO(id2, "name", "code", 1L)
        );

        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(sampleUser);
        when(userRepository.getUserByUserIdIncluideDeleted(eq(id2))).thenReturn(user2);
        Mockito.doNothing().when(userRepository).deleteUserByUserId(any(UUID.class));

        String result = userService.deleteMultiUser(ids, mockUser);

        assertEquals("", result);
        verify(logService, times(2)).createLog(anyString(), anyMap(), any(), any(), any());
    }

    @Test
    void deleteMultiUser_OneVersionMismatch() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(userId, "name", "code", 999L)
        );

        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(sampleUser);

        String result = userService.deleteMultiUser(ids, mockUser);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteMultiUser_existsByUserId() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(userId, "name", "code", 1L)
        );

        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(sampleUser);
        when(userGroupsRepository.existsByUserId(any())).thenReturn(true);
        String result = userService.deleteMultiUser(ids, mockUser);

        assertEquals("error.UserGroupUsed", result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteMultiUser_existsUserByUserId() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(userId, "name", "code", 1L)
        );

        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(sampleUser);
        when(userGroupsRepository.existsByUserId(any())).thenReturn(false);
        when(userRolesRepository.existsUserByUserId(any())).thenReturn(true);
        String result = userService.deleteMultiUser(ids, mockUser);

        assertEquals("error.UserRolesUsed", result);
        verifyNoInteractions(logService);
    }

    @Test
    void lockUser_LockSuccess() {
        sampleUser.setIsActive(true);
        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(sampleUser);
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        String result = userService.lockUser(userId, mockUser, 1L);

        assertEquals("", result);
        assertFalse(sampleUser.getIsActive());
        verify(logService).createLog(eq(ActionType.LOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUser_UnlockSuccess() {
        sampleUser.setIsActive(false);
        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(sampleUser);
        when(userRepository.save(any())).thenReturn(sampleUser);

        userService.lockUser(userId, mockUser, 1L);

        assertTrue(sampleUser.getIsActive());
        verify(logService).createLog(eq(ActionType.UNLOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUser_Error() {
        sampleUser.setIsActive(false);
        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(null);

        String result = userService.lockUser(userId, mockUser, 1L);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
    }

    @Test
    void getListUser_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> page = new PageImpl<>(List.of(sampleUser));

        when(userRepository.listUsersNative(any(), any(), any(), any(), any(), any(), eq(partnerId), eq(pageable)))
                .thenReturn(page);
        when(userRolesRepository.getRolesByUserId(any())).thenReturn(List.of(UUID.fromString("11111111-1111-1111-1111-111111111111"), UUID.fromString("22222222-2222-2222-2222-222222222222")));
        when(roleRepository.findByRoleId(any())).thenReturn(Optional.of(new Role()));
        Page<User> result = userService.listUsers("test", "code", "name", "desc", "01/01/2000", "userCode", sampleUser, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test User", result.getContent().get(0).getFullName());
    }

    @Test
    void getAllUser_ReturnsList() {
        when(userRepository.findUser(eq(partnerId))).thenReturn(List.of(sampleUser));

        List<User> result = userService.getAllUser(partnerId);

        assertEquals(1, result.size());
    }

    @Test
    void getLogDetailUser_LogsView() {
        when(userRepository.getUserByUserId(any())).thenReturn(sampleUser);

        userService.getLogDetailUser(userId, mockUser);

        verify(logService).createLog(eq(ActionType.VIEW.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void checkDeleteMulti_HasError() {
        sampleUser.setVersion(999L); // version không khớp
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(userId, "name", "code", 1L));

        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(null);

        ErrorListResponse result = userService.checkDeleteMulti(ids);

        assertTrue(result.getHasError());
        assertEquals(Constants.errorResponse.DATA_CHANGED, result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void checkDeleteMulti_NoError_ReturnsNull() {
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(userId, "name", "code", 1L));

        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(sampleUser);

        ErrorListResponse result = userService.checkDeleteMulti(ids);

        assertNull(result);
    }

    @Test
    void checkDeleteMulti_existsByUser() {
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(userId, "name", "code", 1L));

        when(userRepository.getUserByUserIdIncluideDeleted(eq(userId))).thenReturn(sampleUser);
        when(userGroupsRepository.existsByUserId(any())).thenReturn(true);

        ErrorListResponse result = userService.checkDeleteMulti(ids);

        assertTrue(result.getHasError());
        assertEquals("error.UserGroupUsed", result.getErrors().get(0).getErrorMessage());

    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByUsername("newuser")).thenReturn(0);
        when(userRepository.existsByUserCode("USER001", partnerId)).thenReturn(0);
        when(passwordEncoder.encode("Pass@123")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        String result = userService.createUser(dto, partnerId, "profile/path.jpg", "sign/path.png", mockUser, List.of(UUID.randomUUID()));

        assertEquals("", result); // thành công → trả rỗng

        verify(userRepository).save(argThat(user -> {
            assertEquals("newuser", user.getUsername());
            assertEquals("USER001", user.getUserCode());
            assertEquals(partnerId, user.getPartnerId());
            assertEquals("encodedPass", user.getPassword());
            assertEquals("profile/path.jpg", user.getProfileImage());
            assertEquals("sign/path.png", user.getSignatureImage());
            assertNotNull(user.getCreateAt());
            assertEquals(mockUser.getId(), user.getCreateBy());
            assertTrue(user.getIsActive());
            return true;
        }));

    }

    @Test
    void createUser_UsernameExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(1);

        String result = userService.createUser(dto, partnerId, null, null, mockUser, List.of());

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_UserCodeExists() {
        when(userRepository.existsByUsername(anyString())).thenReturn(0);
        when(userRepository.existsByUserCode("USER001", partnerId)).thenReturn(1);

        String result = userService.createUser(dto, partnerId, null, null, mockUser, List.of());

        assertEquals("error.UserCodeDoesExist", result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_Success_NoUserCodeChange() {
        User existing = new User();
        existing.setId(mockUser.getId());
        existing.setUsername("newuser");
        existing.setUserCode("USER001");
        existing.setVersion(5L);

        when(userRepository.findByUsernameIncluideDeleted("newuser")).thenReturn(existing);
        when(passwordEncoder.encode(anyString())).thenReturn("newEncodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        dto.setFullname("Tên mới");
        dto.setPassword("NewPass@456");

        String result = userService.updateUser(dto, partnerId, "new-profile.jpg", "new-sign.png",
                mockUser, List.of(UUID.randomUUID()), 5L);

        assertEquals("", result);

        verify(userRepository).save(argThat(u -> {
            assertEquals("Tên mới", u.getFullName());
            assertEquals("newEncodedPass", u.getPassword());
            assertEquals("new-profile.jpg", u.getProfileImage());
            assertEquals("new-sign.png", u.getSignatureImage());
            assertNotNull(u.getUpdateAt());
            assertEquals(mockUser.getId(), u.getUpdateBy());
            return true;
        }));
    }

    @Test
    void updateUser_ChangeUserCode_ReturnsError() {
        User existing = new User();
        existing.setUserCode("OLD001");
        existing.setVersion(10L);

        when(userRepository.findByUsernameIncluideDeleted(anyString())).thenReturn(existing);
        dto.setUserCode("NEW001"); // cố tình đổi

        String result = userService.updateUser(dto, partnerId, null, null, mockUser, List.of(), 10L);

        assertEquals("error.UserCodeError", result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_OptimisticLockFailure() {
        User existing = new User();
        existing.setVersion(99L);

        when(userRepository.findByUsernameIncluideDeleted(anyString())).thenReturn(existing);

        String result = userService.updateUser(dto, partnerId, null, null, mockUser, List.of(), 88L);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getByUserId_Success_WithPartner() {
        Partners partner = new Partners();
        partner.setPartnerName("Công ty ABC");

        when(userRepository.getUserByUserId(userId)).thenReturn(mockUser);
        when(partnerRepository.getPartnerById(mockUser.getPartnerId())).thenReturn(partner);
        when(mapper.map(mockUser, UserDetailDTO.class)).thenReturn(new UserDetailDTO());

        UserDetailDTO result = userService.getByUserId(userId);

        assertNotNull(result);
        assertEquals("Công ty ABC", result.getPartnerName());
    }

    @Test
    void getByUserId_PartnerNull_ReturnsNullPartnerName() {
        when(userRepository.getUserByUserId(userId)).thenReturn(mockUser);
        when(partnerRepository.getPartnerById(any())).thenReturn(null);

        UserDetailDTO userDetailDTO = new UserDetailDTO();
        when(mapper.map(any(), eq(UserDetailDTO.class))).thenReturn(userDetailDTO);

        UserDetailDTO result = userService.getByUserId(userId);

        assertNull(result.getPartnerName());
    }

    @Test
    void downFile_FileExists_ReturnsResource() throws IOException {
        // Bước 1: Mock Files.exists() để trả về true/false theo ý muốn
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(true);

            // Bước 2: Mock FileSystemResource khi được tạo
            try (MockedConstruction<FileSystemResource> mocked = mockConstruction(FileSystemResource.class,
                    (mock, context) -> {
                        when(mock.exists()).thenReturn(true);
                        when(mock.isReadable()).thenReturn(true);
                        when(mock.getFilename()).thenReturn("user123.jpg");
                        when(mock.getInputStream())
                                .thenReturn(new ByteArrayInputStream("fake image".getBytes()));
                    })) {

                Resource result = userService.downFile("profile/user123.jpg");

                assertNotNull(result);
                assertTrue(result.exists());
                assertEquals("user123.jpg", result.getFilename());
                assertEquals("fake image", new String(result.getInputStream().readAllBytes()));
            }
        }
    }

    @Test
    void downFile_FileNotFound_ThrowsFromService() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);

            FileNotFoundException ex = assertThrows(FileNotFoundException.class, () ->
                    userService.downFile("not-exist.png")
            );

            assertTrue(ex.getMessage().contains("Không tìm thấy file"));
        }
    }

    @Test
    void downFile_PathTraversal_Safe() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.exists(any(Path.class))).thenReturn(false);

            assertThrows(FileNotFoundException.class, () ->
                    userService.downFile("../../etc/passwd")
            );
            assertThrows(FileNotFoundException.class, () ->
                    userService.downFile("..\\windows\\win.ini")
            );
        }
    }

    @Test
    void downFile_FileNotFound_ThrowsException() {
        try (MockedConstruction<FileSystemResource> mocked = mockConstruction(FileSystemResource.class,
                (mock, context) -> when(mock.exists()).thenReturn(false))) {

            FileNotFoundException ex = assertThrows(FileNotFoundException.class, () ->
                    userService.downFile("not-exist.png")
            );

            assertTrue(ex.getMessage().contains("Không tìm thấy file"));
        }
    }

    @Test
    void updateUserImages_BothImages_Updated() {
        when(userRepository.getUserByUserId(userId)).thenReturn(mockUser);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        userService.updateUserImages(userId, "new-profile.jpg", "new-sign.png");

        assertEquals("new-profile.jpg", mockUser.getProfileImage());
        assertEquals("new-sign.png", mockUser.getSignatureImage());
        verify(userRepository).save(mockUser);
    }

    @Test
    void updateUserImages_OnlyProfile_Updated() {
        mockUser.setSignatureImage("old-sign.png");
        when(userRepository.getUserByUserId(userId)).thenReturn(mockUser);

        userService.updateUserImages(userId, "only-profile.jpg", null);

        assertEquals("only-profile.jpg", mockUser.getProfileImage());
        assertEquals("old-sign.png", mockUser.getSignatureImage()); // không đổi
    }

    @Test
    void updateUserImages_UserNotFound_DoNothing() {
        when(userRepository.getUserByUserId(userId)).thenReturn(null);

        userService.updateUserImages(userId, "a.jpg", "b.png");

        verify(userRepository, never()).save(any());
    }
}
