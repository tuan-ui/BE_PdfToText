package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.DocDocumentDTO;
import com.noffice.dto.NodeDeptUserDTO;
import com.noffice.entity.*;
import com.noffice.enumtype.ActionType;
import com.noffice.ultils.Constants;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.AttachRepository;
import com.noffice.repository.DocDocumentRepository;
import com.noffice.repository.NodeDeptUserRepository;
import com.noffice.ultils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocDocumentServiceTest {

    @Mock
    private DocDocumentRepository docDocumentRepository;

    @Mock
    private NodeDeptUserRepository nodeDeptUserRepository;

    @Mock
    private LogService logService;

    @Mock
    private AttachRepository attachRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DocDocumentService docDocumentService;

    private User user;
    private DocDocument docDocument;
    private UUID docId;
    private DocDocumentDTO docDocumentDTO;
    private NodeDeptUser nodeDeptUser;
    private Attachs attachs;
    private NodeDeptUserDTO nodeDeptUserDTO;

    @BeforeEach
    void setUp() {
        docId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setFullName("Nguyen Van A");
        user.setPartnerId(UUID.randomUUID());

        docDocument = new DocDocument();
        docDocument.setId(docId);
        docDocument.setDocumentTitle("Test Document");
        docDocument.setDocTemplateId(docId);
        docDocument.setDocTypeId(docId);
        docDocument.setDeptName("dept name");
        docDocument.setPurpose("purpose");
        docDocument.setFormData("form data");
        docDocument.setVersion(1L);
        docDocument.setIsDeleted(Constants.isDeleted.ACTIVE);
        docDocument.setIsActive(true);
        docDocument.setPartnerId(user.getPartnerId());

        nodeDeptUserDTO = new NodeDeptUserDTO();
        nodeDeptUserDTO.setStep("step");
        nodeDeptUserDTO.setUserId(userId);
        nodeDeptUserDTO.setDeptName("dept name");
        nodeDeptUserDTO.setRoleId(userId);
        nodeDeptUserDTO.setApprovalType("approval");
        nodeDeptUserDTO.setNote("note");

        nodeDeptUser  = new NodeDeptUser();
        nodeDeptUser.setId(userId);
        nodeDeptUser.setStep("step");
        nodeDeptUser.setUserId(userId);
        nodeDeptUser.setDeptName("dept name");
        nodeDeptUser.setRoleId(userId);
        nodeDeptUser.setApproveType("approval");
        nodeDeptUser.setNote("note");
        nodeDeptUser.setVersion(1L);

        UUID[] uuidArray = new UUID[] {
                docId
        };

        docDocumentDTO = new DocDocumentDTO();
        docDocumentDTO.setId(docId);
        docDocumentDTO.setDocumentTitle("Updated Document");
        docDocumentDTO.setDocTemplateId(docId);
        docDocumentDTO.setDocTypeId(docId);
        docDocumentDTO.setDeptName("dept name");
        docDocumentDTO.setPurpose("purpose");
        docDocumentDTO.setFormData("form data");
        docDocumentDTO.setIsActive(false);
        docDocumentDTO.setRemovedFiles(uuidArray);

        attachs = new Attachs();
        attachs.setId(userId);
    }

    @Test
    void delete_Success() {
        when(docDocumentRepository.findByDocumentId(docId)).thenReturn(docDocument);
        when(docDocumentRepository.save(any(DocDocument.class))).thenReturn(docDocument);

        Boolean result = docDocumentService.delete(docId, user);

        assertTrue(result);
        assertEquals(Constants.isDeleted.DELETED, docDocument.getIsDeleted());
        assertEquals(user.getId(), docDocument.getDeletedBy());
        verify(logService).createLog(anyString(), anyMap(), eq(user.getId()), eq(docId), eq(user.getPartnerId()));
    }

    @Test
    void delete_Failure_Exception() {
        when(docDocumentRepository.findByDocumentId(docId)).thenThrow(new RuntimeException("DB error"));

        Boolean result = docDocumentService.delete(docId, user);

        assertFalse(result);
    }

    @Test
    void deleteMulti_Success() {
        DeleteMultiDTO dto = new DeleteMultiDTO();
        dto.setId(docId);
        dto.setVersion(1L);

        when(docDocumentRepository.findByDocumentId(docId)).thenReturn(docDocument);
        when(docDocumentRepository.save(any())).thenReturn(docDocument);

        String result = docDocumentService.deleteMulti(List.of(dto), user);

        assertEquals("", result);
        verify(docDocumentRepository).save(docDocument);
        verify(logService).createLog(any(), any(), any(), any(), any());
    }

    @Test
    void deleteMulti_VersionMismatch() {
        DeleteMultiDTO dto = new DeleteMultiDTO();
        dto.setId(docId);
        dto.setVersion(999L);

        when(docDocumentRepository.findByDocumentId(docId)).thenReturn(docDocument);

        String result = docDocumentService.deleteMulti(List.of(dto), user);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
        verify(docDocumentRepository, never()).save(any());
    }

    @Test
    void deleteMulti_AlreadyDeleted() {
        docDocument.setIsDeleted(Constants.isDeleted.DELETED);
        DeleteMultiDTO dto = new DeleteMultiDTO();
        dto.setId(docId);
        dto.setVersion(1L);

        when(docDocumentRepository.findByDocumentId(docId)).thenReturn(docDocument);

        String result = docDocumentService.deleteMulti(List.of(dto), user);

        assertEquals("error.DocTypeNotExists", result);
    }

    @Test
    void lockUnlock_Success_Lock() {
        when(docDocumentRepository.findByDocumentId(docId)).thenReturn(docDocument);
        when(docDocumentRepository.save(any())).thenReturn(docDocument);

        String result = docDocumentService.lockUnlock(docId, user, 1L);

        assertEquals("", result);
        assertFalse(docDocument.getIsActive());
        verify(logService).createLog(eq(ActionType.LOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlock_Success_Unlock() {
        docDocument.setIsActive(false);
        when(docDocumentRepository.findByDocumentId(docId)).thenReturn(docDocument);
        when(docDocumentRepository.save(any())).thenReturn(docDocument);
        docDocumentService.lockUnlock(docId, user, 1L);

        assertTrue(docDocument.getIsActive());
        verify(logService).createLog(eq(ActionType.UNLOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlock_Success_DataChangedReload() {
        when(docDocumentRepository.findByDocumentId(any())).thenReturn(docDocument);
        String result = docDocumentService.lockUnlock(docId, user, 2L);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
    }

    @Test
    void lockUnlock_Success_DocTypeNotExists() {
        docDocument.setIsDeleted(true);
        when(docDocumentRepository.findByDocumentId(any())).thenReturn(docDocument);
        String result = docDocumentService.lockUnlock(docId, user, 1L);

        assertEquals("error.DocTypeNotExists", result);
    }

    @Test
    void save_NewDocument_Success() {
        docDocumentDTO.setApprovalSteps(List.of(nodeDeptUserDTO));
        when(docDocumentRepository.findByDocumentId(any())).thenReturn(null);
        when(docDocumentRepository.save(any(DocDocument.class))).thenAnswer(i -> i.getArguments()[0]);
        when(nodeDeptUserRepository.save(any(NodeDeptUser.class))).thenAnswer(i -> i.getArguments()[0]);
        when(attachRepository.findById(docId)).thenReturn(Optional.ofNullable(attachs));
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        Attachs attach = new Attachs();
        attach.setId(UUID.randomUUID());

        try (MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class)) {
            fileUtils.when(() -> FileUtils.saveFile(any(), eq(user))).thenReturn(List.of(attach));

            DocDocument result = docDocumentService.save(docDocumentDTO, new MockMultipartFile[]{file}, user);

            assertNotNull(result);
            assertEquals(user.getId(), result.getCreateBy());
            verify(logService).createLog(eq(ActionType.CREATE.getAction()), anyMap(), any(), any(), any());
        }
    }

    @Test
    void save_UpdateDocument_Success() {
        nodeDeptUserDTO.setId(docId);
        docDocument.setCreateBy(user.getId());
        docDocumentDTO.setApprovalSteps(List.of(nodeDeptUserDTO));
        when(docDocumentRepository.findByDocumentId(any())).thenReturn(docDocument);
        when(docDocumentRepository.save(any(DocDocument.class))).thenAnswer(i -> i.getArguments()[0]);
        when(nodeDeptUserRepository.findById(any())).thenReturn(Optional.ofNullable(nodeDeptUser));
        when(nodeDeptUserRepository.save(any(NodeDeptUser.class))).thenAnswer(i -> i.getArguments()[0]);
        when(attachRepository.findById(docId)).thenReturn(Optional.ofNullable(attachs));
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "content".getBytes());
        Attachs attach = new Attachs();
        attach.setId(UUID.randomUUID());

        try (MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class)) {
            fileUtils.when(() -> FileUtils.saveFile(any(), eq(user))).thenReturn(List.of(attach));

            DocDocument result = docDocumentService.save(docDocumentDTO, new MockMultipartFile[]{file}, user);

            assertNotNull(result);
            assertEquals(user.getId(), result.getCreateBy());
            verify(logService).createLog(eq(ActionType.CREATE.getAction()), anyMap(), any(), any(), any());
        }
    }

    @Test
    void update_Success() {
        when(authentication.getPrincipal()).thenReturn(user);
        when(docDocumentRepository.findByDocumentId(docId)).thenReturn(docDocument);
        when(docDocumentRepository.save(any())).thenReturn(docDocument);

        String result = docDocumentService.update(docDocument, authentication);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.UPDATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void update_VersionMismatch() {
        DocDocument docDocumentRequest =  new DocDocument();
        docDocumentRequest.setId(docId);
        docDocumentRequest.setVersion(2L);
        when(authentication.getPrincipal()).thenReturn(user);
        when(docDocumentRepository.findByDocumentId(docId)).thenReturn(docDocument);
        String result = docDocumentService.update(docDocumentRequest, authentication);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
        verify(docDocumentRepository, never()).save(any());
    }

    @Test
    void update_DocTypeNotExists() {
        docDocument.setIsDeleted(true);
        when(authentication.getPrincipal()).thenReturn(user);
        when(docDocumentRepository.findByDocumentId(docId)).thenReturn(docDocument);
        String result = docDocumentService.update(docDocument, authentication);

        assertEquals("error.DocTypeNotExists", result);
        verify(docDocumentRepository, never()).save(any());
    }

    @Test
    void getListDoc_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<DocDocumentDTO> page = new PageImpl<>(List.of(new DocDocumentDTO()), pageable, 1);

        when(docDocumentRepository.getDocWithPagination(any(), any(), any(), any(), any()))
                .thenReturn(page);

        Page<DocDocumentDTO> result = docDocumentService.getListDoc("test", "code", "name", "desc", pageable, user.getPartnerId());

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void checkDeleteMulti_NoError() {
        DeleteMultiDTO dto = new DeleteMultiDTO();
        dto.setId(docId);
        dto.setVersion(1L);

        when(docDocumentRepository.findByDocumentId(docId)).thenReturn(docDocument);

        ErrorListResponse response = docDocumentService.checkDeleteMulti(List.of(dto));

        assertNull(response); // Không lỗi → trả về null theo logic hiện tại
    }

    @Test
    void checkDeleteMulti_HasError() {
        DeleteMultiDTO dto = new DeleteMultiDTO();
        dto.setId(docId);
        dto.setVersion(999L);

        when(docDocumentRepository.findByDocumentId(docId)).thenReturn(docDocument);

        ErrorListResponse response = docDocumentService.checkDeleteMulti(List.of(dto));

        assertNotNull(response);
        assertTrue(response.getHasError());
        assertEquals(Constants.errorResponse.DATA_CHANGED, response.getErrors().get(0).getErrorMessage());
    }

    @Test
    void checkDeleteMulti_DocTypeNotExists() {
        DeleteMultiDTO dto = new DeleteMultiDTO();
        dto.setId(docId);
        dto.setVersion(1L);

        docDocument.setIsDeleted(true);

        when(docDocumentRepository.findByDocumentId(docId)).thenReturn(docDocument);

        ErrorListResponse response = docDocumentService.checkDeleteMulti(List.of(dto));

        assertNotNull(response);
        assertTrue(response.getHasError());
        assertEquals("error.DocTypeNotExists", response.getErrors().get(0).getErrorMessage());
    }

    @Test
    void getAttachsByDocument() {
        when(attachRepository.getListAttachs(docId, Constants.OBJECT_TYPE.DOC_DOCUMENT))
                .thenReturn(List.of(new Attachs()));

        List<Attachs> result = docDocumentService.getAttachsByDocument(docId, Constants.OBJECT_TYPE.DOC_DOCUMENT);

        assertEquals(1, result.size());
    }

    @Test
    void getByDocId() {
        when(nodeDeptUserRepository.getByDocId(docId)).thenReturn(List.of(new NodeDeptUser()));

        List<NodeDeptUser> result = docDocumentService.getByDocId(docId);

        assertEquals(1, result.size());
    }

    @Test
    void getDocTypeByDocument() {
        when(docDocumentRepository.getAllDocType(docId))
                .thenReturn(List.of(new DocType()));

        List<DocType> result = docDocumentService.getAllDocType(docId);

        assertEquals(1, result.size());
    }
    @Test
    void getLogDetailDocType_LogsView() {
        DocType docType = new DocType();
        docType.setId(docId);
        docType.setDocTypeName("test");
        when(docDocumentRepository.findByDocTypeCode(eq("TEST001"))).thenReturn(docType);

        docDocumentService.getLogDetailDocType("TEST001", user);

        verify(logService).createLog(eq(ActionType.VIEW.getAction()), anyMap(), any(), any(), any());
    }

}