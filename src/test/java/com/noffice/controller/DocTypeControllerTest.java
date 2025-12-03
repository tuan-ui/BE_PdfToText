package com.noffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noffice.config.TestSecurityConfig;
import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.DocType;
import com.noffice.entity.User;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.service.DocTypeService;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocTypeController.class)
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc(addFilters = false)
public class DocTypeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocTypeService docTypeService;
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
    private DocType docType;
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

        docType = new DocType();
        docType.setId(testId);
        docType.setId(UUID.randomUUID());
        docType.setDocTypeCode("HC");
        docType.setDocTypeName("Hành chính");
        docType.setDocTypeDescription("Hành chính Description");
        docType.setCreateAt(LocalDate.of(2025, 1, 1).atStartOfDay());
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
        Page<DocType> page = new PageImpl<>(
                List.of(docType),
                PageRequest.of(0, 10),
                1L
        );

        Mockito.when(docTypeService.getListDocType(
                anyString(), anyString(), anyString(), anyString(),
                any(Pageable.class),
                eq(partnerId)
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-type/search")
                        .param("page", "0")
                        .param("size", "10")
                        .param("docTypeCode", "")
                        .param("docTypeName", "")
                        .param("docTypeDescription", "")
                        .param("searchString", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object.totalElements").value(1))
                .andExpect(jsonPath("$.object.totalPages").value(1))
                .andExpect(jsonPath("$.object.numberOfElements").value(1))
                .andExpect(jsonPath("$.object.content[0].docTypeCode").value("HC"))
                .andExpect(jsonPath("$.object.content[0].docTypeName").value("Hành chính"));
    }

    @Test
    void search_Fail() throws Exception {
        Page<DocType> page = new PageImpl<>(
                List.of(docType),
                PageRequest.of(0, 10),
                1L
        );

        Mockito.when(docTypeService.getListDocType(
                anyString(), anyString(), anyString(), anyString(),
                any(Pageable.class),
                eq(partnerId)
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-type/search")
                        .param("page", "-1")
                        .param("size", "-10")
                        .param("docTypeCode", "")
                        .param("docTypeName", "")
                        .param("docTypeDescription", "")
                        .param("searchString", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Constants.message.SYSTEM_ERROR))
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void deleteDoc_Success() throws Exception {
        Mockito.when(docTypeService.deleteDocType(eq(testId), any(), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-type/delete")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteDoc_BusinessError() throws Exception {
        Mockito.when(docTypeService.deleteDocType(eq(testId), any(), anyLong()))
                .thenReturn("không thể xóa!");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-type/delete")
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

        Mockito.when(docTypeService.deleteMultiDocType(anyList(), any()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/doc-type/deleteMuti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteMulti_BusinessError() throws Exception {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(testId,"name","code", 1L),
                new DeleteMultiDTO(UUID.randomUUID(),"name","code", 1L)
        );

        Mockito.when(docTypeService.deleteMultiDocType(anyList(), any()))
                .thenReturn("không thể xóa");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/doc-type/deleteMuti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("không thể xóa"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void lock_Success() throws Exception {
        Mockito.when(docTypeService.lockUnlockDocType(eq(testId), any(), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-type/lock")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void lock_BusinessError() throws Exception {
        Mockito.when(docTypeService.lockUnlockDocType(eq(testId), any(), anyLong()))
                .thenReturn("không thể thay đổi!");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-type/lock")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("không thể thay đổi!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addDocType_Success() throws Exception {

        // docTypeService.saveDocType trả "" -> success
        Mockito.when(docTypeService.saveDocType(any(), any()))
                .thenReturn("");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/doc-type/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(docType))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.ADD_SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void addDocType_Fail() throws Exception {

        Mockito.when(docTypeService.saveDocType(any(), any()))
                .thenReturn("Name already exists");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/doc-type/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(docType))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name already exists"))
                .andExpect(jsonPath("$.status").value(400));
    }


    @Test
    void updateDocType_Success() throws Exception {

        Mockito.when(docTypeService.updateDocType(any(), any()))
                .thenReturn("");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/doc-type/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(docType))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.UPDATE_SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void updateDocType_Fail() throws Exception {

        Mockito.when(docTypeService.updateDocType(any(), any()))
                .thenReturn("not found");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/doc-type/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(docType))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("not found"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getLogDetailDocType_Success() throws Exception {

        Mockito.doNothing().when(docTypeService).getLogDetailDocType(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-type/LogDetailDocType")
                        .param("id", testId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getAllDocType_Success() throws Exception {

        Mockito.when(docTypeService.getAllDocType(
                eq(partnerId)
        )).thenReturn(List.of(docType));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/doc-type/getAllDocType"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object[0].docTypeCode").value("HC"))
                .andExpect(jsonPath("$.object[0].docTypeName").value("Hành chính"));
    }

    @Test
    void checkDeleteMulti_Success() throws Exception {
        ErrorListResponse response = new ErrorListResponse();
        ErrorListResponse.ErrorResponse errorResponse = new ErrorListResponse.ErrorResponse();
        response.setErrors(List.of(errorResponse));

        Mockito.when(docTypeService.checkDeleteMulti(anyList())).thenReturn(response);

        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(testId,"name","code", 1L));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/doc-type/checkDeleteMulti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk());
    }
}
