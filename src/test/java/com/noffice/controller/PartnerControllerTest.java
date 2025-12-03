package com.noffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noffice.config.TestSecurityConfig;
import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.PartnerRequest;
import com.noffice.entity.Partners;
import com.noffice.entity.User;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.service.PartnerService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PartnerController.class)
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc(addFilters = false)
public class PartnerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartnerService partnerService;
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
    private Partners partner;
    private PartnerRequest partnerRequest;
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

        partner = new Partners();
        partner.setId(testId);
        partner.setId(UUID.randomUUID());
        partner.setPartnerCode("HC");
        partner.setPartnerName("Hành chính");
        partner.setCreateAt(LocalDate.of(2025, 1, 1).atStartOfDay());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.setDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));

        partnerRequest = new PartnerRequest();
        partnerRequest.setId(testId);
        partnerRequest.setPartnerName("Hành chính");
        partnerRequest.setEmail("111@gmail.com");
        partnerRequest.setPhone("1234567890");
        partnerRequest.setPartnerCode("HC");
        partnerRequest.setTaxCode("1231231231");
        partnerRequest.setWebsite("www.google.com");
        partnerRequest.setAddress("123");
        partnerRequest.setFax("123");
        partnerRequest.setIsActive("true");
        partnerRequest.setBase64Image("123");
        partnerRequest.setPage(0);
        partnerRequest.setSize(10);
        partnerRequest.setOffset(5);
        partnerRequest.setSearchString("123");
        partnerRequest.setVersion(1L);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void search_Success() throws Exception {
        Page<Partners> page = new PageImpl<>(
                List.of(partner),
                PageRequest.of(0, 10),
                1L
        );

        Mockito.when(partnerService.searchPartners(
                any(),
                any(Pageable.class)
        )).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/partner/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partnerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.numberOfElements").value(1))
                .andExpect(jsonPath("$.data.content[0].partnerCode").value("HC"))
                .andExpect(jsonPath("$.data.content[0].partnerName").value("Hành chính"));
    }

    @Test
    void deleteDoc_Success() throws Exception {
        Mockito.when(partnerService.deletePartner(eq(testId), any(), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/partner/delete")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void deleteDoc_BusinessError() throws Exception {
        Mockito.when(partnerService.deletePartner(eq(testId), any(), anyLong()))
                .thenReturn("không thể xóa!");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/partner/delete")
                        .param("id", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("không thể xóa!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void deleteMulti_Success() throws Exception {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(testId,"name","code", 1L),
                new DeleteMultiDTO(UUID.randomUUID(),"name","code", 1L)
        );

        Mockito.when(partnerService.deleteMultiPartner(anyList(), any()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/partner/deleteMulti")
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

        Mockito.when(partnerService.deleteMultiPartner(anyList(), any()))
                .thenReturn("không thể xóa");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/partner/deleteMulti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("không thể xóa"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void lock_Success() throws Exception {
        Mockito.when(partnerService.lockPartner(eq(testId), any(), anyLong()))
                .thenReturn("");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/partner/lock")
                        .param("partner", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void lock_BusinessError() throws Exception {
        Mockito.when(partnerService.lockPartner(eq(testId), any(), anyLong()))
                .thenReturn("không thể thay đổi!");

        mockMvc.perform(MockMvcRequestBuilders.get("/api/partner/lock")
                        .param("partner", testId.toString())
                        .param("version", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("không thể thay đổi!"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void addPartner_Success() throws Exception {

        // partnerService.savePartner trả "" -> success
        Mockito.when(partnerService.createPartner(any(), any()))
                .thenReturn("");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/partner/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partner))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.ADD_SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void addPartner_Fail() throws Exception {

        Mockito.when(partnerService.createPartner(any(), any()))
                .thenReturn("Name already exists");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/partner/create")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partner))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("Name already exists"))
                .andExpect(jsonPath("$.status").value(400));
    }


    @Test
    void updatePartner_Success() throws Exception {

        Mockito.when(partnerService.updatePartner(any(), any()))
                .thenReturn("");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/partner/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partner))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.UPDATE_SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void updatePartner_Fail() throws Exception {

        Mockito.when(partnerService.updatePartner(any(), any()))
                .thenReturn("not found");

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/api/partner/update")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(partner))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("not found"))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void getLogDetailPartner_Success() throws Exception {

        Mockito.doNothing().when(partnerService).getLogDetailPartner(any(), any());

        mockMvc.perform(MockMvcRequestBuilders.get("/api/partner/LogDetailPartner")
                        .param("id", testId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }


    @Test
    void checkDeleteMulti_Success() throws Exception {
        ErrorListResponse response = new ErrorListResponse();
        ErrorListResponse.ErrorResponse errorResponse = new ErrorListResponse.ErrorResponse();
        response.setErrors(List.of(errorResponse));

        Mockito.when(partnerService.checkDeleteMulti(anyList(), any(User.class))).thenReturn(response);

        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(testId,"name","code", 1L));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/partner/checkDeleteMulti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ids)))
                .andExpect(status().isOk());
    }

    @Test
    void updateImage_Success() throws Exception {

        Mockito.when(partnerService.updatePartnerImage(
                any(), any(User.class)
        )).thenReturn(partner);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/partner/updateImage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(partnerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value(Constants.message.SUCCESS))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.partnerCode").value("HC"))
                .andExpect(jsonPath("$.data.partnerName").value("Hành chính"));
    }
}
