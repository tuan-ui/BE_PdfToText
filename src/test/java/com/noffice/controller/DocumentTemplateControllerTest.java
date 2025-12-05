package com.noffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noffice.config.TestSecurityConfig;
import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.DocumentTemplateDTO;
import com.noffice.dto.DocumentTemplateDetailDTO;
import com.noffice.entity.DocumentTemplate;
import com.noffice.entity.User;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.service.DocumentTemplateService;
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
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentTemplateController.class)
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentTemplateControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentTemplateService documentTemplateService;
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
    private DocumentTemplateDTO documentTemplateDTO;
    private DocumentTemplate documentTemplate;
    private DocumentTemplateDetailDTO documentTemplateDetailDTO;
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

        documentTemplateDTO = new DocumentTemplateDTO();
        documentTemplateDTO.setId(testId);
        documentTemplateDTO.setDocumentTemplateCode("HC");
        documentTemplateDTO.setDocumentTemplateName("Hành chính");
        documentTemplateDTO.setDocumentTemplateDescription("Hành chính Description");

        documentTemplate = new DocumentTemplate();
        documentTemplate.setId(testId);
        documentTemplate.setDocumentTemplateCode("HC");
        documentTemplate.setDocumentTemplateName("Hành chính");
        documentTemplate.setDocumentTemplateDescription("Hành chính Description");


        documentTemplateDetailDTO = new DocumentTemplateDetailDTO();
        documentTemplateDetailDTO.setId(testId);
        documentTemplateDetailDTO.setDocumentTemplateCode("HC");
        documentTemplateDetailDTO.setDocumentTemplateName("Hành chính");
        documentTemplateDetailDTO.setDocumentTemplateDescription("Hành chính Description");

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
        Page<DocumentTemplateDTO> page = new PageImpl<>(
                List.of(documentTemplateDTO),
                PageRequest.of(0, 10),
                1L
        );

        Mockito.when(documentTemplateService.getListDocumentTemplate(
                anyString(), anyString(), anyString(), anyString(),
                any(Pageable.class),
                eq(partnerId)
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/documentTemplates/search")
                        .param("page", "0")
                        .param("size", "10")
                        .param("documentTemplateCode", "")
                        .param("documentTemplateName", "")
                        .param("documentTemplateDescription", "")
                        .param("searchString", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object.totalElements").value(1))
                .andExpect(jsonPath("$.object.totalPages").value(1))
                .andExpect(jsonPath("$.object.numberOfElements").value(1))
                .andExpect(jsonPath("$.object.content[0].documentTemplateCode").value("HC"))
                .andExpect(jsonPath("$.object.content[0].documentTemplateName").value("Hành chính"));
    }

    @Test
    void search_Fail() throws Exception {
        Page<DocumentTemplateDTO> page = new PageImpl<>(
                List.of(documentTemplateDTO),
                PageRequest.of(0, 10),
                1L
        );

        Mockito.when(documentTemplateService.getListDocumentTemplate(
                anyString(), anyString(), anyString(), anyString(),
                any(Pageable.class),
                eq(partnerId)
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/documentTemplates/search")
                        .param("page", "-1")
                        .param("size", "-10")
                        .param("documentTemplateCode", "")
                        .param("documentTemplateName", "")
                        .param("documentTemplateDescription", "")
                        .param("searchString", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void deleteDoc_Success() throws Exception {
        Mockito.when(documentTemplateService.deleteDocumentTemplate(eq(testId), any(), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/documentTemplates/delete")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteDoc_BusinessError() throws Exception {
        Mockito.when(documentTemplateService.deleteDocumentTemplate(eq(testId), any(), anyLong()))
                .thenReturn("không thể xóa!");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/documentTemplates/delete")
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

        Mockito.when(documentTemplateService.deleteMultiDocumentTemplate(anyList(), any()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/documentTemplates/deleteMuti")
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

        Mockito.when(documentTemplateService.deleteMultiDocumentTemplate(anyList(), any()))
                .thenReturn("không thể xóa");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/documentTemplates/deleteMuti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("không thể xóa"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void lock_Success() throws Exception {
        Mockito.when(documentTemplateService.lockUnlockDocumentTemplate(eq(testId), any(), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/documentTemplates/lock")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void lock_BusinessError() throws Exception {
        Mockito.when(documentTemplateService.lockUnlockDocumentTemplate(eq(testId), any(), anyLong()))
                .thenReturn("không thể thay đổi!");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/documentTemplates/lock")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("không thể thay đổi!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addDocumentTemplate_Success() throws Exception {

        // documentTemplateService.saveDocumentTemplate trả "" -> success
        Mockito.when(documentTemplateService.saveDocumentTemplate(any(), any()))
                .thenReturn("");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/documentTemplates/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(documentTemplate))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Thêm mới thành công"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void addDocumentTemplate_Fail() throws Exception {

        Mockito.when(documentTemplateService.saveDocumentTemplate(any(), any()))
                .thenReturn("Name already exists");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/documentTemplates/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(documentTemplate))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name already exists"))
                .andExpect(jsonPath("$.status").value(400));
    }


    @Test
    void updateDocumentTemplate_Success() throws Exception {

        Mockito.when(documentTemplateService.updateDocumentTemplate(any(), any()))
                .thenReturn("");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/documentTemplates/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(documentTemplate))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật thành công"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void updateDocumentTemplate_Fail() throws Exception {

        Mockito.when(documentTemplateService.updateDocumentTemplate(any(), any()))
                .thenReturn("not found");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/documentTemplates/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(documentTemplate))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("not found"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getAllDocumentTemplate_Success() throws Exception {

        Mockito.when(documentTemplateService.getAllDocumentTemplate(
                eq(partnerId)
        )).thenReturn(List.of(documentTemplate));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/documentTemplates/getAllDocumentTemplate"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object[0].documentTemplateCode").value("HC"))
                .andExpect(jsonPath("$.object[0].documentTemplateName").value("Hành chính"));
    }

    @Test
    void checkDeleteMulti_Success() throws Exception {
        ErrorListResponse response = new ErrorListResponse();
        ErrorListResponse.ErrorResponse errorResponse = new ErrorListResponse.ErrorResponse();
        response.setErrors(List.of(errorResponse));

        Mockito.when(documentTemplateService.checkDeleteMulti(anyList())).thenReturn(response);

        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(testId,"name","code", 1L));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/documentTemplates/checkDeleteMulti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk());
    }

    @Test
    void getDocumentDetail_Success() throws Exception {

        Mockito.when(documentTemplateService.getDocumentDetail(
                eq(testId),
                any()
        )).thenReturn(documentTemplateDetailDTO);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/documentTemplates/getDocumentDetail")
                        .param("id", testId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object.documentTemplateCode").value("HC"))
                .andExpect(jsonPath("$.object.documentTemplateName").value("Hành chính"));
    }

    @Test
    void getAllowspermission_Success() throws Exception {
        List<Map<String, String>> editors = new ArrayList<>();
        editors.add(Map.of(
                "id", "22222222-2222-2222-2222-222222222222",
                "documentId", "11111111-1111-1111-1111-111111111111",
                "editorId", "33333333-3333-3333-3333-333333333333"
        ));

        List<Map<String, String>> viewers = new ArrayList<>();
        viewers.add(Map.of(
                "id", "44444444-4444-4444-4444-444444444444",
                "documentId", "11111111-1111-1111-1111-111111111111",
                "viewerId", "55555555-5555-5555-5555-555555555555"
        ));

        Map<String, Object> mockResponseObject = Map.of(
                "Editors", editors,
                "Viewers", viewers
        );

        Mockito.when(documentTemplateService.getAllowspermission(
                any()
        )).thenReturn(mockResponseObject);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/documentTemplates/getAllowspermission")
                        .param("id", "11111111-1111-1111-1111-111111111111"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.object").exists())
                .andExpect(jsonPath("$.object.Editors").isArray())
                .andExpect(jsonPath("$.object.Editors.length()").value(1))
                .andExpect(jsonPath("$.object.Editors[0].id").value("22222222-2222-2222-2222-222222222222"))
                .andExpect(jsonPath("$.object.Editors[0].editorId").value("33333333-3333-3333-3333-333333333333"))
                .andExpect(jsonPath("$.object.Viewers").isArray())
                .andExpect(jsonPath("$.object.Viewers.length()").value(1))
                .andExpect(jsonPath("$.object.Viewers[0].id").value("44444444-4444-4444-4444-444444444444"))
                .andExpect(jsonPath("$.object.Viewers[0].viewerId").value("55555555-5555-5555-5555-555555555555"));
    }
}
