package com.noffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noffice.config.TestSecurityConfig;
import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.DocDocumentDTO;
import com.noffice.entity.*;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.service.DocDocumentService;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocDocumentController.class)
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc(addFilters = false)
class DocDocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocDocumentService docDocumentService;
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
    private DocDocumentDTO sampleDoc;

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

        sampleDoc = new DocDocumentDTO();
        sampleDoc.setId(testId);
        sampleDoc.setId(UUID.randomUUID());
        sampleDoc.setDocumentTitle("HC");
        sampleDoc.setDocTypeName("Hành chính");
        sampleDoc.setCreateAt(LocalDate.of(2025, 1, 1).atStartOfDay());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void search_Success() throws Exception {
        Page<DocDocumentDTO> page = new PageImpl<>(
                List.of(sampleDoc),
                PageRequest.of(0, 10),
                1L
        );

        Mockito.when(docDocumentService.getListDoc(
                anyString(), anyString(), anyString(), anyString(),
                any(Pageable.class),
                eq(partnerId)
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-document/search")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "")
                        .param("docTypeCode", "")
                        .param("docTypeName", "")
                        .param("docTypeDescription", "")
                        .param("searchString", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object.totalElements").value(1))
                .andExpect(jsonPath("$.object.totalPages").value(1))
                .andExpect(jsonPath("$.object.numberOfElements").value(1))
                .andExpect(jsonPath("$.object.content[0].documentTitle").value("HC"))
                .andExpect(jsonPath("$.object.content[0].docTypeName").value("Hành chính"))
                .andExpect(jsonPath("$.object.content[0].createAt").value("01-01-2025 00:00:00"));
    }

    @Test
    void search_Fail() throws Exception {
        Page<DocDocumentDTO> page = new PageImpl<>(
                List.of(sampleDoc),
                PageRequest.of(0, 10),
                1L
        );

        Mockito.when(docDocumentService.getListDoc(
                anyString(), anyString(), anyString(), anyString(),
                any(Pageable.class),
                eq(partnerId)
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-document/search")
                        .param("page", "-1")
                        .param("size", "-10")
                        .param("status", "")
                        .param("docTypeDescription", "")
                        .param("searchString", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void deleteDoc_Success() throws Exception {
        Mockito.when(docDocumentService.delete(eq(testId), any(User.class)))
                .thenReturn(true); // result == null → thành công

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-document/delete")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteDoc_BusinessError() throws Exception {
        Mockito.when(docDocumentService.delete(eq(testId), any(User.class)))
                .thenReturn(false);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-document/delete")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("error"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteMulti_Success() throws Exception {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(testId,"name","code", 1L),
                new DeleteMultiDTO(UUID.randomUUID(),"name","code", 1L)
        );

        Mockito.when(docDocumentService.deleteMulti(anyList(), any()))
                .thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/doc-document/deleteMuti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteMulti_BusinessError() throws Exception {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(testId,"name","code", 1L),
                new DeleteMultiDTO(UUID.randomUUID(),"name","code", 1L)
        );

        Mockito.when(docDocumentService.deleteMulti(anyList(), any()))
                .thenReturn("không thể xóa");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/doc-document/deleteMuti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("không thể xóa"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void lock_Success() throws Exception {
        Mockito.when(docDocumentService.lockUnlock(eq(testId), any(), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-document/lock")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void lock_BusinessError() throws Exception {
        Mockito.when(docDocumentService.lockUnlock(eq(testId), any(), anyLong()))
                .thenReturn("không thể thay đổi!");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-document/lock")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("không thể thay đổi!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createOrUpdate_Success() throws Exception {
        MockMultipartFile jsonPart = new MockMultipartFile(
                "docDocument", "", "application/json",
                objectMapper.writeValueAsBytes(sampleDoc));

        MockMultipartFile filePart = new MockMultipartFile(
                "files", "test.pdf", "application/pdf", "PDF content".getBytes());

        Mockito.when(docDocumentService.save(any(), any(), any()))
                .thenReturn(new DocDocument());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/doc-document/createOrUpdate")
                        .file(jsonPart)
                        .file(filePart)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Thêm mới thành công"));
    }

    @Test
    void createOrUpdate_Fail() throws Exception {
        MockMultipartFile jsonPart = new MockMultipartFile(
                "docDocument", "", "application/json",
                objectMapper.writeValueAsBytes(sampleDoc));

        MockMultipartFile filePart = new MockMultipartFile(
                "files", "test.pdf", "application/pdf", "PDF content".getBytes());

        Mockito.when(docDocumentService.save(any(), any(), any()))
                .thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/doc-document/createOrUpdate")
                        .file(jsonPart)
                        .file(filePart)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Thao tác thất bại"))
                .andExpect(jsonPath("$.status").value(400));
    }


    @Test
    void createOrUpdate_InvalidJson() throws Exception {
        MockMultipartFile invalidJson = new MockMultipartFile(
                "docDocument", "", "application/json", "{invalid json}".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/doc-document/createOrUpdate")
                        .file(invalidJson)
                        .with(request -> {
                            request.setMethod("POST");
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Lỗi dữ liệu đầu vào"));
    }

    @Test
    void getLogDetailDocType_Success() throws Exception {

        Mockito.doNothing().when(docDocumentService).getLogDetailDocType(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-document/LogDetailDocType")
                        .param("id", testId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getAllDocType_Success() throws Exception {
        List<DocType> docTypes = List.of(new DocType("VBPL", "Văn bản pháp luật", "Mô tả"));
        Mockito.when(docDocumentService.getAllDocType(any())).thenReturn(docTypes);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-document/getAllDocType"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object[0].docTypeCode").value("VBPL"))
                .andExpect(jsonPath("$.object[0].docTypeName").value("Văn bản pháp luật"));
    }

    @Test
    void getAttachs_Success() throws Exception {
        Attachs at = new Attachs();
        at.setAttachName("file.pdf");
        List<Attachs> attachs = List.of(at);
        Mockito.when(docDocumentService.getAttachsByDocument(eq(testId), eq(Constants.OBJECT_TYPE.DOC_DOCUMENT)))
                .thenReturn(attachs);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-document/attachs")
                        .param("id", testId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object[0].attachName").value("file.pdf"));
    }

    @Test
    void checkDeleteMulti_Success() throws Exception {
        ErrorListResponse response = new ErrorListResponse();
        ErrorListResponse.ErrorResponse errorResponse = new ErrorListResponse.ErrorResponse();
        response.setErrors(List.of(errorResponse));

        Mockito.when(docDocumentService.checkDeleteMulti(anyList())).thenReturn(response);

        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(testId,"name","code", 1L));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/doc-document/checkDeleteMulti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk());
    }
}