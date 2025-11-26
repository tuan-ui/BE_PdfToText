package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.DocType;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.DocTypeRepository;
import com.noffice.repository.DocumentTemplateDocumentTypesRepository;
import com.noffice.ultils.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DocTypeServiceTest {
    @Mock
    private DocTypeRepository docTypeRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private DocTypeService docTypeService;
    @Mock
    private DocumentTemplateDocumentTypesRepository documentTemplateDocumentTypesRepository;

    private User mockUser;
    private DocType sampleDocType;
    private UUID docTypeId;
    private UUID partnerId;

    @BeforeEach
    void setUp() {
        partnerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        docTypeId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setFullName("Nguyễn Văn Test");
        mockUser.setPartnerId(partnerId);

        sampleDocType = new DocType();
        sampleDocType.setId(docTypeId);
        sampleDocType.setDocTypeName("Test DocType");
        sampleDocType.setDocTypeCode("TEST001");
        sampleDocType.setIsActive(true);
        sampleDocType.setIsDeleted(Constants.isDeleted.ACTIVE);
        sampleDocType.setVersion(1L);
        sampleDocType.setPartnerId(partnerId);
    }

    @Test
    void deleteDocType_Success() {
        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(docTypeId))).thenReturn(sampleDocType);
        Mockito.doNothing().when(docTypeRepository).deleteDocTypeByDocTypeId(any(UUID.class));
        when(documentTemplateDocumentTypesRepository.existsDocumentTemplateByDocumentTypeId(eq(docTypeId))).thenReturn(false);

        String result = docTypeService.deleteDocType(docTypeId, mockUser, 1L);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.DELETE.getAction()), anyMap(), eq(mockUser.getId()), eq(docTypeId), eq(partnerId));
    }

    @Test
    void deleteDocType_VersionMismatch() {
        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(docTypeId))).thenReturn(sampleDocType);

        String result = docTypeService.deleteDocType(docTypeId, mockUser, 999L);

        assertEquals("error.DataChangedReload", result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteDocType_ExistingDocTemplate() {
        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(docTypeId))).thenReturn(sampleDocType);
        when(documentTemplateDocumentTypesRepository.existsDocumentTemplateByDocumentTypeId(eq(docTypeId))).thenReturn(true);

        String result = docTypeService.deleteDocType(docTypeId, mockUser, 1L);

        assertEquals("error.UnableToDeleteExistingDocTemplate", result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteMultiDocType_Success() {
        UUID id2 = UUID.randomUUID();
        DocType docType2 = new DocType();
        docType2.setId(id2);
        docType2.setDocTypeName("DocType 2");
        docType2.setDocTypeCode("TEST002");
        docType2.setVersion(1L);
        docType2.setIsDeleted(Constants.isDeleted.ACTIVE);
        docType2.setPartnerId(partnerId);

        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(docTypeId,"name","code", 1L),
                new DeleteMultiDTO(id2,"name","code", 1L)
        );

        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(docTypeId))).thenReturn(sampleDocType);
        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(id2))).thenReturn(docType2);
        when(documentTemplateDocumentTypesRepository.existsDocumentTemplateByDocumentTypeId(eq(docTypeId))).thenReturn(false);
        Mockito.doNothing().when(docTypeRepository).deleteDocTypeByDocTypeId(any(UUID.class));

        String result = docTypeService.deleteMultiDocType(ids, mockUser);

        assertEquals("", result);
        verify(logService, times(2)).createLog(anyString(), anyMap(), any(), any(), any());
    }

    @Test
    void deleteMultiDocType_OneVersionMismatch() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(docTypeId,"name","code", 999L)
        );

        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(docTypeId))).thenReturn(sampleDocType);

        String result = docTypeService.deleteMultiDocType(ids, mockUser);

        assertEquals("error.DataChangedReload", result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteMultiDocType_OneExistingDocTemplate() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(docTypeId,"name","code", 1L)
        );
        when(documentTemplateDocumentTypesRepository.existsDocumentTemplateByDocumentTypeId(eq(docTypeId))).thenReturn(true);


        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(docTypeId))).thenReturn(sampleDocType);

        String result = docTypeService.deleteMultiDocType(ids, mockUser);

        assertEquals("error.UnableToDeleteExistingDocTemplate", result);
        verifyNoInteractions(logService);
    }

    @Test
    void lockUnlockDocType_LockSuccess() {
        sampleDocType.setIsActive(true);
        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(docTypeId))).thenReturn(sampleDocType);
        when(docTypeRepository.save(any(DocType.class))).thenReturn(sampleDocType);

        String result = docTypeService.lockUnlockDocType(docTypeId, mockUser, 1L);

        assertEquals("", result);
        assertFalse(sampleDocType.getIsActive());
        verify(logService).createLog(eq(ActionType.LOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockDocType_UnlockSuccess() {
        sampleDocType.setIsActive(false);
        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(docTypeId))).thenReturn(sampleDocType);
        when(docTypeRepository.save(any())).thenReturn(sampleDocType);

        docTypeService.lockUnlockDocType(docTypeId, mockUser, 1L);

        assertTrue(sampleDocType.getIsActive());
        verify(logService).createLog(eq(ActionType.UNLOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockDocType_Error() {
        sampleDocType.setIsActive(false);
        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(docTypeId))).thenReturn(null);

        String result = docTypeService.lockUnlockDocType(docTypeId, mockUser, 1L);

        assertEquals("error.DataChangedReload", result);
    }

    @Test
    void saveDocType_CreateSuccess() {
        DocType docTypeDTO = new DocType();
        docTypeDTO.setDocTypeName("New DocType");
        docTypeDTO.setDocTypeCode("NEW001");
        docTypeDTO.setIsActive(true);

        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(docTypeRepository.findByCode(eq("NEW001"), eq(partnerId))).thenReturn(null);
        when(docTypeRepository.save(any(DocType.class))).thenAnswer(i -> i.getArgument(0));

        String result = docTypeService.saveDocType(docTypeDTO, auth);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.CREATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void saveDocType_CodeExists() {
        DocType docTypeDTO = new DocType();
        docTypeDTO.setDocTypeCode("TEST001");

        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(docTypeRepository.findByCode(eq("TEST001"), eq(partnerId))).thenReturn(sampleDocType);

        String result = docTypeService.saveDocType(docTypeDTO, auth);

        assertEquals("error.DocTypeExists", result);
        verifyNoInteractions(logService);
    }

    @Test
    void updateDocType_Success() {
        DocType docTypeDTO = new DocType();
        docTypeDTO.setId(docTypeId);
        docTypeDTO.setDocTypeName("Updated Name");
        docTypeDTO.setDocTypeCode("UPDATED");
        docTypeDTO.setVersion(1L);
        docTypeDTO.setIsActive(false);

        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(docTypeId))).thenReturn(sampleDocType);
        when(docTypeRepository.save(any(DocType.class))).thenReturn(sampleDocType);

        String result = docTypeService.updateDocType(docTypeDTO, auth);

        assertEquals("", result);
        assertEquals("Updated Name", sampleDocType.getDocTypeName());
        verify(logService).createLog(eq(ActionType.UPDATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void updateDocType_Error() {
        DocType docTypeDTO = new DocType();
        docTypeDTO.setId(docTypeId);
        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(docTypeId))).thenReturn(null);

        String result = docTypeService.updateDocType(docTypeDTO, auth);

        assertEquals("error.DataChangedReload", result);
    }

    @Test
    void getListDocType_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DocType> page = new PageImpl<>(List.of(sampleDocType));

        when(docTypeRepository.getDocTypeWithPagination(any(), any(), any(), any(), eq(partnerId), eq(pageable)))
                .thenReturn(page);

        Page<DocType> result = docTypeService.getListDocType("test", "code", "name", "desc", pageable, partnerId);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test DocType", result.getContent().get(0).getDocTypeName());
    }

    @Test
    void getAllDocType_ReturnsList() {
        when(docTypeRepository.getAllDocType(eq(partnerId))).thenReturn(List.of(sampleDocType));

        List<DocType> result = docTypeService.getAllDocType(partnerId);

        assertEquals(1, result.size());
    }

    @Test
    void getLogDetailDocType_LogsView() {
        when(docTypeRepository.findByDocTypeCode(eq("TEST001"))).thenReturn(sampleDocType);

        docTypeService.getLogDetailDocType("TEST001", mockUser);

        verify(logService).createLog(eq(ActionType.VIEW.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void checkDeleteMulti_HasError() {
        sampleDocType.setVersion(999L); // version không khớp
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(docTypeId,"name","code", 1L));

        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(docTypeId))).thenReturn(null);

        ErrorListResponse result = docTypeService.checkDeleteMulti(ids);

        assertTrue(result.getHasError());
        assertEquals("error.DataChangedReload", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void checkDeleteMulti_NoError_ReturnsNull() {
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(docTypeId,"name","code", 1L));

        when(docTypeRepository.findByDocTypeIdIncludeDeleted(eq(docTypeId))).thenReturn(sampleDocType);

        ErrorListResponse result = docTypeService.checkDeleteMulti(ids);

        assertNull(result);
    }
}
