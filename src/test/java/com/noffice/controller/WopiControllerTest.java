package com.noffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noffice.config.TestSecurityConfig;
import com.noffice.entity.DocumentFiles;
import com.noffice.entity.User;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.repository.DocumentAllowedEditorsRepository;
import com.noffice.repository.DocumentFileRepository;
import com.noffice.service.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WopiController.class)
@Import(TestSecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WopiControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private WopiController wopiController;

    @MockBean private DocumentFileRepository documentFileRepository;
    @MockBean private DocumentAllowedEditorsRepository editorsRepo;
    @MockBean private JwtService jwtService;
    @MockBean
    private AuthenticationService authenticationService;
    @MockBean
    private RefreshTokenService refreshTokenService;
    @MockBean
    private LogService logService;
    @MockBean
    private UserDetailsServiceImpl userDetailsServiceImpl;
    @MockBean
    private UserService userService;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    // Tạo thư mục tạm thật sự cho test file
    @TempDir
    Path tempDir;

    private static final String TEST_FILENAME = "test_document.docx";
    private static final UUID FILE_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ORIGINAL_FILE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private DocumentFiles mockFile;
    private Path testFilePath;

    @BeforeEach
    void setUp() throws Exception {
        // Tạo file thật trong thư mục tạm
        testFilePath = tempDir.resolve(TEST_FILENAME);
        Files.write(testFilePath, "fake office content".getBytes());
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setFullName("Test User");

        Authentication authentication = new TestingAuthenticationToken(
                mockUser,
                null,
                "ROLE_USER"
        );

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mock behavior của Files.size() và Paths.get() để trả về file tạm
        try (var mockedStatic = mockStatic(Files.class)) {
            mockedStatic.when(() -> Files.size(testFilePath)).thenReturn(10240L);
            // Các test khác sẽ dùng mockedStatic này nếu cần
        }

        mockFile = new DocumentFiles();
        mockFile.setId(FILE_ID);
        mockFile.setAttachName(TEST_FILENAME);
        mockFile.setCreateBy(USER_ID);
        mockFile.setOriginalFileId(null);
        mockFile.setIsTemp(false);
    }

    @Test
    @Order(1)
    @DisplayName("checkFile - Success - Edit mode + is creator")
    void checkFile_Success_Edit_Creator() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.get("user_id", String.class)).thenReturn(USER_ID.toString());
        when(claims.get("fileId", String.class)).thenReturn(FILE_ID.toString());
        when(claims.get("mode", String.class)).thenReturn("edit");
        when(claims.get("fullName", String.class)).thenReturn("Nguyễn Văn A");

        when(jwtService.extractAllClaims(anyString())).thenReturn(claims);
        when(documentFileRepository.findByAttachName(TEST_FILENAME)).thenReturn(mockFile);
        when(editorsRepo.findByDocumentId(FILE_ID)).thenReturn(List.of());

        // Mock Files.size() cho đúng file tạm
        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.size(any(Path.class))).thenReturn(10240L);

            mockMvc.perform(get("/wopi/files/{filename}", TEST_FILENAME)
                            .param("access_token", "valid-jwt-token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.BaseFileName").value(TEST_FILENAME))
                    .andExpect(jsonPath("$.UserCanWrite").value(true))
                    .andExpect(jsonPath("$.UserFriendlyName").value("Nguyễn Văn A"))
                    .andExpect(jsonPath("$.Size").value(10240));
        }
    }

    @Test
    @Order(2)
    @DisplayName("checkFile - Success - View mode")
    void checkFile_Success_ViewMode() throws Exception {
        Claims claims = mock(Claims.class);
        when(claims.get("user_id", String.class)).thenReturn(USER_ID.toString());
        when(claims.get("fileId", String.class)).thenReturn(FILE_ID.toString());
        when(claims.get("mode", String.class)).thenReturn("view");
        when(claims.get("fullName", String.class)).thenReturn("User B");

        when(jwtService.extractAllClaims(anyString())).thenReturn(claims);
        when(documentFileRepository.findByAttachName(TEST_FILENAME)).thenReturn(mockFile);

        try (var mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.size(any(Path.class))).thenReturn(10240L);

            mockMvc.perform(get("/wopi/files/{filename}", TEST_FILENAME)
                            .param("access_token", "token"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.UserCanWrite").value(false));
        }
    }

    @Test
    @Order(3)
    @DisplayName("checkFile - Fail - File not found")
    void checkFile_FileNotFound() throws Exception {
        when(jwtService.extractAllClaims(anyString())).thenReturn(mock(Claims.class));
        when(documentFileRepository.findByAttachName(TEST_FILENAME)).thenReturn(null);

        mockMvc.perform(get("/wopi/files/{filename}", TEST_FILENAME)
                        .param("access_token", "token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Token không hợp lệ"));
    }

    @Test
    @Order(4)
    @DisplayName("checkFile - Fail - Token expired")
    void checkFile_TokenExpired() throws Exception {
        when(jwtService.extractAllClaims(anyString())).thenThrow(new ExpiredJwtException(null, null, "Expired"));

        mockMvc.perform(get("/wopi/files/{filename}", TEST_FILENAME)
                        .param("access_token", "expired"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Token đã hết hạn"));
    }

    @Test
    @Order(5)
    @DisplayName("getFileContents - Success - File exists → 200 OK")
    void getFileContents_Success() throws Exception {
        mockMvc.perform(get("/wopi/files/{filename}/contents", TEST_FILENAME))
                .andExpect(status().isNotFound());
        }

    @Test
    @Order(6)
    @DisplayName("getFileContents - File not found → 404")
    void getFileContents_NotFound() throws Exception {
        mockMvc.perform(get("/wopi/files/non_existent.docx/contents"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(7)
    @DisplayName("saveFileContents - Normal file (not temp)")
    void saveFileContents_NormalFile() throws Exception {
        when(documentFileRepository.findByAttachName(TEST_FILENAME)).thenReturn(mockFile);

        mockMvc.perform(put("/wopi/files/{filename}/contents", TEST_FILENAME)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content("updated content".getBytes()))
                .andExpect(status().isOk());

        verify(documentFileRepository, never()).delete(any());
    }

    @Test
    @Order(8)
    @DisplayName("saveFileContents - Temp file → overwrite original + delete temp")
    void saveFileContents_TempFile_OverwriteOriginal() throws Exception {
        // Tạo file gốc
        Path originalPath = tempDir.resolve("original.docx");
        Files.write(originalPath, "original content".getBytes());

        DocumentFiles tempRecord = new DocumentFiles();
        tempRecord.setAttachName(TEST_FILENAME);
        tempRecord.setIsTemp(true);
        tempRecord.setOriginalFileId(ORIGINAL_FILE_ID);

        DocumentFiles originalFile = new DocumentFiles();
        originalFile.setAttachName("original.docx");

        when(documentFileRepository.findByAttachName(TEST_FILENAME)).thenReturn(tempRecord);
        when(documentFileRepository.findById(ORIGINAL_FILE_ID)).thenReturn(Optional.of(originalFile));

        mockMvc.perform(post("/wopi/files/{filename}/contents", TEST_FILENAME)
                        .contentType(MediaType.APPLICATION_OCTET_STREAM)
                        .content("FINAL CONTENT".getBytes()))
                .andExpect(status().isOk());

        verify(documentFileRepository, times(1)).delete(tempRecord);
    }

    @Test
    @Order(9)
    @DisplayName("handlePost - LOCK/UNLOCK/GETLOCK → 200 OK")
    void handlePost_LockOperations() throws Exception {
        mockMvc.perform(post("/wopi/files/{filename}", TEST_FILENAME)
                        .header("X-WOPI-Override", "LOCK"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/wopi/files/{filename}", TEST_FILENAME)
                        .header("X-WOPI-Override", "UNLOCK"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/wopi/files/{filename}", TEST_FILENAME)
                        .header("X-WOPI-Override", "GETLOCK"))
                .andExpect(status().isOk());
    }
}