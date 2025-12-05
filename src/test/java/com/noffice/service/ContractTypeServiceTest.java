package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.ContractType;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.ContractTypeRepository;
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
class ContractTypeServiceTest {
    @Mock
    private ContractTypeRepository contractTypeRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private ContractTypeService contractTypeService;

    private User mockUser;
    private ContractType sampleContractType;
    private UUID contractTypeId;
    private UUID partnerId;

    @BeforeEach
    void setUp() {
        partnerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        contractTypeId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setFullName("Nguyễn Văn Test");
        mockUser.setPartnerId(partnerId);

        sampleContractType = new ContractType();
        sampleContractType.setId(contractTypeId);
        sampleContractType.setContractTypeName("Test ContractType");
        sampleContractType.setContractTypeCode("TEST001");
        sampleContractType.setIsActive(true);
        sampleContractType.setIsDeleted(Constants.isDeleted.ACTIVE);
        sampleContractType.setVersion(1L);
        sampleContractType.setPartnerId(partnerId);
    }

    @Test
    void deleteContractType_Success() {
        when(contractTypeRepository.findByContractTypeIdIncludeDeleted(eq(contractTypeId))).thenReturn(sampleContractType);
        Mockito.doNothing().when(contractTypeRepository).deleteContractTypeByContractTypeId(any(UUID.class));

        String result = contractTypeService.deleteContractType(contractTypeId, mockUser, 1L);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.DELETE.getAction()), anyMap(), eq(mockUser.getId()), eq(contractTypeId), eq(partnerId));
    }

    @Test
    void deleteContractType_VersionMismatch() {
        when(contractTypeRepository.findByContractTypeIdIncludeDeleted(eq(contractTypeId))).thenReturn(sampleContractType);

        String result = contractTypeService.deleteContractType(contractTypeId, mockUser, 999L);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteMultiContractType_Success() {
        UUID id2 = UUID.randomUUID();
        ContractType contractType2 = new ContractType();
        contractType2.setId(id2);
        contractType2.setContractTypeName("ContractType 2");
        contractType2.setContractTypeCode("TEST002");
        contractType2.setVersion(1L);
        contractType2.setIsDeleted(Constants.isDeleted.ACTIVE);
        contractType2.setPartnerId(partnerId);

        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(contractTypeId,"name","code", 1L),
                new DeleteMultiDTO(id2,"name","code", 1L)
        );

        when(contractTypeRepository.findByContractTypeIdIncludeDeleted(eq(contractTypeId))).thenReturn(sampleContractType);
        when(contractTypeRepository.findByContractTypeIdIncludeDeleted(eq(id2))).thenReturn(contractType2);
        Mockito.doNothing().when(contractTypeRepository).deleteContractTypeByContractTypeId(any(UUID.class));

        String result = contractTypeService.deleteMultiContractType(ids, mockUser);

        assertEquals("", result);
        verify(logService, times(2)).createLog(anyString(), anyMap(), any(), any(), any());
    }

    @Test
    void deleteMultiContractType_OneVersionMismatch() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(contractTypeId,"name","code", 999L)
        );

        when(contractTypeRepository.findByContractTypeIdIncludeDeleted(eq(contractTypeId))).thenReturn(sampleContractType);

        String result = contractTypeService.deleteMultiContractType(ids, mockUser);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
        verifyNoInteractions(logService);
    }

    @Test
    void lockUnlockContractType_LockSuccess() {
        sampleContractType.setIsActive(true);
        when(contractTypeRepository.findByContractTypeIdIncludeDeleted(eq(contractTypeId))).thenReturn(sampleContractType);
        when(contractTypeRepository.save(any(ContractType.class))).thenReturn(sampleContractType);

        String result = contractTypeService.lockUnlockContractType(contractTypeId, mockUser, 1L);

        assertEquals("", result);
        assertFalse(sampleContractType.getIsActive());
        verify(logService).createLog(eq(ActionType.LOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockContractType_UnlockSuccess() {
        sampleContractType.setIsActive(false);
        when(contractTypeRepository.findByContractTypeIdIncludeDeleted(eq(contractTypeId))).thenReturn(sampleContractType);
        when(contractTypeRepository.save(any())).thenReturn(sampleContractType);

        contractTypeService.lockUnlockContractType(contractTypeId, mockUser, 1L);

        assertTrue(sampleContractType.getIsActive());
        verify(logService).createLog(eq(ActionType.UNLOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockContractType_Error() {
        sampleContractType.setIsActive(false);
        when(contractTypeRepository.findByContractTypeIdIncludeDeleted(eq(contractTypeId))).thenReturn(null);

        String result = contractTypeService.lockUnlockContractType(contractTypeId, mockUser, 1L);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
    }

    @Test
    void saveContractType_CreateSuccess() {
        ContractType contractTypeDTO = new ContractType();
        contractTypeDTO.setContractTypeName("New ContractType");
        contractTypeDTO.setContractTypeCode("NEW001");
        contractTypeDTO.setIsActive(true);

        when(contractTypeRepository.findByCode(eq("NEW001"), eq(partnerId))).thenReturn(null);
        when(contractTypeRepository.save(any(ContractType.class))).thenAnswer(i -> i.getArgument(0));

        String result = contractTypeService.saveContractType(contractTypeDTO, mockUser);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.CREATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void saveContractType_CodeExists() {
        ContractType contractTypeDTO = new ContractType();
        contractTypeDTO.setContractTypeCode("TEST001");

        when(contractTypeRepository.findByCode(eq("TEST001"), eq(partnerId))).thenReturn(sampleContractType);

        String result = contractTypeService.saveContractType(contractTypeDTO, mockUser);

        assertEquals("error.ContractTypeExists", result);
        verifyNoInteractions(logService);
    }

    @Test
    void updateContractType_Success() {
        ContractType contractTypeDTO = new ContractType();
        contractTypeDTO.setId(contractTypeId);
        contractTypeDTO.setContractTypeName("Updated Name");
        contractTypeDTO.setContractTypeCode("UPDATED");
        contractTypeDTO.setVersion(1L);
        contractTypeDTO.setIsActive(false);

        when(contractTypeRepository.findByContractTypeIdIncludeDeleted(eq(contractTypeId))).thenReturn(sampleContractType);
        when(contractTypeRepository.save(any(ContractType.class))).thenReturn(sampleContractType);

        String result = contractTypeService.updateContractType(contractTypeDTO, mockUser);

        assertEquals("", result);
        assertEquals("Updated Name", sampleContractType.getContractTypeName());
        verify(logService).createLog(eq(ActionType.UPDATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void updateContractType_Error() {
        ContractType contractTypeDTO = new ContractType();
        contractTypeDTO.setId(contractTypeId);

        when(contractTypeRepository.findByContractTypeIdIncludeDeleted(eq(contractTypeId))).thenReturn(null);

        String result = contractTypeService.updateContractType(contractTypeDTO, mockUser);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
    }

    @Test
    void getListContractType_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ContractType> page = new PageImpl<>(List.of(sampleContractType));

        when(contractTypeRepository.getContractTypeWithPagination(any(), any(), any(), any(), eq(partnerId), eq(pageable)))
                .thenReturn(page);

        Page<ContractType> result = contractTypeService.getListContractType("test", "code", "name", "desc", pageable, partnerId);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test ContractType", result.getContent().get(0).getContractTypeName());
    }

    @Test
    void getAllContractType_ReturnsList() {
        when(contractTypeRepository.getAllContractType(eq(partnerId))).thenReturn(List.of(sampleContractType));

        List<ContractType> result = contractTypeService.getAllContractType(partnerId);

        assertEquals(1, result.size());
    }

    @Test
    void getLogDetailContractType_LogsView() {
        when(contractTypeRepository.findByContractTypeCode(eq("TEST001"))).thenReturn(sampleContractType);

        contractTypeService.getLogDetailContractType("TEST001", mockUser);

        verify(logService).createLog(eq(ActionType.VIEW.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void checkDeleteMulti_HasError() {
        sampleContractType.setVersion(999L); // version không khớp
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(contractTypeId,"name","code", 1L));

        when(contractTypeRepository.findByContractTypeIdIncludeDeleted(eq(contractTypeId))).thenReturn(null);

        ErrorListResponse result = contractTypeService.checkDeleteMulti(ids);

        assertTrue(result.getHasError());
        assertEquals(Constants.errorResponse.DATA_CHANGED, result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void checkDeleteMulti_NoError_ReturnsNull() {
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(contractTypeId,"name","code", 1L));

        when(contractTypeRepository.findByContractTypeIdIncludeDeleted(eq(contractTypeId))).thenReturn(sampleContractType);

        ErrorListResponse result = contractTypeService.checkDeleteMulti(ids);

        assertNull(result);
    }
}
