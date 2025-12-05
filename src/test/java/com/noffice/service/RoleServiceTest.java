package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.RoleDTO;
import com.noffice.entity.Permission;
import com.noffice.entity.Role;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.PermissionsRepository;
import com.noffice.repository.RoleRepository;
import com.noffice.repository.UserRolesRepository;
import com.noffice.ultils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private RoleService roleService;
    @Mock
    private UserRolesRepository userRolesRepository;
    @Mock
    private PermissionsRepository permissionsRepository;

    private User mockUser;
    private Role sampleRole;
    private UUID roleId;
    private UUID partnerId;
    private RoleDTO sampleRoleDTO;

    @BeforeEach
    void setUp() {
        partnerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        roleId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setFullName("Nguyễn Văn Test");
        mockUser.setPartnerId(partnerId);

        sampleRole = new Role();
        sampleRole.setId(roleId);
        sampleRole.setRoleName("Test Role");
        sampleRole.setRoleCode("TEST001");
        sampleRole.setIsActive(true);
        sampleRole.setIsDeleted(Constants.isDeleted.ACTIVE);
        sampleRole.setVersion(1L);
        sampleRole.setPartnerId(partnerId);

        sampleRoleDTO = new RoleDTO();
        sampleRoleDTO.setId(roleId);
        sampleRoleDTO.setRoleName("Test Role");
        sampleRoleDTO.setRoleCode("TEST001");
        sampleRoleDTO.setIsActive(true);
        sampleRoleDTO.setIsDeleted(Constants.isDeleted.ACTIVE);
        sampleRoleDTO.setVersion(1L);
        sampleRoleDTO.setPartnerId(partnerId);
    }

    @Test
    void deleteRole_Faild() {
        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(sampleRole);
        when(userRolesRepository.existsUserByRoleId(eq(roleId))).thenReturn(true);
        String result = roleService.delete(roleId, mockUser, 1L);

        assertEquals("error.RoleAlreadyUseOnUser", result);
    }

    @Test
    void deleteRole_Success() {
        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(sampleRole);
        Mockito.doNothing().when(roleRepository).deleteRoleByRoleId(any(UUID.class));
        when(userRolesRepository.existsUserByRoleId(eq(roleId))).thenReturn(false);
        String result = roleService.delete(roleId, mockUser, 1L);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.DELETE.getAction()), anyMap(), eq(mockUser.getId()), eq(roleId), eq(partnerId));
    }

    @Test
    void deleteRole_VersionMismatch() {
        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(sampleRole);

        String result = roleService.delete(roleId, mockUser, 999L);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteMultiRole_Success() {
        UUID id2 = UUID.randomUUID();
        Role role2 = new Role();
        role2.setId(id2);
        role2.setRoleName("Role 2");
        role2.setRoleCode("TEST002");
        role2.setVersion(1L);
        role2.setIsDeleted(Constants.isDeleted.ACTIVE);
        role2.setPartnerId(partnerId);

        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(roleId,"name","code", 1L),
                new DeleteMultiDTO(id2,"name","code", 1L)
        );

        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(sampleRole);
        when(roleRepository.findByRoleIdIncluideDeleted(eq(id2))).thenReturn(role2);
        when(userRolesRepository.existsUserByRoleId(eq(roleId))).thenReturn(false);
        Mockito.doNothing().when(roleRepository).deleteRoleByRoleId(any(UUID.class));

        String result = roleService.deleteMuti(ids, mockUser);

        assertEquals("", result);
        verify(logService, times(2)).createLog(anyString(), anyMap(), any(), any(), any());
    }

    @Test
    void deleteMultiRole_Faild() {
        UUID id2 = UUID.randomUUID();
        Role role2 = new Role();
        role2.setId(id2);
        role2.setRoleName("Role 2");
        role2.setRoleCode("TEST002");
        role2.setVersion(1L);
        role2.setIsDeleted(Constants.isDeleted.ACTIVE);
        role2.setPartnerId(partnerId);

        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(roleId,"name","code", 1L),
                new DeleteMultiDTO(id2,"name","code", 1L)
        );

        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(sampleRole);
        when(userRolesRepository.existsUserByRoleId(eq(roleId))).thenReturn(true);

        String result = roleService.deleteMuti(ids, mockUser);

        assertEquals("error.RoleAlreadyUseOnUser", result);
    }

    @Test
    void deleteMultiRole_OneVersionMismatch() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(roleId,"name","code", 999L)
        );

        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(sampleRole);

        String result = roleService.deleteMuti(ids, mockUser);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
        verifyNoInteractions(logService);
    }

    @Test
    void lockRole_LockSuccess() {
        sampleRole.setIsActive(true);
        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(sampleRole);
        when(roleRepository.save(any(Role.class))).thenReturn(sampleRole);

        String result = roleService.lockRole(roleId, mockUser, 1L);

        assertEquals("", result);
        assertFalse(sampleRole.getIsActive());
        verify(logService).createLog(eq(ActionType.LOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockRole_UnlockSuccess() {
        sampleRole.setIsActive(false);
        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(sampleRole);
        when(roleRepository.save(any())).thenReturn(sampleRole);

        roleService.lockRole(roleId, mockUser, 1L);

        assertTrue(sampleRole.getIsActive());
        verify(logService).createLog(eq(ActionType.UNLOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockRole_Error() {
        sampleRole.setIsActive(false);
        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(null);

        String result = roleService.lockRole(roleId, mockUser, 1L);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
    }

    @Test
    void saveRole_CreateSuccess() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setRoleName("New Role");
        roleDTO.setRoleCode("NEW001");
        roleDTO.setIsActive(true);


        when(roleRepository.existsByRoleCodeIgnoreCase(eq("NEW001"), eq(partnerId))).thenReturn(false);
        when(roleRepository.save(any(Role.class))).thenAnswer(i -> i.getArgument(0));

        String result = roleService.save(roleDTO, mockUser);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.CREATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void saveRole_CodeExists() {
        Role roleDTO = new Role();
        roleDTO.setRoleCode("TEST001");

        when(roleRepository.existsByRoleCodeIgnoreCase(eq("TEST001"), eq(partnerId))).thenReturn(true);

        String result = roleService.save(sampleRoleDTO, mockUser);

        assertEquals("error.SameRoleCode", result);
        verifyNoInteractions(logService);
    }

    @Test
    void updateRole_Success() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(roleId);
        roleDTO.setRoleName("Updated Name");
        roleDTO.setRoleCode("UPDATED");
        roleDTO.setVersion(1L);
        roleDTO.setIsActive(false);


        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(sampleRole);
        when(roleRepository.save(any(Role.class))).thenReturn(sampleRole);

        String result = roleService.update(roleDTO, mockUser);

        assertEquals("", result);
        assertEquals("Updated Name", roleDTO.getRoleName());
        verify(logService).createLog(eq(ActionType.UPDATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void updateRole_Faild() {
        Role roleDTO = new Role();
        roleDTO.setId(roleId);
        roleDTO.setRoleName("Updated Name");
        roleDTO.setRoleCode("UPDATED");
        roleDTO.setVersion(1L);
        roleDTO.setIsActive(false);


        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(roleDTO);
        when(roleRepository.existsByRoleCodeIgnoreCaseNotId(anyString(),any(),any())).thenReturn(true);

        String result = roleService.update(sampleRoleDTO, mockUser);

        assertEquals("error.SameRoleCode", result);
        assertEquals("Updated Name", roleDTO.getRoleName());
    }

    @Test
    void updateRole_Error() {
        Role roleDTO = new Role();
        roleDTO.setId(roleId);

        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(null);

        String result = roleService.update(sampleRoleDTO, mockUser);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
    }

    @Test
    void getListRole_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<RoleDTO> page = new PageImpl<>(List.of(sampleRoleDTO));

        when(roleRepository.searchRoles(any(), any(), any(), any(), eq(partnerId),any(), eq(pageable)))
                .thenReturn(page);

        Page<RoleDTO> result = roleService.searchRoles("test", "code", "name", "desc", partnerId, true, 0,10);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Role", result.getContent().get(0).getRoleName());
    }

    @Test
    void getAllRole_ReturnsList() {
        when(roleRepository.findByOnlyPartnerId(eq(partnerId))).thenReturn(List.of(sampleRole));

        List<Role> result = roleService.getAllRole(partnerId);

        assertEquals(1, result.size());
    }

    @Test
    void getLogDetailRole_LogsView() {
        when(roleRepository.findByRoleId(roleId)).thenReturn(Optional.ofNullable(sampleRole));

        roleService.getLogDetailRole(roleId, mockUser);

        verify(logService).createLog(eq(ActionType.VIEW.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void checkDeleteMulti_HasError() {
        sampleRole.setVersion(999L); // version không khớp
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(roleId,"name","code", 1L));

        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(null);

        ErrorListResponse result = roleService.checkDeleteMulti(ids);

        assertTrue(result.getHasError());
        assertEquals(Constants.errorResponse.DATA_CHANGED, result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void checkDeleteMulti_HasError2() {
        sampleRole.setVersion(999L); // version không khớp
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(roleId,"name","code", 1L));

        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(sampleRole);
        when(userRolesRepository.existsUserByRoleId(eq(roleId))).thenReturn(true);

        ErrorListResponse result = roleService.checkDeleteMulti(ids);

        assertTrue(result.getHasError());
        assertEquals("error.RoleAlreadyUseOnUser", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void checkDeleteMulti_NoError_ReturnsNull() {
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(roleId,"name","code", 1L));

        when(roleRepository.findByRoleIdIncluideDeleted(eq(roleId))).thenReturn(sampleRole);
        when(userRolesRepository.existsUserByRoleId(eq(roleId))).thenReturn(false);
        ErrorListResponse result = roleService.checkDeleteMulti(ids);

        assertNull(result);
    }
    @Test
    void getALlPermisstion_success() {
        Permission permission = new Permission();
        permission.setId(roleId);
        permission.setPermissionName("Permission 1");

        when(permissionsRepository.findAllByIsDeletedFalseOrderByPositionAsc()).thenReturn(List.of(permission));

        List<Permission> result = roleService.getALlPermisstion();

        assertEquals("Permission 1", result.get(0).getPermissionName());
    }
}
