package com.noffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noffice.config.TestSecurityConfig;
import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.Domain;
import com.noffice.entity.User;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.service.*;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(DomainController.class)
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc(addFilters = false)
public class DomainControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DomainService domainService;
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

    private User mockUser;
    private UUID partnerId;
    private UUID testId;
    private Domain domain;
    @BeforeEach
    void setUp() {
        partnerId = UUID.fromString("411f2d97-96ed-4bd2-a480-cec1afba65e8");
        testId = UUID.randomUUID();
        mockUser = new User();
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

        domain = new Domain();
        domain.setId(testId);
        domain.setId(UUID.randomUUID());
        domain.setDomainCode("HC");
        domain.setDomainName("Hành chính");
        domain.setDomainDescription("Hành chính Description");
        domain.setCreateAt(LocalDate.of(2025, 1, 1).atStartOfDay());
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
        Page<Domain> page = new PageImpl<>(
                List.of(domain),
                PageRequest.of(0, 10),
                1L
        );

        Mockito.when(domainService.getListDomain(
                anyString(), anyString(), anyString(), anyString(),
                any(Pageable.class),
                eq(partnerId)
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/domains/searchDomains")
                        .param("page", "0")
                        .param("size", "10")
                        .param("domainCode", "")
                        .param("domainName", "")
                        .param("domainDescription", "")
                        .param("searchString", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object.totalElements").value(1))
                .andExpect(jsonPath("$.object.totalPages").value(1))
                .andExpect(jsonPath("$.object.numberOfElements").value(1))
                .andExpect(jsonPath("$.object.content[0].domainCode").value("HC"))
                .andExpect(jsonPath("$.object.content[0].domainName").value("Hành chính"));
    }

    @Test
    void search_Fail() throws Exception {
        Page<Domain> page = new PageImpl<>(
                List.of(domain),
                PageRequest.of(0, 10),
                1L
        );

        Mockito.when(domainService.getListDomain(
                anyString(), anyString(), anyString(), anyString(),
                any(Pageable.class),
                eq(partnerId)
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/domains/searchDomains")
                        .param("page", "-1")
                        .param("size", "-10")
                        .param("domainCode", "")
                        .param("domainName", "")
                        .param("domainDescription", "")
                        .param("searchString", ""))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("fail"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteDoc_Success() throws Exception {
        Mockito.when(domainService.deleteDomain(eq(testId), any(), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/domains/delete")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteDoc_BusinessError() throws Exception {
        Mockito.when(domainService.deleteDomain(eq(testId), any(), anyLong()))
                .thenReturn("không thể xóa!");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/domains/delete")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("không thể xóa!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteMulti_Success() throws Exception {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(testId, 1L),
                new DeleteMultiDTO(UUID.randomUUID(), 1L)
        );

        Mockito.when(domainService.deleteMultiDomain(anyList(), any()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/domains/deleteMuti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteMulti_BusinessError() throws Exception {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(testId, 1L),
                new DeleteMultiDTO(UUID.randomUUID(), 1L)
        );

        Mockito.when(domainService.deleteMultiDomain(anyList(), any()))
                .thenReturn("không thể xóa");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/domains/deleteMuti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("không thể xóa"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void lock_Success() throws Exception {
        Mockito.when(domainService.lockUnlockDomain(eq(testId), any(), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/domains/lock")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void lock_BusinessError() throws Exception {
        Mockito.when(domainService.lockUnlockDomain(eq(testId), any(), anyLong()))
                .thenReturn("không thể thay đổi!");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/domains/lock")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("không thể thay đổi!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addDomain_Success() throws Exception {

        // domainService.saveDomain trả "" -> success
        Mockito.when(domainService.saveDomain(any(), any()))
                .thenReturn("");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/domains/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(domain))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Thêm mới thành công"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void addDomain_Fail() throws Exception {

        Mockito.when(domainService.saveDomain(any(), any()))
                .thenReturn("Name already exists");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/domains/add")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(domain))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Name already exists"))
                .andExpect(jsonPath("$.status").value(400));
    }


    @Test
    void updateDomain_Success() throws Exception {

        Mockito.when(domainService.updateDomain(any(), any()))
                .thenReturn("");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/domains/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(domain))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Cập nhật thành công"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void updateDomain_Fail() throws Exception {

        Mockito.when(domainService.updateDomain(any(), any()))
                .thenReturn("not found");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/domains/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(domain))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("not found"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getLogDetailDocType_Success() throws Exception {

        Mockito.doNothing().when(domainService).getLogDetailDomain(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/domains/LogDetailDomain")
                        .param("id", testId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void getAllDomain_Success() throws Exception {

        Mockito.when(domainService.getAllDomain(
                eq(partnerId)
        )).thenReturn(List.of(domain));

        mockMvc.perform(MockMvcRequestBuilders.get("/api/domains/getAllDomain"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("success"))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object[0].domainCode").value("HC"))
                .andExpect(jsonPath("$.object[0].domainName").value("Hành chính"));
    }

    @Test
    void checkDeleteMulti_Success() throws Exception {
        ErrorListResponse response = new ErrorListResponse();
        ErrorListResponse.ErrorResponse errorResponse = new ErrorListResponse.ErrorResponse();
        response.setErrors(List.of(errorResponse));

        Mockito.when(domainService.checkDeleteMulti(anyList())).thenReturn(response);

        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(testId, 1L));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/domains/checkDeleteMulti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk());
    }

}
