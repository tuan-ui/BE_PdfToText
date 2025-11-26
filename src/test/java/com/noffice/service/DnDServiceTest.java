package com.noffice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noffice.dto.DnDDTO;
import com.noffice.dto.FormResponseDTO;
import com.noffice.dto.FormSchemaSearchDTO;
import com.noffice.entity.FormData;
import com.noffice.entity.FormSchema;
import com.noffice.entity.User;
import com.noffice.repository.FormDataRepository;
import com.noffice.repository.FormSchemaRepository;
import com.noffice.ultils.Constants;
import com.noffice.ultils.FormParser;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
class DnDServiceTest {

    @Mock
    private FormSchemaRepository formSchemaRepository;

    @Mock
    private FormDataRepository formDataRepository;

    @InjectMocks
    private DnDService dndService;

    private User user;
    private ObjectMapper mapper;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setPartnerId(UUID.randomUUID());
        mapper = new ObjectMapper();
    }

    private DnDDTO buildDTO(String id) {
        ObjectNode json = mapper.createObjectNode();
        json.put("title", "My Form");
        return DnDDTO.builder().id(id).content(json).build();
    }

    @Test
    void publishSchema_WithUUID() {
        String id = UUID.randomUUID().toString();
        DnDDTO dto = buildDTO(id);

        when(formSchemaRepository.getFormSchemaByTemplateID(any())).thenReturn(null);

        String result = dndService.publishSchema(dto, user);
        assertEquals(id, result);

        verify(formSchemaRepository).save(any(FormSchema.class));
    }

    @Test
    void publishSchema_WithFormCode() {
        DnDDTO dto = buildDTO("FORM01");

        when(formSchemaRepository.getFormSchema("FORM01")).thenReturn(null);

        String result = dndService.publishSchema(dto, user);
        assertEquals("FORM01", result);

        verify(formSchemaRepository).save(any(FormSchema.class));
    }

    @Test
    void publishSchema_ThrowsException() {
        DnDDTO dto = buildDTO("ERR");
        when(formSchemaRepository.getFormSchema("ERR")).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> dndService.publishSchema(dto, user));
    }

    @Test
    void getContent_Uuid() {
        UUID id = UUID.randomUUID();
        FormSchema fs = new FormSchema();
        when(formSchemaRepository.getFormSchemaByTemplateID(id)).thenReturn(fs);

        FormSchema result = dndService.getContent(id.toString());
        assertNotNull(result);
    }

    @Test
    void getContent_FormCode() {
        when(formSchemaRepository.getFormSchema("FORM01")).thenReturn(new FormSchema());
        assertNotNull(dndService.getContent("FORM01"));
    }

    @Test
    void getContent_Exception() {
        when(formSchemaRepository.getFormSchema("ERR"))
                .thenThrow(new RuntimeException("ERR"));
        assertThrows(RuntimeException.class, () -> dndService.getContent("ERR"));
    }

    @Test
    void saveContent_Success() {
        DnDDTO dto = buildDTO("FORM01");
        String result = dndService.saveContent(dto, user);
        assertEquals("FORM01", result);
        verify(formDataRepository).save(any(FormData.class));
    }

    @Test
    void saveContent_Exception() {
        DnDDTO dto = buildDTO("FORM01");
        doThrow(new RuntimeException("ERR"))
                .when(formDataRepository).save(any());

        assertThrows(RuntimeException.class, () -> dndService.saveContent(dto, user));
    }

    @Test
    void searchFormSchemas_TestPaging() {
        FormSchemaSearchDTO req = new FormSchemaSearchDTO();
        req.setFormCode("A");
        req.setFormName("B");
        req.setStatus(true);
        req.setPage(0);
        req.setSize(10);

        Page<FormSchema> page = new PageImpl<>(List.of(new FormSchema()));

        when(formSchemaRepository.searchFormSchemas(any(), any(), any(), any(), any()))
                .thenReturn(page);

        Page<FormSchema> result = dndService.searchFormSchemas(req, UUID.randomUUID());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void delete_Success() {
        UUID id = UUID.randomUUID();
        FormSchema fs = new FormSchema();
        when(formSchemaRepository.findByFormSchemaId(id)).thenReturn(fs);

        String result = dndService.delete(id, user);
        assertEquals("", result);
        verify(formSchemaRepository).save(fs);
        assertEquals(Constants.isDeleted.DELETED, fs.getIsDeleted());
    }

    @Test
    void delete_NotFound() {
        UUID id = UUID.randomUUID();
        when(formSchemaRepository.findByFormSchemaId(id)).thenReturn(null);

        assertEquals("error.DndCodeNotExists", dndService.delete(id, user));
    }

    @Test
    void lockUser_Success() {
        UUID id = UUID.randomUUID();
        FormSchema fs = new FormSchema();
        fs.setIsActive(true);

        when(formSchemaRepository.findByFormSchemaId(id)).thenReturn(fs);

        String result = dndService.lockUser(id, user);
        assertEquals("", result);
        assertFalse(fs.getIsActive());
    }

    @Test
    void lockUser_NotFound() {
        when(formSchemaRepository.findByFormSchemaId(any())).thenReturn(null);
        assertEquals("error.DndCodeNotExists", dndService.lockUser(UUID.randomUUID(), user));
    }

    @Test
    void deleteMuti_Success() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        when(formSchemaRepository.findByFormSchemaId(any()))
                .thenReturn(new FormSchema());

        String result = dndService.deleteMuti(List.of(id1, id2), user);
        assertEquals("", result);
        verify(formSchemaRepository, times(2)).save(any());
    }

    @Test
    void deleteMuti_NotFound() {
        when(formSchemaRepository.findByFormSchemaId(any())).thenReturn(null);
        String result = dndService.deleteMuti(List.of(UUID.randomUUID()), user);
        assertEquals("error.DndCodeNotExists", result);
    }

    @Test
    void getResponsesBySchema_Test() {
        // Mock FormData from DB
        FormData fd = new FormData();
        fd.setFormContent("{\"at\": 1700000000000, \"name\":\"John\"}");

        when(formDataRepository.getFormSchema("FORM01"))
                .thenReturn(List.of(fd));

        // Build FormResponseDTO returned by parser
        FormResponseDTO mockResponse = new FormResponseDTO();
        mockResponse.setAt(1700000000000L); // test conversion to LocalDateTime

        Map<String, String> map = new HashMap<>();
        map.put("name", "John");
        mockResponse.setResponses(map);

        try (MockedStatic<FormParser> mocked = mockStatic(FormParser.class)) {

            mocked.when(() -> FormParser.parseFormContent(any()))
                    .thenReturn(mockResponse);

            List<FormResponseDTO> result = dndService.getResponsesBySchema("FORM01");

            assertEquals(1, result.size());
            assertEquals("John", result.get(0).getResponses().get("name"));
            assertNotNull(result.get(0).getAt()); // ensure setAt() worked
        }
    }

    @Test
    void summarizeResponses_Test() {

        // Prepare FormData list
        FormData fd1 = new FormData();
        fd1.setFormContent("{}");

        FormData fd2 = new FormData();
        fd2.setFormContent("{}");

        when(formDataRepository.getFormSchema("FORM01"))
                .thenReturn(List.of(fd1, fd2));

        // Response 1
        FormResponseDTO r1 = new FormResponseDTO();
        r1.setAt(1700000000000L);

        Map<String, String> m1 = new HashMap<>();
        m1.put("name", "A");
        r1.setResponses(m1);

        // Response 2
        FormResponseDTO r2 = new FormResponseDTO();
        r2.setAt(1700000005000L);

        Map<String, String> m2 = new HashMap<>();
        m2.put("name", "B");
        r2.setResponses(m2);

        try (MockedStatic<FormParser> mocked = mockStatic(FormParser.class)) {

            mocked.when(() -> FormParser.parseFormContent(any()))
                    .thenReturn(r1, r2);

            Map<String, List<String>> result = dndService.summarizeResponses("FORM01");

            assertNotNull(result);
            assertTrue(result.containsKey("name"));
            assertEquals(List.of("A", "B"), result.get("name"));
        }
    }

}
