package com.noffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noffice.config.TestSecurityConfig;
import com.noffice.dto.CreateUserGroupDTO;
import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.UserGroup;
import com.noffice.entity.User;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.UserGroupResponse;
import com.noffice.service.UserGroupService;
import com.noffice.service.JwtService;
import com.noffice.service.UserDetailsServiceImpl;
import com.noffice.service.UserService;
import com.noffice.ultils.Constants;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserGroupController.class)
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc(addFilters = false)
class UserGroupControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserGroupService userGroupService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID partnerId;
    private UUID testId;
    private UserGroup userGroup;
    private UserGroupResponse userGroupResponse;
    private CreateUserGroupDTO validPayload;
    @BeforeEach
    void setUp() {
        partnerId = UUID.fromString("411f2d97-96ed-4bd2-a480-cec1afba65e8");
        testId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setPartnerId(partnerId);

        Authentication authentication = new TestingAuthenticationToken(
                mockUser,
                null,
                "ROLE_USER"
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        userGroup = new UserGroup();
        userGroup.setId(testId);
        userGroup.setId(UUID.randomUUID());
        userGroup.setGroupCode("HC");
        userGroup.setGroupName("Hành chính");
        userGroup.setCreateAt(LocalDate.of(2025, 1, 1).atStartOfDay());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));

        UserGroupResponse.UserResponse user = new UserGroupResponse.UserResponse();
        user.setUserId(testId);
        user.setUsername("testuser");
        user.setUserCode("testusercode");
        user.setFullName("testfullname");

        userGroupResponse = new UserGroupResponse();
        userGroupResponse.setId(testId);
        userGroupResponse.setGroupCode("HC");
        userGroupResponse.setGroupName("Hành chính");
        userGroupResponse.setIsActive(true);
        userGroupResponse.setVersion(1L);
        userGroupResponse.setUsers(List.of(user));

        validPayload = new CreateUserGroupDTO(
                null,
                "testuser",
                "testusercode",
                Collections.singletonList(testId),
                1L
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void search_Success_Fixed() throws Exception {
        User userDetails = new User();
        userDetails.setPartnerId(partnerId);

        Page<UserGroupResponse> page = new PageImpl<>(
                List.of(userGroupResponse),
                PageRequest.of(0, 10),
                1L
        );

        Mockito.when(userGroupService.searchUserGroups(eq(partnerId),
                any(),
                any(),
                any(),
                any(),
                eq(PageRequest.of(0, 10))
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/userGroups/search")
                        .param("page", "0")
                        .param("size", "10")
                        .param("searchString", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User groups retrieved successfully")) // Đã sửa message theo Controller
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object.totals").value(1))
                .andExpect(jsonPath("$.object.datas[0].groupCode").value("HC"))
                .andExpect(jsonPath("$.object.datas[0].groupName").value("Hành chính"));
    }

    @Test
    void search_Fail() throws Exception {
        Page<UserGroupResponse> page = new PageImpl<>(
                List.of(userGroupResponse),
                PageRequest.of(0, 10),
                1L
        );

        Mockito.when(userGroupService.searchUserGroups(eq(partnerId),
                anyString(), anyString(), anyString(), anyBoolean(),
                any(Pageable.class)

        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/userGroups/search")
                        .param("page", "-1")
                        .param("size", "-10")
                        .param("userGroupCode", "")
                        .param("userGroupName", "")
                        .param("userGroupDescription", "")
                        .param("searchString", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void deleteDoc_Success() throws Exception {
        Mockito.when(userGroupService.deleteUserGroup(eq(testId), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/userGroups/delete")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User group deleted successfully"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteDoc_BusinessError() throws Exception {
        Mockito.when(userGroupService.deleteUserGroup(eq(testId), anyLong()))
                .thenReturn("không thể xóa!");

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/userGroups/delete")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("không thể xóa!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteMulti_Success() throws Exception {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(testId,"name","code", 1L),
                new DeleteMultiDTO(UUID.randomUUID(),"name","code", 1L)
        );

        Mockito.when(userGroupService.deleteUserGroup(eq(testId), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/userGroups/deleteMultiple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Deleted multiple user group successfully"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteMulti_BusinessError() throws Exception {
        List<DeleteMultiDTO> ids = List.of();

        Mockito.when(userGroupService.deleteUserGroup(any(), anyLong()))
                .thenReturn("không thể xóa");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/userGroups/deleteMultiple")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("No user groups to delete"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void lock_Success() throws Exception {
        Mockito.when(userGroupService.updateUserGroupStatus(eq(testId), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/userGroups/updateStatus")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User group status updated successfully"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void lock_BusinessError() throws Exception {
        Mockito.when(userGroupService.updateUserGroupStatus(eq(testId), anyLong()))
                .thenReturn("không thể thay đổi!");

        mockMvc.perform(MockMvcRequestBuilders.put("/api/userGroups/updateStatus")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("không thể thay đổi!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addUserGroup_Success() throws Exception {

        // userGroupService.saveUserGroup trả "" -> success
        Mockito.when(userGroupService.saveUserGroup(any(), any(), any(), anyList(), any()))
                .thenReturn("");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/userGroups/save")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userGroup))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void saveUserGroup_Faild() throws Exception {
        String errorMessage = "Mã nhóm người dùng đã tồn tại.";
        Mockito.when(userGroupService.saveUserGroup(
                any(),
                anyString(),
                anyString(),
                anyList(),
                anyLong()
        )).thenReturn(errorMessage);
        
        mockMvc.perform(MockMvcRequestBuilders.post("/api/userGroups/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.object").doesNotExist());
    }
    @Test
    void getLogDetailDocType_Success() throws Exception {

        Mockito.doNothing().when(userGroupService).saveLogDetail(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/userGroups/logDetail")
                        .param("id", testId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Log details retrieved successfully"))
                .andExpect(jsonPath("$.status").value(200));
    }


    @Test
    void checkDeleteMulti_Success() throws Exception {
        ErrorListResponse response = new ErrorListResponse();
        ErrorListResponse.ErrorResponse errorResponse = new ErrorListResponse.ErrorResponse();
        response.setErrors(List.of(errorResponse));

        Mockito.when(userGroupService.checkDeleteMulti(anyList())).thenReturn(response);

        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(testId,"name","code", 1L));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/userGroups/checkDeleteMulti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk());
    }
}
