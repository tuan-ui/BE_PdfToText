package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.TaskType;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.TaskTypeRepository;
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
public class TaskTypeServiceTest {
    @Mock
    private TaskTypeRepository taskTypeRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private TaskTypeService taskTypeService;

    private User mockUser;
    private TaskType sampleTaskType;
    private UUID taskTypeId;
    private UUID partnerId;

    @BeforeEach
    void setUp() {
        partnerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        taskTypeId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setFullName("Nguyễn Văn Test");
        mockUser.setPartnerId(partnerId);

        sampleTaskType = new TaskType();
        sampleTaskType.setId(taskTypeId);
        sampleTaskType.setTaskTypeName("Test TaskType");
        sampleTaskType.setTaskTypeCode("TEST001");
        sampleTaskType.setIsActive(true);
        sampleTaskType.setIsDeleted(Constants.isDeleted.ACTIVE);
        sampleTaskType.setVersion(1L);
        sampleTaskType.setPartnerId(partnerId);
    }

    @Test
    void deleteTaskType_Success() {
        when(taskTypeRepository.findByTaskTypeIdIncludeDeleted(eq(taskTypeId))).thenReturn(sampleTaskType);
        Mockito.doNothing().when(taskTypeRepository).deleteTaskTypeByTaskTypeId(any(UUID.class));

        String result = taskTypeService.deleteTaskType(taskTypeId, mockUser, 1L);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.DELETE.getAction()), anyMap(), eq(mockUser.getId()), eq(taskTypeId), eq(partnerId));
    }

    @Test
    void deleteTaskType_VersionMismatch() {
        when(taskTypeRepository.findByTaskTypeIdIncludeDeleted(eq(taskTypeId))).thenReturn(sampleTaskType);

        String result = taskTypeService.deleteTaskType(taskTypeId, mockUser, 999L);

        assertEquals("error.DataChangedReload", result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteMultiTaskType_Success() {
        UUID id2 = UUID.randomUUID();
        TaskType taskType2 = new TaskType();
        taskType2.setId(id2);
        taskType2.setTaskTypeName("TaskType 2");
        taskType2.setTaskTypeCode("TEST002");
        taskType2.setVersion(1L);
        taskType2.setIsDeleted(Constants.isDeleted.ACTIVE);
        taskType2.setPartnerId(partnerId);

        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(taskTypeId,"name","code", 1L),
                new DeleteMultiDTO(id2,"name","code", 1L)
        );

        when(taskTypeRepository.findByTaskTypeIdIncludeDeleted(eq(taskTypeId))).thenReturn(sampleTaskType);
        when(taskTypeRepository.findByTaskTypeIdIncludeDeleted(eq(id2))).thenReturn(taskType2);
        Mockito.doNothing().when(taskTypeRepository).deleteTaskTypeByTaskTypeId(any(UUID.class));

        String result = taskTypeService.deleteMultiTaskType(ids, mockUser);

        assertEquals("", result);
        verify(logService, times(2)).createLog(anyString(), anyMap(), any(), any(), any());
    }

    @Test
    void deleteMultiTaskType_OneVersionMismatch() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(taskTypeId,"name","code", 999L)
        );

        when(taskTypeRepository.findByTaskTypeIdIncludeDeleted(eq(taskTypeId))).thenReturn(sampleTaskType);

        String result = taskTypeService.deleteMultiTaskType(ids, mockUser);

        assertEquals("error.DataChangedReload", result);
        verifyNoInteractions(logService);
    }

    @Test
    void lockUnlockTaskType_LockSuccess() {
        sampleTaskType.setIsActive(true);
        when(taskTypeRepository.findByTaskTypeIdIncludeDeleted(eq(taskTypeId))).thenReturn(sampleTaskType);
        when(taskTypeRepository.save(any(TaskType.class))).thenReturn(sampleTaskType);

        String result = taskTypeService.lockUnlockTaskType(taskTypeId, mockUser, 1L);

        assertEquals("", result);
        assertFalse(sampleTaskType.getIsActive());
        verify(logService).createLog(eq(ActionType.LOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockTaskType_UnlockSuccess() {
        sampleTaskType.setIsActive(false);
        when(taskTypeRepository.findByTaskTypeIdIncludeDeleted(eq(taskTypeId))).thenReturn(sampleTaskType);
        when(taskTypeRepository.save(any())).thenReturn(sampleTaskType);

        taskTypeService.lockUnlockTaskType(taskTypeId, mockUser, 1L);

        assertTrue(sampleTaskType.getIsActive());
        verify(logService).createLog(eq(ActionType.UNLOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockTaskType_Error() {
        sampleTaskType.setIsActive(false);
        when(taskTypeRepository.findByTaskTypeIdIncludeDeleted(eq(taskTypeId))).thenReturn(null);

        String result = taskTypeService.lockUnlockTaskType(taskTypeId, mockUser, 1L);

        assertEquals("error.DataChangedReload", result);
    }

    @Test
    void saveTaskType_CreateSuccess() {
        TaskType taskTypeDTO = new TaskType();
        taskTypeDTO.setTaskTypeName("New TaskType");
        taskTypeDTO.setTaskTypeCode("NEW001");
        taskTypeDTO.setIsActive(true);

        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(taskTypeRepository.findByCode(eq("NEW001"), eq(partnerId))).thenReturn(null);
        when(taskTypeRepository.save(any(TaskType.class))).thenAnswer(i -> i.getArgument(0));

        String result = taskTypeService.saveTaskType(taskTypeDTO, auth);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.CREATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void saveTaskType_CodeExists() {
        TaskType taskTypeDTO = new TaskType();
        taskTypeDTO.setTaskTypeCode("TEST001");

        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(taskTypeRepository.findByCode(eq("TEST001"), eq(partnerId))).thenReturn(sampleTaskType);

        String result = taskTypeService.saveTaskType(taskTypeDTO, auth);

        assertEquals("error.TaskTypeExists", result);
        verifyNoInteractions(logService);
    }

    @Test
    void updateTaskType_Success() {
        TaskType taskTypeDTO = new TaskType();
        taskTypeDTO.setId(taskTypeId);
        taskTypeDTO.setTaskTypeName("Updated Name");
        taskTypeDTO.setTaskTypeCode("UPDATED");
        taskTypeDTO.setVersion(1L);
        taskTypeDTO.setIsActive(false);

        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(taskTypeRepository.findByTaskTypeIdIncludeDeleted(eq(taskTypeId))).thenReturn(sampleTaskType);
        when(taskTypeRepository.save(any(TaskType.class))).thenReturn(sampleTaskType);

        String result = taskTypeService.updateTaskType(taskTypeDTO, auth);

        assertEquals("", result);
        assertEquals("Updated Name", sampleTaskType.getTaskTypeName());
        verify(logService).createLog(eq(ActionType.UPDATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void updateTaskType_Error() {
        TaskType taskTypeDTO = new TaskType();
        taskTypeDTO.setId(taskTypeId);
        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(taskTypeRepository.findByTaskTypeIdIncludeDeleted(eq(taskTypeId))).thenReturn(null);

        String result = taskTypeService.updateTaskType(taskTypeDTO, auth);

        assertEquals("error.DataChangedReload", result);
    }

    @Test
    void getListTaskType_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TaskType> page = new PageImpl<>(List.of(sampleTaskType));

        when(taskTypeRepository.getTaskTypeWithPagination(any(), any(), any(), any(), eq(partnerId), eq(pageable)))
                .thenReturn(page);

        Page<TaskType> result = taskTypeService.getListTaskType("test", "code", "name", "desc", pageable, partnerId);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test TaskType", result.getContent().get(0).getTaskTypeName());
    }

    @Test
    void getAllTaskType_ReturnsList() {
        when(taskTypeRepository.getAllTaskType(eq(partnerId))).thenReturn(List.of(sampleTaskType));

        List<TaskType> result = taskTypeService.getAllTaskType(partnerId);

        assertEquals(1, result.size());
    }

    @Test
    void getLogDetailTaskType_LogsView() {
        when(taskTypeRepository.findByTaskTypeCode(eq("TEST001"))).thenReturn(sampleTaskType);

        taskTypeService.getLogDetailTaskType("TEST001", mockUser);

        verify(logService).createLog(eq(ActionType.VIEW.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void checkDeleteMulti_HasError() {
        sampleTaskType.setVersion(999L); // version không khớp
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(taskTypeId,"name","code", 1L));

        when(taskTypeRepository.findByTaskTypeIdIncludeDeleted(eq(taskTypeId))).thenReturn(null);

        ErrorListResponse result = taskTypeService.checkDeleteMulti(ids);

        assertTrue(result.getHasError());
        assertEquals("error.DataChangedReload", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void checkDeleteMulti_NoError_ReturnsNull() {
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(taskTypeId,"name","code", 1L));

        when(taskTypeRepository.findByTaskTypeIdIncludeDeleted(eq(taskTypeId))).thenReturn(sampleTaskType);

        ErrorListResponse result = taskTypeService.checkDeleteMulti(ids);

        assertNull(result);
    }
}
