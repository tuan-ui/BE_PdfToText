package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.*;
import com.noffice.enumtype.ActionType;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.UserGroupResponse;
import com.noffice.repository.UserGroupRepository;
import com.noffice.repository.UserGroupsRepository;
import com.noffice.repository.UserRepository;
import com.noffice.ultils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserGroupServiceTest {

    @Mock
    private UserGroupRepository userGroupRepository;
    @Mock
    private UserGroupsRepository userGroupsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LogService logService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @InjectMocks
    private UserGroupService userGroupService;

    private final UUID mockUserId = UUID.randomUUID();
    private final UUID mockPartnerId = UUID.randomUUID();
    private final UUID mockGroupId = UUID.randomUUID();
    private UserGroup sampleUserGroup;
    private final Long mockVersion = 1L;

    @BeforeEach
    void setUp() {
        // Thiết lập người dùng giả mạo trong Security Context
        User mockUser = new User();
        mockUser.setId(mockUserId);
        mockUser.setPartnerId(mockPartnerId);
        mockUser.setFullName("Test User");

        Authentication authentication = new TestingAuthenticationToken(
                mockUser,
                null,
                "ROLE_USER"
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Thiết lập đối tượng UserGroup mẫu
        sampleUserGroup = new UserGroup();
        sampleUserGroup.setId(mockGroupId);
        sampleUserGroup.setVersion(mockVersion);
        sampleUserGroup.setGroupCode("TEST_CODE");
        sampleUserGroup.setGroupName("Test Group");
        sampleUserGroup.setPartnerId(mockPartnerId);
        sampleUserGroup.setIsActive(true);
    }

    // --- 1. saveUserGroup (Tạo mới) ---

    @Test
    void saveUserGroup_Create_Success() {
        // Arrange
        String newGroupName = "New Group";
        String newGroupCode = "NEW_CODE";
        List<UUID> userIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        UserGroup newUserGroup = new UserGroup();
        newUserGroup.setId(mockGroupId);
        newUserGroup.setGroupName("New Group");

        // Mock: Kiểm tra GroupCode không tồn tại
        when(userGroupRepository.getUserGroupByCode(newGroupCode)).thenReturn(null);

        // Mock: Lưu UserGroup
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(newUserGroup);

        // Act
        String result = userGroupService.saveUserGroup(null, newGroupName, newGroupCode, userIds, null);

        // Assert
        assertEquals("", result, "Nên trả về chuỗi rỗng khi thành công");

        // Verify: Kiểm tra Repository được gọi đúng
        verify(userGroupRepository, times(1)).getUserGroupByCode(newGroupCode);
        verify(userGroupRepository, times(1)).save(any(UserGroup.class));
        verify(userGroupsRepository, times(1)).deleteByGroupId(mockGroupId);
        verify(userGroupsRepository, times(1)).saveAll(anyList());
        verify(logService, times(1)).createLog(anyString(), anyMap(), eq(mockUserId), eq(mockGroupId), eq(mockPartnerId));
    }

    @Test
    void saveUserGroup_Create_GroupCodeAlreadyExists_ShouldReturnError() {
        // Arrange
        String existingGroupCode = "EXISTING_CODE";

        // Mock: Kiểm tra GroupCode đã tồn tại
        when(userGroupRepository.getUserGroupByCode(existingGroupCode)).thenReturn(new UserGroup());

        // Act
        String result = userGroupService.saveUserGroup(null, "Name", existingGroupCode, Collections.emptyList(), null);

        // Assert
        assertEquals("error.UserGroupDoesExist", result, "Nên trả về lỗi tồn tại GroupCode");

        // Verify: Kiểm tra các repository khác không được gọi
        verify(userGroupRepository, never()).findById(any());
        verify(userGroupRepository, never()).save(any(UserGroup.class));
        verify(logService, never()).createLog(anyString(), anyMap(), any(), any(), any());
    }

    // --- 2. saveUserGroup (Cập nhật) ---

    @Test
    void saveUserGroup_Update_Success() {
        // Arrange
        UUID groupId = mockGroupId;
        Long currentVersion = mockVersion;
        List<UUID> userIds = List.of(UUID.randomUUID());

        // Mock: Tìm thấy UserGroup với Version khớp
        when(userGroupRepository.findById(groupId)).thenReturn(Optional.of(sampleUserGroup));
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(sampleUserGroup);

        // Act
        String result = userGroupService.saveUserGroup(groupId, "Updated Name", "NEW_CODE", userIds, currentVersion);

        // Assert
        assertEquals("", result, "Nên trả về chuỗi rỗng khi cập nhật thành công");

        // Verify: Kiểm tra LogService ghi lại hành động UPDATE
        verify(logService, times(1)).createLog(anyString(), anyMap(), eq(mockUserId), eq(groupId), eq(mockPartnerId));
        verify(userGroupRepository, times(1)).save(argThat(group ->
                group.getGroupName().equals("Updated Name") && group.getGroupCode().equals("NEW_CODE")
        ));
    }

    @Test
    void saveUserGroup_Update_VersionMismatch_ShouldReturnError() {
        // Arrange
        UUID groupId = mockGroupId;
        Long staleVersion = 0L;

        // Mock: Tìm thấy UserGroup nhưng Version không khớp
        when(userGroupRepository.findById(groupId)).thenReturn(Optional.of(sampleUserGroup));

        // Act
        String result = userGroupService.saveUserGroup(groupId, "Updated Name", "NEW_CODE", Collections.emptyList(), staleVersion);

        // Assert
        assertEquals(Constants.errorResponse.DATA_CHANGED, result);

        // Verify: Không có thao tác lưu hoặc ghi log nào xảy ra
        verify(userGroupRepository, never()).save(any(UserGroup.class));
        verify(logService, never()).createLog(anyString(), anyMap(), any(), any(), any());
    }

    // --- 3. searchUserGroups ---

    @Test
    void searchUserGroups_Success_WithUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id"));
        UUID userId1 = UUID.randomUUID();

        // Mock: Dữ liệu UserGroup từ Repository
        Page<UserGroup> mockPage = new PageImpl<>(List.of(sampleUserGroup), pageable, 1L);
        when(userGroupRepository.searchUserGroup(eq(mockPartnerId), anyString(), anyString(), anyString(), any(Boolean.class), eq(pageable)))
                .thenReturn(mockPage);

        // Mock: UserIds từ UserGroupsRepository
        List<UUID> userIds = List.of(userId1);
        when(userGroupsRepository.findUserIdsByGroupId(mockGroupId)).thenReturn(userIds);

        // Mock: Dữ liệu User từ UserRepository
        User user1 = new User();
        user1.setId(userId1);
        user1.setFullName("User One");
        when(userRepository.findAllById(userIds)).thenReturn(List.of(user1));

        // Act
        Page<UserGroupResponse> resultPage = userGroupService.searchUserGroups(mockPartnerId, "search", "code", "name", true, pageable);

        // Assert
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
        UserGroupResponse response = resultPage.getContent().get(0);
        assertEquals(sampleUserGroup.getGroupName(), response.getGroupName());
        assertFalse(response.getUsers().isEmpty());
        assertEquals("User One", response.getUsers().get(0).getFullName());

        // Verify
        verify(userGroupsRepository, times(1)).findUserIdsByGroupId(mockGroupId);
        verify(userRepository, times(1)).findAllById(userIds);
    }

    @Test
    void searchUserGroups_Success_NoUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<UserGroup> mockPage = new PageImpl<>(List.of(sampleUserGroup), pageable, 1L);
        when(userGroupRepository.searchUserGroup(any(), any(), any(), any(), any(), any()))
                .thenReturn(mockPage);

        // Mock: Không có UserIds
        when(userGroupsRepository.findUserIdsByGroupId(mockGroupId)).thenReturn(Collections.emptyList());

        // Act
        Page<UserGroupResponse> resultPage = userGroupService.searchUserGroups(mockPartnerId, "", "", "", null, pageable);

        // Assert
        assertNotNull(resultPage);
        UserGroupResponse response = resultPage.getContent().get(0);
        assertTrue(response.getUsers().isEmpty());

    }

    // --- 4. updateUserGroupStatus (Khóa/Mở khóa) ---

    @Test
    void updateUserGroupStatus_Lock_Success() {
        // Arrange
        sampleUserGroup.setIsActive(true); // Trạng thái ban đầu là UNLOCK

        when(userGroupRepository.findByIdIncludeDeleted(mockGroupId)).thenReturn(sampleUserGroup);
        when(userGroupRepository.save(any(UserGroup.class))).thenReturn(sampleUserGroup);

        // Act
        String result = userGroupService.updateUserGroupStatus(mockGroupId, mockVersion);

        // Assert
        assertEquals("", result);
        assertFalse(sampleUserGroup.getIsActive()); // Kiểm tra đã chuyển thành LOCK (false)

        // Verify
        verify(userGroupRepository, times(1)).save(sampleUserGroup);
        verify(logService).createLog(eq(ActionType.LOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void updateUserGroupStatus_NotFoundOrVersionMismatch_ShouldReturnError() {
        // Arrange
        Long wrongVersion = 0L;

        // Mock: Giả lập không tìm thấy hoặc Version không khớp
        when(userGroupRepository.findByIdIncludeDeleted(mockGroupId)).thenReturn(sampleUserGroup);

        // Act
        String result = userGroupService.updateUserGroupStatus(mockGroupId, wrongVersion);

        // Assert
        assertEquals(Constants.errorResponse.DATA_CHANGED, result);

        // Verify: Không có thao tác lưu hoặc ghi log nào xảy ra
        verify(userGroupRepository, never()).save(any(UserGroup.class));
        verify(logService, never()).createLog(anyString(), anyMap(), any(), any(), any());
    }

    // --- 5. deleteUserGroup ---

    @Test
    void deleteUserGroup_Success() {
        // Arrange
        when(userGroupRepository.findByIdIncludeDeleted(mockGroupId)).thenReturn(sampleUserGroup);
        when(userGroupsRepository.existsUserByGroupId(mockGroupId)).thenReturn(false); // Chưa được sử dụng

        // Act
        String result = userGroupService.deleteUserGroup(mockGroupId, mockVersion);

        // Assert
        assertEquals("", result);

        // Verify
        verify(userGroupRepository, times(1)).deleteUserGroupByUserGroupId(mockGroupId);
        verify(logService).createLog(eq(ActionType.DELETE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void deleteUserGroup_InUse_ShouldReturnError() {
        // Arrange
        when(userGroupRepository.findByIdIncludeDeleted(mockGroupId)).thenReturn(sampleUserGroup);
        when(userGroupsRepository.existsUserByGroupId(mockGroupId)).thenReturn(true); // Đang được sử dụng

        // Act
        String result = userGroupService.deleteUserGroup(mockGroupId, mockVersion);

        // Assert
        assertEquals("error.UserGroupsUsed", result);

        // Verify: Thao tác xóa không được gọi
        verify(userGroupRepository, never()).deleteUserGroupByUserGroupId(any());
        verify(logService, never()).createLog(anyString(), anyMap(), any(), any(), any());
    }

    // --- 6. checkDeleteMulti ---

    @Test
    void checkDeleteMulti_AllValid_ShouldReturnNull() {
        // Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(id1, "Code1", "Name1", 1L ),
                new DeleteMultiDTO(id2, "Code2", "Name2", 1L)
        );

        // Mock: UserGroup tồn tại và không được sử dụng
        when(userGroupRepository.findByIdIncludeDeleted(any(UUID.class))).thenReturn(sampleUserGroup);
        when(userGroupsRepository.existsUserByGroupId(any(UUID.class))).thenReturn(false);

        // Act
        ErrorListResponse result = userGroupService.checkDeleteMulti(ids);

        // Assert
        assertNull(result, "Khi không có lỗi, nên trả về null");

        // Verify
        verify(userGroupRepository, times(2)).findByIdIncludeDeleted(any(UUID.class));
        verify(userGroupsRepository, times(2)).existsUserByGroupId(any(UUID.class));
    }

    @Test
    void checkDeleteMulti_MixedErrors_ShouldReturnErrorList() {
        // Arrange
        UUID id1Used = UUID.randomUUID();
        UUID id2NotFound = UUID.randomUUID();
        UUID id3Valid = UUID.randomUUID();

        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(id1Used, "Code1", "Name1", 1L ),
                new DeleteMultiDTO(id2NotFound, "Code2", "Name2", 1L ),
                new DeleteMultiDTO(id3Valid, "Code3", "Name3", 1L )
        );

        // Mock UserGroup
        UserGroup group1 = new UserGroup(); group1.setGroupCode("CODE1"); group1.setGroupName("NAME1");
        UserGroup group3 = new UserGroup(); group3.setGroupCode("CODE3"); group3.setGroupName("NAME3");

        when(userGroupRepository.findByIdIncludeDeleted(id1Used)).thenReturn(group1);
        when(userGroupRepository.findByIdIncludeDeleted(id2NotFound)).thenReturn(null); // Not found error
        when(userGroupRepository.findByIdIncludeDeleted(id3Valid)).thenReturn(group3);

        // Mock Usage
        when(userGroupsRepository.existsUserByGroupId(id1Used)).thenReturn(true); // Used error
        when(userGroupsRepository.existsUserByGroupId(id3Valid)).thenReturn(false); // Valid

        // Act
        ErrorListResponse response = userGroupService.checkDeleteMulti(ids);

        // Assert
        assertNotNull(response);
        assertTrue(response.getHasError());
        assertEquals(3, response.getTotal());
        assertEquals(3, response.getErrors().size());

        // Kiểm tra lỗi 1 (Used)
        assertEquals("error.UserGroupAlreadyUseOnUser", response.getErrors().get(0).getErrorMessage());

        // Kiểm tra lỗi 2 (Not Found)
        assertEquals(Constants.errorResponse.DATA_CHANGED, response.getErrors().get(1).getErrorMessage());

        // Kiểm tra lỗi 3 (Valid)
        assertNull(response.getErrors().get(2).getErrorMessage());
    }
}