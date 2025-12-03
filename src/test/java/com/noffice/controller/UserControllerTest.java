package com.noffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noffice.config.TestSecurityConfig;
import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.UserDetailDTO;
import com.noffice.entity.User;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.UserRepository;
import com.noffice.service.UserService;
import com.noffice.service.JwtService;
import com.noffice.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;
    @MockBean
    private JwtService jwtService;
    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID partnerId;
    private UUID testId;
    private User user;
    private UserDetailDTO mockUser;

    @BeforeEach
    void setUp() {
        partnerId = UUID.fromString("411f2d97-96ed-4bd2-a480-cec1afba65e8");
        testId = UUID.randomUUID();

        user = new User();
        user.setId(testId);
        user.setId(UUID.randomUUID());
        user.setUserCode("HC");
        user.setFullName("Hành chính");
        user.setUsername("testuser");
        user.setPartnerId(partnerId);
        user.setCreateAt(LocalDate.of(2025, 1, 1).atStartOfDay());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));

        Authentication authentication = new TestingAuthenticationToken(
                user,
                null,
                "ROLE_USER"
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        mockUser = new UserDetailDTO();
        mockUser.setUserCode("USER001");
        mockUser.setUsername("testuser");
        mockUser.setProfileImage("uploads/profiles/avatar.jpg");
        mockUser.setSignatureImage("uploads/signatures/sign.png");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void search_Success() throws Exception {
        Page<User> page = new PageImpl<>(
                List.of(user),
                PageRequest.of(0, 10),
                1L
        );

        Mockito.when(userService.listUsers(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("userCode", "")
                        .param("userName", "")
                        .param("userDescription", "")
                        .param("searchString", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object.totalElements").value(1))
                .andExpect(jsonPath("$.object.totalPages").value(1))
                .andExpect(jsonPath("$.object.numberOfElements").value(1))
                .andExpect(jsonPath("$.object.content[0].userCode").value("HC"))
                .andExpect(jsonPath("$.object.content[0].fullName").value("Hành chính"));
    }

    @Test
    void search_Fail() throws Exception {
        Page<User> page = new PageImpl<>(
                List.of(user),
                PageRequest.of(0, 10),
                1L
        );

        Mockito.when(userService.listUsers(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyString(), any(), any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/list")
                        .param("page", "-1")
                        .param("size", "-10")
                        .param("userCode", "")
                        .param("userName", "")
                        .param("userDescription", "")
                        .param("searchString", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void deleteDoc_Success() throws Exception {
        Mockito.when(userService.deleteUser(eq(testId), any(), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/delete")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteDoc_BusinessError() throws Exception {
        Mockito.when(userService.deleteUser(eq(testId), any(), anyLong()))
                .thenReturn("không thể xóa!");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/delete")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("không thể xóa!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteMulti_Success() throws Exception {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(testId, "name", "code", 1L),
                new DeleteMultiDTO(UUID.randomUUID(), "name", "code", 1L)
        );

        Mockito.when(userService.deleteMultiUser(anyList(), any()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/deleteMuti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteMulti_BusinessError() throws Exception {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(testId, "name", "code", 1L),
                new DeleteMultiDTO(UUID.randomUUID(), "name", "code", 1L)
        );

        Mockito.when(userService.deleteMultiUser(anyList(), any()))
                .thenReturn("không thể xóa");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/deleteMuti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("không thể xóa"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void lock_Success() throws Exception {
        Mockito.when(userService.lockUser(eq(testId), any(), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/lock")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void lock_BusinessError() throws Exception {
        Mockito.when(userService.lockUser(eq(testId), any(), anyLong()))
                .thenReturn("không thể thay đổi!");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/lock")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("không thể thay đổi!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addUser_Success() throws Exception {

        // userService.saveUser trả "" -> success
        Mockito.when(userService.createUser(any(), any(), any(), any(), any(), any()))
                .thenReturn("");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/users/add")
                                .param("userName", "userName")
                                .param("fullName", "fullName")
                                .param("identifyCode", "111111111")
                                .param("password", "password")
                                .param("userCode", "userCode")
                                .param("birthday", "01/01/2000")
                                .param("issueDate", "01/01/2000")
                                .param("issuePlace", "issuePlace")
                                .param("email", "111@gmail.com")
                                .param("phone", "1111111111")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .content(objectMapper.writeValueAsString(user))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void addUser_Fail() throws Exception {

        Mockito.when(userService.createUser(any(), any(), any(), any(), any(), any()))
                .thenReturn("Name already exists");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/users/add")
                                .param("userName", "userName")
                                .param("fullName", "fullName")
                                .param("identifyCode", "111111111")
                                .param("password", "password")
                                .param("userCode", "userCode")
                                .param("birthday", "01/01/2000")
                                .param("issueDate", "01/01/2000")
                                .param("issuePlace", "issuePlace")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .content(objectMapper.writeValueAsString(user))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Name already exists"))
                .andExpect(jsonPath("$.status").value(400));
    }


    @Test
    void updateUser_Success() throws Exception {

        Mockito.when(userService.updateUser(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("");
        Mockito.when(userRepository.findByUsername(anyString())).thenReturn(Optional.ofNullable(user));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/users/update")
                                .param("userName", "userName")
                                .param("fullName", "fullName")
                                .param("identifyCode", "111111111")
                                .param("password", "password")
                                .param("userCode", "userCode")
                                .param("birthday", "01/01/2000")
                                .param("issueDate", "01/01/2000")
                                .param("issuePlace", "issuePlace")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .content(objectMapper.writeValueAsString(user))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void updateUser_Fail() throws Exception {

        Mockito.when(userService.updateUser(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn("not found");
        Mockito.when(userRepository.findByUsername(anyString())).thenReturn(Optional.ofNullable(user));

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/users/update")
                                .param("userName", "userName")
                                .param("fullName", "fullName")
                                .param("identifyCode", "111111111")
                                .param("password", "password")
                                .param("userCode", "userCode")
                                .param("birthday", "01/01/2000")
                                .param("issueDate", "01/01/2000")
                                .param("issuePlace", "issuePlace")
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .content(objectMapper.writeValueAsString(user))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("not found"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getLogDetailDocType_Success() throws Exception {

        Mockito.doNothing().when(userService).getLogDetailUser(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/LogDetailUser")
                        .param("id", testId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getAllUser_Success() throws Exception {

        Mockito.when(userService.getAllUser(
                eq(partnerId)
        )).thenReturn(List.of(user));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/getAllUser"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Thành công"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object[0].userCode").value("HC"))
                .andExpect(jsonPath("$.object[0].fullName").value("Hành chính"));
    }

    @Test
    void checkDeleteMulti_Success() throws Exception {
        ErrorListResponse response = new ErrorListResponse();
        ErrorListResponse.ErrorResponse errorResponse = new ErrorListResponse.ErrorResponse();
        response.setErrors(List.of(errorResponse));

        Mockito.when(userService.checkDeleteMulti(anyList())).thenReturn(response);

        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(testId, "name", "code", 1L));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/checkDeleteMulti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk());
    }

    @Test
    void getUserById_success() throws Exception {
        UserDetailDTO userDetail = new UserDetailDTO();
        userDetail.setUserCode("code");
        Mockito.when(userService.getByUserId(eq(testId)))
                .thenReturn(userDetail);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/getUserById")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getImageFile_Signature_Success_Simple() throws Exception {
        Mockito.when(userService.getByUserId(testId)).thenReturn(mockUser);

        // Tạo mock Resource bình thường (không cần MockedConstruction)
        Resource mockResource = Mockito.mock(Resource.class);
        Mockito.when(mockResource.exists()).thenReturn(true);
        Mockito.when(mockResource.isReadable()).thenReturn(true);
        Mockito.when(mockResource.getFilename()).thenReturn("signature.png");
        Mockito.when(mockResource.getInputStream()).thenReturn(
                new ByteArrayInputStream("PNG fake".getBytes())
        );
        Mockito.when(mockResource.contentLength()).thenReturn(123L);

        // Trả về mock này
        Mockito.when(userService.downFile(anyString())).thenReturn(mockResource);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/getImage")
                        .param("id", testId.toString())
                        .param("type", "signature"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        containsString("filename=\"signature.png\"")))
                .andExpect(content().contentTypeCompatibleWith(MediaType.IMAGE_PNG));
    }

    @Test
    void getImageFile_Unauthenticated_Returns401() throws Exception {
        SecurityContextHolder.clearContext(); // xóa auth

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/getImage")
                        .param("id", testId.toString())
                        .param("type", "profile"))
                .andExpect(status().isOk()) // bạn đang trả 200 + body → nên sửa thành 401
                .andExpect(jsonPath("$.message").value("Không có token hoặc phiên đăng nhập hợp lệ"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void getImageFile_UserNotFound_Returns404() throws Exception {
        Mockito.when(userService.getByUserId(testId)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/getImage")
                        .param("id", testId.toString())
                        .param("type", "profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Người dùng không tồn tại"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getImageFile_InvalidType_Returns400() throws Exception {
        Mockito.when(userService.getByUserId(testId)).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/getImage")
                        .param("id", testId.toString())
                        .param("type", "invalid_type"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Loại hình ảnh không hợp lệ"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getImageFile_Exception_Returns500() throws Exception {
        Mockito.when(userService.getByUserId(testId))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/getImage")
                        .param("id", testId.toString())
                        .param("type", "profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void updateImage_NoFiles_KeepsOldImages() throws Exception {
        Mockito.when(userRepository.getUserByUserId(user.getId())).thenReturn(user);
        Mockito.doNothing().when(userService).updateUserImages(any(), any(), any());

        mockMvc.perform(multipart("/api/users/updateImage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật thành công"));

    }

    @Test
    void updateImage_Unauthenticated_Returns401() throws Exception {
        SecurityContextHolder.clearContext();

        mockMvc.perform(multipart("/api/users/updateImage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Không có token hoặc phiên đăng nhập hợp lệ"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void checkAndGenerateUserCode_NotExists_ReturnsValid() throws Exception {
        Mockito.when(userRepository.existsByUserCodeAndPartnerId(any(), any())).thenReturn(false);

        String requestJson = """
                {
                    "userCode": "USER001"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/checkAndGenerateUserCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").isEmpty())
                .andExpect(jsonPath("$.message").value("Mã user hợp lệ"))
                .andExpect(jsonPath("$.status").value(200));

    }

    @Test
    void checkAndGenerateUserCode_Exists_ReturnsNewCode() throws Exception {
        Mockito.when(userRepository.existsByUserCodeAndPartnerId(any(), any())).thenReturn(true);

        String requestJson = """
                {
                    "userCode": "USER001"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/checkAndGenerateUserCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("newCode"))
                .andExpect(jsonPath("$.message").value("Mã user mới đã được sinh ra"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void checkAndGenerateUserCode_Unauthenticated_Returns401() throws Exception {
        SecurityContextHolder.clearContext(); // xóa token

        String requestJson = """
                {
                    "userCode": "USER001"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/checkAndGenerateUserCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Không có token hoặc phiên đăng nhập hợp lệ"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void checkAndGenerateUserCode_Exception_Returns500() throws Exception {
        Mockito.when(userRepository.existsByUserCodeAndPartnerId(anyString(), any()))
                .thenThrow(new RuntimeException("Database connection failed"));

        String requestJson = """
                {
                    "userCode": "USER001"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/checkAndGenerateUserCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value(containsString("Lỗi hệ thống")));
    }

    @Test
    void checkAndGenerateUserCode_NullPartnerId() throws Exception {
        user.setPartnerId(null); // PartnerId = null

        // Cập nhật lại authentication
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null)
        );

        Mockito.when(userRepository.existsByUserCodeAndPartnerId(any(), any())).thenReturn(false);

        String requestJson = """
                {
                    "userCode": "USER001"
                }
                """;

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users/checkAndGenerateUserCode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Không có token hoặc phiên đăng nhập hợp lệ"))
                .andExpect(jsonPath("$.status").value(401));
    }
}


