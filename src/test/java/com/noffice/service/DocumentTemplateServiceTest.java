package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.DocumentTemplateCreateDTO;
import com.noffice.dto.DocumentTemplateDTO;
import com.noffice.dto.DocumentTemplateDetailDTO;
import com.noffice.entity.*;
import com.noffice.enumtype.ActionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.*;
import com.noffice.ultils.Constants;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocumentTemplateServiceTest {
    @Mock
    private DocumentTemplateRepository documentTemplateRepository;

    @Mock
    private LogService logService;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private DocumentTemplateService documentTemplateService;
    @Mock
    private DocumentTemplateDocumentTypesRepository documentTemplateDocumentTypesRepository;
    @Mock
    private DocumentAllowedEditorsRepository documentAllowedEditorsRepository;
    @Mock
    private DocumentAllowedViewersRepository documentAllowedViewersRepository;
    @Mock
    private ModelMapper mapper;
    @Mock
    private DocumentFileRepository documentFileRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private FormSchemaRepository formSchemaRepository;

    private User mockUser;
    private DocumentTemplate sampleDocumentTemplate;
    private UUID documentTemplateId;
    private UUID partnerId;
    private UUID testDocTemplateId;
    private UUID testPartnerId;
    private UUID attachFileId;
    private DocumentTemplate mockTemplate;
    private Long testVersion;
    private DocumentTemplateDTO mockDto;
    private DocumentFiles mockFile;

    @BeforeEach
    void setUp() {
        partnerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        documentTemplateId = UUID.randomUUID();
        testDocTemplateId = UUID.randomUUID();
        testPartnerId = UUID.randomUUID();
        attachFileId = UUID.randomUUID();
        testVersion = 1L;

        mockTemplate = new DocumentTemplate();
        mockTemplate.setId(documentTemplateId);
        mockTemplate.setVersion(testVersion);
        mockTemplate.setDocumentTemplateName("name");
        mockTemplate.setAttachFileId(attachFileId);

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setFullName("Nguyễn Văn Test");
        mockUser.setPartnerId(partnerId);

        sampleDocumentTemplate = new DocumentTemplate();
        sampleDocumentTemplate.setId(documentTemplateId);
        sampleDocumentTemplate.setDocumentTemplateName("Test DocumentTemplate");
        sampleDocumentTemplate.setDocumentTemplateCode("TEST001");
        sampleDocumentTemplate.setIsActive(true);
        sampleDocumentTemplate.setIsDeleted(Constants.isDeleted.ACTIVE);
        sampleDocumentTemplate.setVersion(1L);
        sampleDocumentTemplate.setPartnerId(partnerId);

        mockDto = new DocumentTemplateDTO();
        mockDto.setId(documentTemplateId);
        mockDto.setDocumentTemplateName("Template B");
        mockDto.setDocumentTypeIds(List.of(documentTemplateId));

        mockFile = new DocumentFiles();
        mockFile.setId(attachFileId);
        mockFile.setAttachName("document_a.pdf");
    }

    @Test
    void deleteDocumentTemplate_Success() {
        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(eq(documentTemplateId))).thenReturn(sampleDocumentTemplate);
        Mockito.doNothing().when(documentTemplateRepository).deleteDocumentTemplateByDocumentTemplateId(any(UUID.class));
        when(documentTemplateDocumentTypesRepository.existsDocumentTemplateBydocumentTemplateId(eq(documentTemplateId))).thenReturn(false);

        String result = documentTemplateService.deleteDocumentTemplate(documentTemplateId, mockUser, 1L);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.DELETE.getAction()), anyMap(), eq(mockUser.getId()), eq(documentTemplateId), eq(partnerId));
    }

    @Test
    void deleteDocumentTemplate_VersionMismatch() {
        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(eq(documentTemplateId))).thenReturn(sampleDocumentTemplate);

        String result = documentTemplateService.deleteDocumentTemplate(documentTemplateId, mockUser, 999L);

        assertEquals("error.DataChangedReload", result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteDocumentTemplate_ExistingDocTemplate() {
        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(eq(documentTemplateId))).thenReturn(sampleDocumentTemplate);
        when(documentTemplateDocumentTypesRepository.existsDocumentTemplateBydocumentTemplateId(eq(documentTemplateId))).thenReturn(true);

        String result = documentTemplateService.deleteDocumentTemplate(documentTemplateId, mockUser, 1L);

        assertEquals("error.HasDocumentType", result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteMultiDocumentTemplate_Success() {
        UUID id2 = UUID.randomUUID();
        DocumentTemplate documentTemplate2 = new DocumentTemplate();
        documentTemplate2.setId(id2);
        documentTemplate2.setDocumentTemplateName("DocumentTemplate 2");
        documentTemplate2.setDocumentTemplateCode("TEST002");
        documentTemplate2.setVersion(1L);
        documentTemplate2.setIsDeleted(Constants.isDeleted.ACTIVE);
        documentTemplate2.setPartnerId(partnerId);

        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(documentTemplateId,"name","code", 1L),
                new DeleteMultiDTO(id2,"name","code", 1L)
        );

        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(eq(documentTemplateId))).thenReturn(sampleDocumentTemplate);
        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(eq(id2))).thenReturn(documentTemplate2);
        when(documentTemplateDocumentTypesRepository.existsDocumentTemplateBydocumentTemplateId(eq(documentTemplateId))).thenReturn(false);
        Mockito.doNothing().when(documentTemplateRepository).deleteDocumentTemplateByDocumentTemplateId(any(UUID.class));

        String result = documentTemplateService.deleteMultiDocumentTemplate(ids, mockUser);

        assertEquals("", result);
        verify(logService, times(2)).createLog(anyString(), anyMap(), any(), any(), any());
    }

    @Test
    void deleteMultiDocumentTemplate_OneVersionMismatch() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(documentTemplateId,"name","code", 999L)
        );

        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(eq(documentTemplateId))).thenReturn(sampleDocumentTemplate);

        String result = documentTemplateService.deleteMultiDocumentTemplate(ids, mockUser);

        assertEquals("error.DataChangedReload", result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteMultiDocumentTemplate_OneExistingDocTemplate() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(documentTemplateId,"name","code", 1L)
        );
        when(documentTemplateDocumentTypesRepository.existsDocumentTemplateBydocumentTemplateId(eq(documentTemplateId))).thenReturn(true);


        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(eq(documentTemplateId))).thenReturn(sampleDocumentTemplate);

        String result = documentTemplateService.deleteMultiDocumentTemplate(ids, mockUser);

        assertEquals("error.HasDocumentType", result);
        verifyNoInteractions(logService);
    }

    @Test
    void lockUnlockDocumentTemplate_LockSuccess() {
        sampleDocumentTemplate.setIsActive(true);
        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(eq(documentTemplateId))).thenReturn(sampleDocumentTemplate);
        when(documentTemplateRepository.save(any(DocumentTemplate.class))).thenReturn(sampleDocumentTemplate);

        String result = documentTemplateService.lockUnlockDocumentTemplate(documentTemplateId, mockUser, 1L);

        assertEquals("", result);
        assertFalse(sampleDocumentTemplate.getIsActive());
        verify(logService).createLog(eq(ActionType.LOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockDocumentTemplate_UnlockSuccess() {
        sampleDocumentTemplate.setIsActive(false);
        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(eq(documentTemplateId))).thenReturn(sampleDocumentTemplate);
        when(documentTemplateRepository.save(any())).thenReturn(sampleDocumentTemplate);

        documentTemplateService.lockUnlockDocumentTemplate(documentTemplateId, mockUser, 1L);

        assertTrue(sampleDocumentTemplate.getIsActive());
        verify(logService).createLog(eq(ActionType.UNLOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockDocumentTemplate_Error() {
        sampleDocumentTemplate.setIsActive(false);
        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(eq(documentTemplateId))).thenReturn(null);

        String result = documentTemplateService.lockUnlockDocumentTemplate(documentTemplateId, mockUser, 1L);

        assertEquals("error.DataChangedReload", result);
    }

    @Test
    void saveDocumentTemplate_Success() {
        UUID attachFileId = UUID.randomUUID();

        // Thay thế @Builder cho DocumentTemplateCreateDTO
        DocumentTemplateCreateDTO createDTO = new DocumentTemplateCreateDTO();
        createDTO.setDocumentTemplateCode("NEW_CODE");
        createDTO.setDocumentTemplateName("New Template");
        createDTO.setAttachFileId(attachFileId);
        createDTO.setDocumentTypeIds(List.of(UUID.randomUUID()));
        createDTO.setAllowedEditors(List.of(UUID.randomUUID()));
        createDTO.setAllowedViewers(List.of(UUID.randomUUID()));
        createDTO.setIsActive(true);

        when(documentTemplateRepository.findByCode(any(), any())).thenReturn(null);
        when(documentTemplateRepository.save(any(DocumentTemplate.class))).thenReturn(mockTemplate);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        // Act
        String result = documentTemplateService.saveDocumentTemplate(createDTO, authentication);

        // Assert
        assertEquals("", result);
        verify(documentTemplateRepository, times(1)).save(any(DocumentTemplate.class));
        verify(documentTemplateDocumentTypesRepository, times(1)).saveAll(anyList());
        verify(logService).createLog(eq(ActionType.CREATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void saveDocumentTemplate_CodeExists() {
        // Thay thế @Builder cho DocumentTemplateCreateDTO
        DocumentTemplateCreateDTO createDTO = new DocumentTemplateCreateDTO();
        createDTO.setDocumentTemplateCode("EXISTING_CODE");

        when(documentTemplateRepository.findByCode(any(),any())).thenReturn(mockTemplate);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        // Act
        String result = documentTemplateService.saveDocumentTemplate(createDTO, authentication);

        // Assert
        assertEquals("error.DocumentTemplateExists", result);
        verify(documentTemplateRepository, never()).save(any(DocumentTemplate.class));
        verify(logService, never()).createLog(any(), any(), any(), any(), any());
    }

    @Test
    void updateDocumentTemplate_Success() {
        UUID newDocTypeId = UUID.randomUUID();
        UUID newEditorId = UUID.randomUUID();

        DocumentTemplateCreateDTO updateDTO = new DocumentTemplateCreateDTO();
        updateDTO.setId(testDocTemplateId);
        updateDTO.setVersion(testVersion);
        updateDTO.setDocumentTemplateCode("UPDATED_CODE");
        updateDTO.setDocumentTemplateName("Updated Name");
        updateDTO.setAttachFileId(attachFileId);
        updateDTO.setDocumentTypeIds(List.of(newDocTypeId));
        updateDTO.setAllowedEditors(List.of(newEditorId));
        updateDTO.setAllowedViewers(List.of(newEditorId));
        updateDTO.setIsActive(false);

        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(eq(testDocTemplateId))).thenReturn(mockTemplate);
        when(documentTemplateRepository.save(any(DocumentTemplate.class))).thenReturn(mockTemplate);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        String result = documentTemplateService.updateDocumentTemplate(updateDTO, authentication);

        assertEquals("", result);

        verify(logService).createLog(eq(ActionType.UPDATE.getAction()), anyMap(), any(), any(), any());


    }

    @Test
    void updateDocumentTemplate_DataChangedReload() {
        DocumentTemplateCreateDTO updateDTO = new DocumentTemplateCreateDTO();
        updateDTO.setId(testDocTemplateId);
        updateDTO.setVersion(testVersion + 1);

        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(testDocTemplateId)).thenReturn(mockTemplate);
        when(authentication.getPrincipal()).thenReturn(mockUser);

        String result = documentTemplateService.updateDocumentTemplate(updateDTO, authentication);

        assertEquals("error.DataChangedReload", result);
        verify(documentTemplateRepository, never()).save(any(DocumentTemplate.class));
        verify(logService, never()).createLog(any(), any(), any(), any(), any());
    }

    @Test
    void getListDocumentTemplate_SuccessWithAndWithoutAttachment() {
        // Arrange
        String searchString = "search";
        List<DocumentTemplate> content = Collections.singletonList(mockTemplate);
        Page<DocumentTemplate> mockPage = new PageImpl<>(content, PageRequest.of(0, 10), content.size());
        List<UUID> docTypeIds = List.of(UUID.randomUUID());
        when(documentTemplateRepository.getDocumentTemplateWithPagination(
                eq(searchString), any(), any(), any(), eq(testPartnerId), eq(PageRequest.of(0, 10))
        )).thenReturn(mockPage);
        when(mapper.map(mockTemplate, DocumentTemplateDTO.class)).thenReturn(mockDto);
        when(documentFileRepository.findById(attachFileId)).thenReturn(Optional.of(mockFile));
        when(documentTemplateDocumentTypesRepository.getDocumentTypeIdByDocumentTemplateId(documentTemplateId)).thenReturn(docTypeIds);

        Page<DocumentTemplateDTO> result = documentTemplateService.getListDocumentTemplate(
                searchString, null, null, null, PageRequest.of(0, 10), testPartnerId
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        List<DocumentTemplateDTO> dtoList = result.getContent();

        DocumentTemplateDTO dto1Result = dtoList.stream().filter(d -> d.getId().equals(documentTemplateId)).findFirst().orElse(null);
        assertNotNull(dto1Result);
        assertEquals(mockFile, dto1Result.getAttachFile());
        assertEquals(docTypeIds, dto1Result.getDocumentTypeIds());

        verify(documentTemplateRepository, times(1)).getDocumentTemplateWithPagination(any(), any(), any(), any(), any(), any());
        verify(documentFileRepository, times(1)).findById(attachFileId);
    }

    @Test
    void getListDocumentTemplate_NoResults() {
        Page<DocumentTemplate> mockEmptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(documentTemplateRepository.getDocumentTemplateWithPagination(any(), any(), any(), any(), any(), any()))
                .thenReturn(mockEmptyPage);

        Page<DocumentTemplateDTO> result = documentTemplateService.getListDocumentTemplate(
                null, null, null, null, PageRequest.of(0, 10), testPartnerId
        );

        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(documentFileRepository, never()).findById(any(UUID.class));
        verify(documentTemplateDocumentTypesRepository, never()).getDocumentTypeIdByDocumentTemplateId(any(UUID.class));
    }

    @Test
    void getListDocumentTemplate_FileThrowsException() {
        List<DocumentTemplate> content = Collections.singletonList(mockTemplate);
        Page<DocumentTemplate> mockPage = new PageImpl<>(content, PageRequest.of(0, 10), 1);
        List<UUID> docTypeIds = List.of(documentTemplateId);

        when(documentTemplateRepository.getDocumentTemplateWithPagination(any(), any(), any(), any(), any(), any()))
                .thenReturn(mockPage);
        when(mapper.map(mockTemplate, DocumentTemplateDTO.class)).thenReturn(mockDto);

        when(documentFileRepository.findById(attachFileId)).thenThrow(new RuntimeException(""));

        Page<DocumentTemplateDTO> result = documentTemplateService.getListDocumentTemplate(
                null, null, null, null, PageRequest.of(0, 10), testPartnerId
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        DocumentTemplateDTO dtoResult = result.getContent().get(0);

        assertNull(dtoResult.getAttachFile());
        assertEquals(docTypeIds, dtoResult.getDocumentTypeIds());
    }

    @Test
    void getAllDocumentTemplate_ReturnsList() {
        when(documentTemplateRepository.getAllDocumentTemplate(eq(partnerId))).thenReturn(List.of(sampleDocumentTemplate));

        List<DocumentTemplate> result = documentTemplateService.getAllDocumentTemplate(partnerId);

        assertEquals(1, result.size());
    }

    @Test
    void checkDeleteMulti_HasError() {
        sampleDocumentTemplate.setVersion(999L); // version không khớp
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(documentTemplateId,"name","code", 1L));

        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(eq(documentTemplateId))).thenReturn(null);

        ErrorListResponse result = documentTemplateService.checkDeleteMulti(ids);

        assertTrue(result.getHasError());
        assertEquals("error.DataChangedReload", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void checkDeleteMulti_NoError_ReturnsNull() {
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(documentTemplateId,"name","code", 1L));

        when(documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(eq(documentTemplateId))).thenReturn(sampleDocumentTemplate);

        ErrorListResponse result = documentTemplateService.checkDeleteMulti(ids);

        assertNull(result);
    }

    @Test
    void getAllowspermission_ReturnsCorrectMap() {
        UUID docFileId = UUID.randomUUID();
        UUID editorId = UUID.randomUUID();
        UUID viewerId = UUID.randomUUID();

        DocumentAllowedEditors editor = new DocumentAllowedEditors();
        editor.setId(UUID.randomUUID());
        editor.setDocumentId(docFileId);
        editor.setEditorId(editorId);

        DocumentAllowedViewers viewer = new DocumentAllowedViewers();
        viewer.setId(UUID.randomUUID());
        viewer.setDocumentId(docFileId);
        viewer.setViewerId(viewerId);

        List<DocumentAllowedEditors> mockEditors = List.of(editor);
        List<DocumentAllowedViewers> mockViewers = List.of(viewer);

        when(documentAllowedEditorsRepository.findByDocumentId(docFileId)).thenReturn(mockEditors);
        when(documentAllowedViewersRepository.findByDocumentId(docFileId)).thenReturn(mockViewers);

        Map<String, Object> result = documentTemplateService.getAllowspermission(docFileId);

        assertNotNull(result);
        assertTrue(result.containsKey("Editors"));
        assertTrue(result.containsKey("Viewers"));

        assertEquals(mockEditors, result.get("Editors"));
        assertEquals(mockViewers, result.get("Viewers"));
        assertEquals(1, ((List<?>) result.get("Editors")).size());
        assertEquals(1, ((List<?>) result.get("Viewers")).size());

        verify(documentAllowedEditorsRepository, times(1)).findByDocumentId(docFileId);
        verify(documentAllowedViewersRepository, times(1)).findByDocumentId(docFileId);
    }

    @Test
    void getAllowspermission_ReturnsEmptyMapForNoPermissions() {
        UUID docFileId = UUID.randomUUID();

        when(documentAllowedEditorsRepository.findByDocumentId(docFileId)).thenReturn(Collections.emptyList());
        when(documentAllowedViewersRepository.findByDocumentId(docFileId)).thenReturn(Collections.emptyList());

        Map<String, Object> result = documentTemplateService.getAllowspermission(docFileId);

        assertNotNull(result);
        assertTrue(result.containsKey("Editors"));
        assertTrue(result.containsKey("Viewers"));

        assertTrue(((List<?>) result.get("Editors")).isEmpty());
        assertTrue(((List<?>) result.get("Viewers")).isEmpty());
    }

    @Test
    void getDocumentDetail_Success() {

        DocumentTemplateDetailDTO createDTO = new DocumentTemplateDetailDTO();
        createDTO.setDocumentTemplateCode(sampleDocumentTemplate.getDocumentTemplateCode());
        createDTO.setDocumentTemplateName(sampleDocumentTemplate.getDocumentTemplateName());
        createDTO.setAttachFileId(sampleDocumentTemplate.getAttachFileId());
        createDTO.setId(sampleDocumentTemplate.getId());
        createDTO.setVersion(sampleDocumentTemplate.getVersion());
        createDTO.setIsActive(sampleDocumentTemplate.getIsActive());
        createDTO.setDocumentTemplateDescription(sampleDocumentTemplate.getDocumentTemplateDescription());

        createDTO.setIsActive(true);

        when(documentTemplateRepository.findById(eq(testDocTemplateId))).thenReturn(sampleDocumentTemplate);
        when(documentFileRepository.findById(any())).thenReturn(Optional.ofNullable(mockFile));
        when(jwtService.generateWopiToken(any(),any(),any(),any())).thenReturn("wopi");
        when(formSchemaRepository.getFormSchemaByTemplateID(any())).thenReturn(new FormSchema());

        DocumentTemplateDetailDTO result = documentTemplateService.getDocumentDetail(testDocTemplateId, mockUser);

        assertEquals(createDTO.getId(), result.getId());
        assertEquals(createDTO.getDocumentTemplateName(), result.getDocumentTemplateName());
        assertEquals(createDTO.getDocumentTemplateCode(), result.getDocumentTemplateCode());
        verify(logService).createLog(eq(ActionType.VIEW.getAction()), anyMap(), any(), any(), any());
    }

}