package com.noffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noffice.config.TestSecurityConfig;
import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.RoleDTO;
import com.noffice.dto.RolePermissionRequest;
import com.noffice.dto.RoleSearchDTO;
import com.noffice.entity.Permission;
import com.noffice.entity.Role;
import com.noffice.entity.User;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.service.*;
import com.noffice.ultils.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RoleController.class)
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RoleService roleService;

    @MockBean
    private RolePermissionsService rolePermissionsService;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private Authentication authentication;

    private User mockUser;
    private UUID testPartnerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private UUID testRoleId = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setPartnerId(testPartnerId);
        mockUser.setFullName("Test User");

        Authentication authentication = new TestingAuthenticationToken(
                mockUser,
                null,
                "ROLE_USER"
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

    }

    @Test
    void searchRoles_Success() throws Exception {

        RoleSearchDTO requestDTO = new RoleSearchDTO(
                "tìm kiếm", "Tên", "CODE", "Mô tả", 1L, true, 0, 10
        );
        List<RoleDTO> content = List.of(new RoleDTO(testRoleId, "Tên vai trò", "RC", "Mô tả", 1L, testPartnerId, "Partner", true, false, null, null, null, null));
        PageImpl<RoleDTO> mockPage = new PageImpl<>(content, PageRequest.of(0, 10), 1);

        String escapedSearch = StringUtils.toLikeAndLowerCaseString(StringUtils.unAccent(requestDTO.getSearchString()).trim());
        String escapedRoleName = StringUtils.toLikeAndLowerCaseString(StringUtils.unAccent(requestDTO.getRoleName()).trim());
        String escapedRoleCode = StringUtils.toLikeAndLowerCaseString(StringUtils.unAccent(requestDTO.getRoleCode()).trim());
        String escapedRoleDescription = StringUtils.toLikeAndLowerCaseString(StringUtils.unAccent(requestDTO.getRoleDescription()).trim());


        when(roleService.searchRoles(
                eq(escapedSearch),
                eq(escapedRoleName),
                eq(escapedRoleCode),
                eq(escapedRoleDescription),
                eq(testPartnerId),
                eq(requestDTO.getStatus()),
                eq(requestDTO.getPage()),
                eq(requestDTO.getSize())
        )).thenReturn(mockPage);

        mockMvc.perform(post("/api/roles/searchRoles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Thành công"))
                .andExpect(jsonPath("$.object.content[0].id").value(testRoleId.toString()));

    }

    @Test
    void save_Success() throws Exception {
        RoleDTO roleDTO = new RoleDTO(null, "New Role", "NEW_CODE", "Mô tả mới", null, null, null, true, false, null, null, null, null);

        when(roleService.save(any(RoleDTO.class), eq(mockUser))).thenReturn("");

        mockMvc.perform(post("/api/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(roleService, times(1)).save(any(RoleDTO.class), eq(mockUser));
    }

    @Test
    void save_ValidationErrorFromService() throws Exception {
        RoleDTO roleDTO = new RoleDTO();
        String errorMessage = "Mã vai trò đã tồn tại";

        when(roleService.save(any(RoleDTO.class), eq(mockUser))).thenReturn(errorMessage);

        mockMvc.perform(post("/api/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk()) // Controller trả về 200 nhưng message là 400
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(errorMessage));

        verify(roleService, times(1)).save(any(RoleDTO.class), eq(mockUser));
    }

    @Test
    void save_InternalServerError() throws Exception {
        RoleDTO roleDTO = new RoleDTO();

        when(roleService.save(any(RoleDTO.class), eq(mockUser))).thenThrow(new RuntimeException("DB access failed"));

        mockMvc.perform(post("/api/roles/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Lỗi hệ thống"));

        verify(roleService, times(1)).save(any(RoleDTO.class), eq(mockUser));
    }

    @Test
    void update_Success() throws Exception {
        RoleDTO roleDTO = new RoleDTO(testRoleId, "Updated Role", "UPDATED_CODE", "Mô tả cập nhật", 5L,
                null, null, true, false, null, null, null, null);
        when(roleService.update(any(RoleDTO.class), eq(mockUser))).thenReturn("");
        mockMvc.perform(post("/api/roles/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(roleDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(roleService, times(1)).update(any(RoleDTO.class), eq(mockUser));
    }

    @Test
    void delete_Success() throws Exception {
        Long version = 1L;
        when(roleService.delete(eq(testRoleId), eq(mockUser), eq(version))).thenReturn("");
        mockMvc.perform(get("/api/roles/delete")
                        .param("id", testRoleId.toString())
                        .param("version", version.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Xóa thành công"));
        verify(roleService, times(1)).delete(eq(testRoleId), eq(mockUser), eq(version));
    }

    @Test
    void delete_Fail() throws Exception {
        Long version = 1L;
        when(roleService.delete(eq(testRoleId), eq(mockUser), eq(version))).thenReturn("Loi");
        mockMvc.perform(get("/api/roles/delete")
                        .param("id", testRoleId.toString())
                        .param("version", version.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Loi"));
        verify(roleService, times(1)).delete(eq(testRoleId), eq(mockUser), eq(version));
    }

    @Test
    void delete_BadRequestFromService() throws Exception {
        Long version = 1L;
        String errorMessage = "Vai trò đang được sử dụng";

        when(roleService.delete(eq(testRoleId), eq(mockUser), eq(version))).thenReturn(errorMessage);

        mockMvc.perform(get("/api/roles/delete")
                        .param("id", testRoleId.toString())
                        .param("version", version.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(errorMessage));

        verify(roleService, times(1)).delete(eq(testRoleId), eq(mockUser), eq(version));
    }

    @Test
    void lock_Success() throws Exception {
        Long version = 1L;

        when(roleService.lockRole(eq(testRoleId), eq(mockUser), eq(version))).thenReturn("");

        mockMvc.perform(get("/api/roles/lock")
                        .param("id", testRoleId.toString())
                        .param("version", version.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(roleService, times(1)).lockRole(eq(testRoleId), eq(mockUser), eq(version));
    }

    @Test
    void lock_Faild() throws Exception {
        Long version = 1L;

        when(roleService.lockRole(eq(testRoleId), eq(mockUser), eq(version))).thenReturn("Loi");

        mockMvc.perform(get("/api/roles/lock")
                        .param("id", testRoleId.toString())
                        .param("version", version.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Loi"));

        verify(roleService, times(1)).lockRole(eq(testRoleId), eq(mockUser), eq(version));
    }

    @Test
    void deleteMuti_Success() throws Exception {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(testRoleId,"name","code", 1L),
                new DeleteMultiDTO(UUID.randomUUID(),"name","code", 2L)
        );

        when(roleService.deleteMuti(eq(ids), eq(mockUser))).thenReturn("");

        mockMvc.perform(post("/api/roles/deleteMuti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("Xóa thành công"));

        verify(roleService, times(1)).deleteMuti(eq(ids), eq(mockUser));
    }

    @Test
    void deleteMuti_Faild() throws Exception {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(testRoleId,"name","code", 1L),
                new DeleteMultiDTO(UUID.randomUUID(),"name","code", 2L)
        );

        when(roleService.deleteMuti(eq(ids), eq(mockUser))).thenReturn("Loi");

        mockMvc.perform(post("/api/roles/deleteMuti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Loi"));

        verify(roleService, times(1)).deleteMuti(eq(ids), eq(mockUser));
    }


    @Test
    void getAllRole_Success() throws Exception {
        List<Role> mockRoles = List.of(new Role("Name", "Role 1", "R1"));
        when(roleService.getAllRole(eq(testPartnerId))).thenReturn(mockRoles);

        mockMvc.perform(get("/api/roles/getAllRole"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object[0].roleName").value("Name"))
                .andExpect(jsonPath("$.object[0].roleCode").value("Role 1"))
                .andExpect(jsonPath("$.object[0].roleDescription").value("R1"));

        verify(roleService, times(1)).getAllRole(eq(testPartnerId));
    }

    @Test
    void updateRolePermissions_Success() throws Exception {
        RolePermissionRequest request = new RolePermissionRequest(
                testRoleId,
                List.of(UUID.randomUUID()),
                Collections.emptyList()
        );

        when(rolePermissionsService.updatePermissionsForRole(any(UUID.class), anyList(), anyList())).thenReturn("");

        mockMvc.perform(post("/api/roles/updateRolePermisstion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("success"));

        verify(rolePermissionsService, times(1)).updatePermissionsForRole(eq(testRoleId), anyList(), anyList());
    }

    @Test
    void updateRolePermissions_Faild() throws Exception {
        RolePermissionRequest request = new RolePermissionRequest(
                testRoleId,
                List.of(UUID.randomUUID()),
                Collections.emptyList()
        );

        when(rolePermissionsService.updatePermissionsForRole(any(UUID.class), anyList(), anyList())).thenReturn("Loi");

        mockMvc.perform(post("/api/roles/updateRolePermisstion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Loi"));

        verify(rolePermissionsService, times(1)).updatePermissionsForRole(eq(testRoleId), anyList(), anyList());
    }

    @Test
    void getPermissionsCurrent_Success() throws Exception {

        String menuCode = "USER_MANAGEMENT";
        List<String> mockPermissions = List.of(
                "USER_MANAGEMENT_ADD",
                "USER_MANAGEMENT_EDIT"
        );

        when(rolePermissionsService.getPermissionsCurrent(eq(menuCode), eq(mockUser))).thenReturn(mockPermissions);

        mockMvc.perform(get("/api/roles/getPermissionsCurrent")
                        .param("menuCode", menuCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.add").value(true)) // USER_MANAGEMENT_ADD có
                .andExpect(jsonPath("$.edit").value(true)) // USER_MANAGEMENT_EDIT có
                .andExpect(jsonPath("$.delete").value(false)) // _DELETE không có
                .andExpect(jsonPath("$.permission").value(false)); // _PERMISSION không có


        verify(rolePermissionsService, times(1)).getPermissionsCurrent(eq(menuCode), eq(mockUser));
    }

    @Test
    void getLogDetailDocType_Success() throws Exception {

        Mockito.doNothing().when(roleService).getLogDetailRole(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/roles/LogDetailRole")
                        .param("id", testRoleId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getALlPermisstion_Success() throws Exception {
        Permission permission = new Permission();
        permission.setId(testRoleId);
        permission.setPermissionName("Permission 1");
        Mockito.when(roleService.getALlPermisstion()).thenReturn(List.of(permission));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/roles/getALlPermisstion"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object[0].id").value(testRoleId.toString()))
                .andExpect(jsonPath("$.object[0].permissionName").value("Permission 1"));
    }

    @Test
    void getRolePermisstion_Success() throws Exception {
        Permission permission = new Permission();
        permission.setId(testRoleId);
        permission.setPermissionName("Permission 1");
        Mockito.when(rolePermissionsService.getRolePermissions(eq(testRoleId))).thenReturn(List.of(permission));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/roles/getRolePermisstion")
                        .param("roleId", testRoleId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object[0].id").value(testRoleId.toString()))
                .andExpect(jsonPath("$.object[0].permissionName").value("Permission 1"));
    }

    @Test
    void getRolePermissionsHalf_Success() throws Exception {
        Permission permission = new Permission();
        permission.setId(testRoleId);
        permission.setPermissionName("Permission 1");
        Mockito.when(rolePermissionsService.getRolePermissionsHalf(eq(testRoleId))).thenReturn(List.of(permission));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/roles/getRolePermissionsHalf")
                        .param("roleId", testRoleId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object[0].id").value(testRoleId.toString()))
                .andExpect(jsonPath("$.object[0].permissionName").value("Permission 1"));
    }

    @Test
    void checkDeleteMulti_Success() throws Exception {
        ErrorListResponse response = new ErrorListResponse();
        ErrorListResponse.ErrorResponse errorResponse = new ErrorListResponse.ErrorResponse();
        response.setErrors(List.of(errorResponse));

        Mockito.when(roleService.checkDeleteMulti(anyList())).thenReturn(response);

        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(testRoleId,"name","code", 1L));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/roles/checkDeleteMulti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserOriginDataPermissions_Success() throws Exception {
        Permission permission = new Permission();
        permission.setId(testRoleId);
        permission.setPermissionName("Permission 1");
        Mockito.when(rolePermissionsService.getUserOriginDataPermissions(anyString(),any())).thenReturn(List.of(permission));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/roles/getUserOriginDataPermissions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object[0].id").value(testRoleId.toString()))
                .andExpect(jsonPath("$.object[0].permissionName").value("Permission 1"));
    }

    @Test
    void getUserPermissions_Success() throws Exception {
        Permission permission = new Permission();
        permission.setId(testRoleId);
        permission.setPermissionName("Permission 1");
        Mockito.when(rolePermissionsService.getUserPermissions(any())).thenReturn(List.of(permission));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/roles/getUserPermissions"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object[0].id").value(testRoleId.toString()))
                .andExpect(jsonPath("$.object[0].permissionName").value("Permission 1"));
    }

}