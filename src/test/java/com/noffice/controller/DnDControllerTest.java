package com.noffice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noffice.config.TestSecurityConfig;
import com.noffice.dto.DnDDTO;
import com.noffice.dto.FormSchemaSearchDTO;
import com.noffice.entity.FormSchema;
import com.noffice.entity.User;
import com.noffice.filter.JwtAuthenticationFilter;
import com.noffice.service.DnDService;
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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DnDController.class)
@Import(TestSecurityConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureMockMvc(addFilters = false)
class DnDControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DnDService dndService;
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
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setPartnerId(UUID.fromString("411f2d97-96ed-4bd2-a480-cec1afba65e8"));

        Authentication auth = new TestingAuthenticationToken(
                mockUser, null, "ROLE_USER"
        );
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void clean() {
        SecurityContextHolder.clearContext();
    }
    @Test
    void saveContent_Success() throws Exception {
        DnDDTO dto = new DnDDTO();
        Mockito.when(dndService.saveContent(any(), any())).thenReturn("OK");

        mockMvc.perform(post("/api/DnD/saveContent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void saveContent_Exception() throws Exception {
        DnDDTO dto = new DnDDTO();
        Mockito.when(dndService.saveContent(any(), any())).thenThrow(new RuntimeException("ERR"));

        mockMvc.perform(post("/api/DnD/saveContent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void publishSchema_Success() throws Exception {
        DnDDTO dto = new DnDDTO();
        Mockito.when(dndService.publishSchema(any(), any())).thenReturn("OK");

        mockMvc.perform(post("/api/DnD/publishSchema")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void publishSchema_Exception() throws Exception {
        DnDDTO dto = new DnDDTO();
        Mockito.when(dndService.publishSchema(any(), any())).thenThrow(new RuntimeException("ERR"));

        mockMvc.perform(post("/api/DnD/publishSchema")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500));
    }

    @Test
    void getContent_Success() throws Exception {
        FormSchema f = new FormSchema();
        f.setFormContent("ABC");

        Mockito.when(dndService.getContent("123")).thenReturn(f);

        mockMvc.perform(get("/api/DnD/getContent/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").value("ABC"));
    }

    @Test
    void getContent_NotFound() throws Exception {
        Mockito.when(dndService.getContent("123")).thenReturn(null);

        mockMvc.perform(get("/api/DnD/getContent/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object").isEmpty());
    }

    @Test
    void getContent_Exception() throws Exception {
        Mockito.when(dndService.getContent(anyString())).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/DnD/getContent/123"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void summarize_Success() throws Exception {
        Map<String, List<String>> map = Map.of("a", List.of("1", "2"));
        Mockito.when(dndService.summarizeResponses(anyString())).thenReturn(map);

        mockMvc.perform(post("/api/DnD/getContent/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.object.a[0]").value("1"));
    }

    @Test
    void summarize_Exception() throws Exception {
        Mockito.when(dndService.summarizeResponses(anyString()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/DnD/getContent/999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void searchFormSchemas_Success() throws Exception {
        FormSchemaSearchDTO req = new FormSchemaSearchDTO();
        req.setFormCode("ABC");
        req.setFormName("NAME");

        Page<FormSchema> page = new PageImpl<>(List.of(new FormSchema()), PageRequest.of(0, 10), 1);
        Mockito.when(dndService.searchFormSchemas(any(), any()))
                .thenReturn(page);

        mockMvc.perform(post("/api/DnD/searchFormSchemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS))
                .andExpect(jsonPath("$.status").value(200));
    }

    @Test
    void searchFormSchemas_Exception() throws Exception {
        FormSchemaSearchDTO req = new FormSchemaSearchDTO();
        Mockito.when(dndService.searchFormSchemas(any(), any()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/DnD/searchFormSchemas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void delete_Success() throws Exception {
        Mockito.when(dndService.delete(any(), any())).thenReturn("");

        mockMvc.perform(get("/api/DnD/delete")
                        .param("id", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Xóa thành công"));
    }

    @Test
    void delete_BusinessError() throws Exception {
        Mockito.when(dndService.delete(any(), any())).thenReturn("Không xóa được");

        mockMvc.perform(get("/api/DnD/delete")
                        .param("id", UUID.randomUUID().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Không xóa được"));
    }

    @Test
    void delete_Exception() throws Exception {
        Mockito.when(dndService.delete(any(), any()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/DnD/delete")
                        .param("id", UUID.randomUUID().toString()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void deleteMuti_Success() throws Exception {
        Mockito.when(dndService.deleteMuti(anyList(), any())).thenReturn("");

        mockMvc.perform(get("/api/DnD/deleteMuti")
                        .param("id", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteMuti_BusinessError() throws Exception {
        Mockito.when(dndService.deleteMuti(anyList(), any())).thenReturn("Không xóa");

        mockMvc.perform(get("/api/DnD/deleteMuti")
                        .param("id", UUID.randomUUID().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Không xóa"));
    }

    @Test
    void deleteMuti_Exception() throws Exception {
        Mockito.when(dndService.deleteMuti(anyList(), any()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/DnD/deleteMuti")
                        .param("id", UUID.randomUUID().toString()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void lock_Success() throws Exception {
        Mockito.when(dndService.lockUser(any(), any())).thenReturn("OK");

        mockMvc.perform(get("/api/DnD/lock")
                        .param("id", UUID.randomUUID().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(Constants.messageResponse.SUCCESS));
    }

    @Test
    void lock_Exception() throws Exception {
        Mockito.when(dndService.lockUser(any(), any()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/DnD/lock")
                        .param("id", UUID.randomUUID().toString()))
                .andExpect(status().isInternalServerError());
    }
}
