package com.noffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.noffice.config.TestSecurityConfig;
import com.noffice.entity.Logs;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.enumtype.FunctionType;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.service.LogService;
import com.noffice.service.JwtService;
import com.noffice.service.UserDetailsServiceImpl;
import com.noffice.service.UserService;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(LogController.class)
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc(addFilters = false)
public class LogControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogService logService;
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

    @BeforeEach
    void setUp() {
        UUID partnerId = UUID.fromString("411f2d97-96ed-4bd2-a480-cec1afba65e8");
        UUID testId = UUID.randomUUID();
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

        Logs sampleDoc = new Logs();
        sampleDoc.setId(testId);
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
    void testGetLogs_ReturnsSuccess() throws Exception {
        Logs log = new Logs();
        Page<Logs> mockPage = new PageImpl<>(List.of(log));

        Mockito.when(logService.getLogs(
                any(), anyString(), anyString(), anyString(), anyString(),
                any(Pageable.class), any(UUID.class)
        )).thenReturn(mockPage);

        mockMvc.perform(get("/api/log/list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("userId", "")
                        .param("actionKey", "")
                        .param("functionKey", "")
                        .param("fromDateStr", "")
                        .param("toDateStr", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object.content").isArray());
    }

    @Test
    void testGetListFunction_ReturnsSuccess() throws Exception {

        List<String> mockList = List.of("FUNC_1", "FUNC_2");
        Mockito.mockStatic(FunctionType.class).when(FunctionType::getAllFunction).thenReturn(mockList);

        mockMvc.perform(get("/api/log/getListFunction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object[0]").value("FUNC_1"));
    }

    @Test
    void testGetListAction_ReturnsSuccess() throws Exception {

        List<String> mockList = List.of("CREATE", "UPDATE", "DELETE");
        Mockito.mockStatic(ActionType.class).when(ActionType::getAllActions).thenReturn(mockList);

        mockMvc.perform(get("/api/log/getListAction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.object[1]").value("UPDATE"));
    }
}
