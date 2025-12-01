package com.noffice.controller;

import com.noffice.config.TestSecurityConfig;
import com.noffice.entity.DocumentAllowedEditors;
import com.noffice.entity.DocumentAllowedViewers;
import com.noffice.entity.DocumentFiles;
import com.noffice.entity.User;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.repository.DocumentAllowedEditorsRepository;
import com.noffice.repository.DocumentAllowedViewersRepository;
import com.noffice.repository.DocumentFileRepository;
import com.noffice.repository.RolePermissionsRepository;
import com.noffice.service.JwtService;
import com.noffice.service.UserDetailsServiceImpl;
import com.noffice.service.UserService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileViewerController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class FileViewerControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockBean private DocumentFileRepository documentFileRepository;
    @MockBean private DocumentAllowedEditorsRepository editorsRepo;
    @MockBean private DocumentAllowedViewersRepository viewersRepo;

    @MockBean private JwtService jwtService;
    @MockBean private UserDetailsServiceImpl userDetailsServiceImpl;
    @MockBean private UserService userService;
    @MockBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean private RolePermissionsRepository rolePermissionsRepository;

    private static final String API_URL = "http://localhost:8080/api";
    private static final String COLLABORA_URL = "https://collabora.example.com";
    private static final String SAVE_PATH = "/app/uploads";

    private static final UUID CURRENT_USER_ID = UUID.randomUUID();
    private static final String VALID_ACCESS_TOKEN = "valid.jwt.access.token.here";
    private static final String WOPI_TOKEN = "wopi.jwt.token.generated";

    @BeforeEach
    void setUp() {
        FileViewerController controller = mockMvc.getDispatcherServlet()
                .getWebApplicationContext()
                .getBean(FileViewerController.class);

        ReflectionTestUtils.setField(controller, "apiUrl", API_URL);
        ReflectionTestUtils.setField(controller, "urlCollaboraOffice", COLLABORA_URL);
        ReflectionTestUtils.setField(controller, "savePath", SAVE_PATH);

        // Mock User trả về khi load từ DB
        User mockUser = new User();
        mockUser.setId(CURRENT_USER_ID);
        mockUser.setUsername("999");
        mockUser.setFullName("Lê Thái Anh");
        mockUser.setEmail("lethai.anh@noffice.vn");
        mockUser.setPhone("0901234567");
        mockUser.setPartnerId(CURRENT_USER_ID);
        mockUser.setIsAdmin(0);


        // Khi JwtAuthenticationFilter gọi loadUserByUsername → trả về user trên
        when(userDetailsServiceImpl.loadUserByUsername(anyString())).thenReturn(new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }

            @Override
            public String getPassword() {
                return "";
            }

            @Override
            public String getUsername() {
                return "";
            }
        });

        // Khi JwtService validate token → trả về true
        when(jwtService.isValidateToken(eq(VALID_ACCESS_TOKEN), any(UserDetails.class))).thenReturn(true);
        when(jwtService.extractUsername(VALID_ACCESS_TOKEN)).thenReturn("999");
        when(jwtService.extractClaim(VALID_ACCESS_TOKEN, Claims::getSubject)).thenReturn("999");

        // Khi generate WOPI token
        when(jwtService.generateWopiToken(
                eq(UUID.fromString("00000000-0000-0000-0000-000000000999")),
                any(UUID.class),
                anyString(),
                eq("Lê Thái Anh")
        )).thenReturn(WOPI_TOKEN);
    }

    private MockHttpServletRequestBuilder withBearer() {
        return MockMvcRequestBuilders
                .request(HttpMethod.POST, "/dummy") // dummy, sẽ bị override
                .header("Authorization", "Bearer " + VALID_ACCESS_TOKEN);
    }

    // Helper để tái sử dụng
    private MockHttpServletRequestBuilder withJwt(MockHttpServletRequestBuilder builder) {
        return builder.header("Authorization", "Bearer " + VALID_ACCESS_TOKEN);
    }

    @Test
    void getWopiUrl_Success() throws Exception {String wopiSrc = API_URL + "/wopi/files/report.docx";
        String encodedWopiSrc = URLEncoder.encode(wopiSrc, StandardCharsets.UTF_8);

        String finalUrl = COLLABORA_URL + "/browser/dist/cool.html?WOPISrc=" + encodedWopiSrc;
        mockMvc.perform(withJwt(MockMvcRequestBuilders.get("/api/fileViewer/wopiUrl/report.docx")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Name").value("report.docx"))
                .andExpect(jsonPath("$.Url").value(finalUrl));
    }

    @Test
    void uploadFile_Success() throws Exception {
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setPartnerId(CURRENT_USER_ID);
        Authentication authentication = new TestingAuthenticationToken(
                mockUser,
                null,
                "ROLE_USER"
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        MockMultipartFile file = new MockMultipartFile(
                "file", "report.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "test content".getBytes());

        DocumentFiles saved = new DocumentFiles();
        saved.setId(UUID.randomUUID());
        when(documentFileRepository.save(any(DocumentFiles.class))).thenReturn(saved);

        try (MockedStatic<Files> files = mockStatic(Files.class);
             MockedStatic<Paths> paths = mockStatic(Paths.class)) {

            Path dir = mock(Path.class);
            Path filePath = mock(Path.class);

            paths.when(() -> Paths.get(SAVE_PATH)).thenReturn(dir);
            when(dir.resolve(anyString())).thenReturn(filePath);
            when(dir.normalize()).thenReturn(dir);
            when(filePath.normalize()).thenReturn(filePath);
            when(filePath.startsWith(dir)).thenReturn(true);

            files.when(() -> Files.exists(dir)).thenReturn(false);
            files.when(() -> Files.createDirectories(dir)).thenReturn(dir);
            files.when(() -> Files.copy(any(Path.class), eq(filePath), any()))
                    .thenReturn(filePath);

            mockMvc.perform(withJwt(multipart("/api/fileViewer/upload").file(file)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").value("Upload thành công"))
                    .andExpect(jsonPath("$.id").isString());
        }
    }

    @Test
    void uploadFile_BlockExe() throws Exception {
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setPartnerId(CURRENT_USER_ID);
        Authentication authentication = new TestingAuthenticationToken(
                mockUser,
                null,
                "ROLE_USER"
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        MockMultipartFile file = new MockMultipartFile("file", "virus.exe", null, new byte[0]);

        mockMvc.perform(withJwt(multipart("/api/fileViewer/upload").file(file)))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    void createTemp_FirstOpen_AsCreator() throws Exception {
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setPartnerId(CURRENT_USER_ID);
        Authentication authentication = new TestingAuthenticationToken(
                mockUser,
                null,
                "ROLE_USER"
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        UUID fileId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID tempId = UUID.randomUUID();

        DocumentFiles original = new DocumentFiles();
        original.setId(fileId);
        original.setAttachName("test.docx");
        original.setCreateBy(CURRENT_USER_ID);

        DocumentFiles tempFile = new DocumentFiles();
        tempFile.setId(tempId);
        tempFile.setAttachName("temp_123_test.docx");

        DocumentAllowedViewers viewers = new DocumentAllowedViewers(UUID.randomUUID(),fileId,mockUser.getId() );

        when(documentFileRepository.findById(fileId)).thenReturn(Optional.of(original));
        when(viewersRepo.findByDocumentId(fileId)).thenReturn(List.of(viewers));
        when(documentFileRepository.findByOriginalFileIdAndIsTemp(fileId, true)).thenReturn(List.of());
        when(documentFileRepository.save(any())).thenReturn(tempFile);

        // Mock generateWopiToken đúng theo signature của bạn
        when(jwtService.generateWopiToken(
                UUID.fromString("00000000-0000-0000-0000-000000000999"), // userId
                fileId,
                "edit",
                "Lê Thái Anh"
        )).thenReturn(WOPI_TOKEN);

        try (MockedStatic<Files> files = mockStatic(Files.class);
             MockedStatic<Paths> paths = mockStatic(Paths.class);
             MockedStatic<LocalDateTime> dt = mockStatic(LocalDateTime.class)) {

            Path p = mock(Path.class);
            paths.when(() -> Paths.get(SAVE_PATH).resolve("test.docx")).thenReturn(p);
            files.when(() -> Files.readAllBytes(p)).thenReturn("hello".getBytes());
            files.when(() -> Files.write(any(Path.class), any(byte[].class))).thenReturn(p);

            mockMvc.perform(withJwt(MockMvcRequestBuilders.post("/api/fileViewer/createTemp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("\"" + fileId + "\"")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value("11111111-1111-1111-1111-111111111111"))
                    .andExpect(jsonPath("$.isModified").value(true))
                    .andExpect(jsonPath("$.collab").value(false));
        }
    }

    @Test
    void unlockFile_Success() throws Exception {
        UUID fileId = UUID.randomUUID();
        DocumentFiles file = new DocumentFiles();
        file.setId(fileId);
        file.setLockValue("my-lock-123");

        when(documentFileRepository.findById(fileId)).thenReturn(Optional.of(file));

        mockMvc.perform(withJwt(MockMvcRequestBuilders.post("/api/fileViewer/unlock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fileId": "%s", "lockValue": "my-lock-123"}
                                """.formatted(fileId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Unlocked"));

        verify(documentFileRepository).save(argThat(f -> f.getLockValue() == null));
    }

    @Test
    void saveFromTemp_Success() throws Exception {
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setPartnerId(CURRENT_USER_ID);
        Authentication authentication = new TestingAuthenticationToken(
                mockUser,
                null,
                "ROLE_USER"
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        UUID tempId = UUID.randomUUID();
        UUID origId = UUID.randomUUID();

        DocumentFiles temp = new DocumentFiles();
        temp.setId(tempId);
        temp.setIsTemp(true);
        temp.setOriginalFileId(origId);
        temp.setAttachName("temp_999.docx");

        DocumentFiles orig = new DocumentFiles();
        orig.setId(origId);
        orig.setAttachName("original.docx");
        orig.setLockValue("correct-lock");

        when(documentFileRepository.findById(tempId)).thenReturn(Optional.of(temp));
        when(documentFileRepository.findById(origId)).thenReturn(Optional.of(orig));

        try (MockedStatic<Files> files = mockStatic(Files.class);
             MockedStatic<Paths> paths = mockStatic(Paths.class)) {

            Path src = mock(Path.class);
            Path dst = mock(Path.class);
            paths.when(() -> Paths.get(SAVE_PATH).resolve("temp_999.docx")).thenReturn(src);
            paths.when(() -> Paths.get(SAVE_PATH).resolve("original.docx")).thenReturn(dst);
            files.when(() -> Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING)).thenReturn(dst);

            mockMvc.perform(withJwt(MockMvcRequestBuilders.post("/api/fileViewer/saveFromTemp")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"tempId\":\"" + tempId + "\",\"lockValue\":\"correct-lock\"}")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Lưu thành công"));
        }
    }
    @Test
    void deleteTemp_Success() throws Exception {
        UUID tempId = UUID.randomUUID();
        String tempFileName = "temp_abc123.docx";

        DocumentFiles tempFile = new DocumentFiles();
        tempFile.setId(tempId);
        tempFile.setAttachName(tempFileName);
        tempFile.setIsTemp(true);

        when(documentFileRepository.findById(tempId)).thenReturn(Optional.of(tempFile));

        try (MockedStatic<Paths> paths = mockStatic(Paths.class);
             MockedStatic<Files> files = mockStatic(Files.class)) {

            Path fullPath = mock(Path.class);

            paths.when(() -> Paths.get(SAVE_PATH).resolve(tempFileName)).thenReturn(fullPath);

            files.when(() -> Files.deleteIfExists(fullPath)).thenReturn(true);

            mockMvc.perform(withJwt(MockMvcRequestBuilders.delete("/api/fileViewer/deleteTemp/" + tempId)))
                    .andExpect(status().isOk())
                    .andExpect(content().string(""));
            verify(documentFileRepository).delete(tempFile);
        }
    }

    @Test
    void getCollaboraUrl_Success() throws Exception {
        mockMvc.perform(withJwt(MockMvcRequestBuilders.get("/api/fileViewer/getCollaboraUrl")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(COLLABORA_URL + "/browser/dist/cool.html"))
                .andExpect(jsonPath("$.url").isString());
    }
}