package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.HolidayType;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.HolidayTypeRepository;
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
class HolidayTypeServiceTest {
    @Mock
    private HolidayTypeRepository holidayTypeRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private HolidayTypeService holidayTypeService;

    private User mockUser;
    private HolidayType sampleHolidayType;
    private UUID holidayTypeId;
    private UUID partnerId;

    @BeforeEach
    void setUp() {
        partnerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        holidayTypeId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setFullName("Nguyễn Văn Test");
        mockUser.setPartnerId(partnerId);

        sampleHolidayType = new HolidayType();
        sampleHolidayType.setId(holidayTypeId);
        sampleHolidayType.setHolidayTypeName("Test HolidayType");
        sampleHolidayType.setHolidayTypeCode("TEST001");
        sampleHolidayType.setIsActive(true);
        sampleHolidayType.setIsDeleted(Constants.isDeleted.ACTIVE);
        sampleHolidayType.setVersion(1L);
        sampleHolidayType.setPartnerId(partnerId);
    }

    @Test
    void deleteHolidayType_Success() {
        when(holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(eq(holidayTypeId))).thenReturn(sampleHolidayType);
        Mockito.doNothing().when(holidayTypeRepository).deleteHolidayTypeByHolidayTypeId(any(UUID.class));

        String result = holidayTypeService.deleteHolidayType(holidayTypeId, mockUser, 1L);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.DELETE.getAction()), anyMap(), eq(mockUser.getId()), eq(holidayTypeId), eq(partnerId));
    }

    @Test
    void deleteHolidayType_VersionMismatch() {
        when(holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(eq(holidayTypeId))).thenReturn(sampleHolidayType);

        String result = holidayTypeService.deleteHolidayType(holidayTypeId, mockUser, 999L);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteMultiHolidayType_Success() {
        UUID id2 = UUID.randomUUID();
        HolidayType holidayType2 = new HolidayType();
        holidayType2.setId(id2);
        holidayType2.setHolidayTypeName("HolidayType 2");
        holidayType2.setHolidayTypeCode("TEST002");
        holidayType2.setVersion(1L);
        holidayType2.setIsDeleted(Constants.isDeleted.ACTIVE);
        holidayType2.setPartnerId(partnerId);

        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(holidayTypeId,"name","code", 1L),
                new DeleteMultiDTO(id2,"name","code", 1L)
        );

        when(holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(eq(holidayTypeId))).thenReturn(sampleHolidayType);
        when(holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(eq(id2))).thenReturn(holidayType2);
        Mockito.doNothing().when(holidayTypeRepository).deleteHolidayTypeByHolidayTypeId(any(UUID.class));

        String result = holidayTypeService.deleteMultiHolidayType(ids, mockUser);

        assertEquals("", result);
        verify(logService, times(2)).createLog(anyString(), anyMap(), any(), any(), any());
    }

    @Test
    void deleteMultiHolidayType_OneVersionMismatch() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(holidayTypeId,"name","code", 999L)
        );

        when(holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(eq(holidayTypeId))).thenReturn(sampleHolidayType);

        String result = holidayTypeService.deleteMultiHolidayType(ids, mockUser);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
        verifyNoInteractions(logService);
    }

    @Test
    void lockUnlockHolidayType_LockSuccess() {
        sampleHolidayType.setIsActive(true);
        when(holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(eq(holidayTypeId))).thenReturn(sampleHolidayType);
        when(holidayTypeRepository.save(any(HolidayType.class))).thenReturn(sampleHolidayType);

        String result = holidayTypeService.lockHolidayType(holidayTypeId, mockUser, 1L);

        assertEquals("", result);
        assertFalse(sampleHolidayType.getIsActive());
        verify(logService).createLog(eq(ActionType.LOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockHolidayType_UnlockSuccess() {
        sampleHolidayType.setIsActive(false);
        when(holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(eq(holidayTypeId))).thenReturn(sampleHolidayType);
        when(holidayTypeRepository.save(any())).thenReturn(sampleHolidayType);

        holidayTypeService.lockHolidayType(holidayTypeId, mockUser, 1L);

        assertTrue(sampleHolidayType.getIsActive());
        verify(logService).createLog(eq(ActionType.UNLOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockHolidayType_Error() {
        sampleHolidayType.setIsActive(false);
        when(holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(eq(holidayTypeId))).thenReturn(null);

        String result = holidayTypeService.lockHolidayType(holidayTypeId, mockUser, 1L);

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
    }

    @Test
    void saveHolidayType_CreateSuccess() {
        HolidayType holidayTypeDTO = new HolidayType();
        holidayTypeDTO.setHolidayTypeName("New HolidayType");
        holidayTypeDTO.setHolidayTypeCode("NEW001");
        holidayTypeDTO.setIsActive(true);

        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(holidayTypeRepository.findByCode(eq("NEW001"), eq(partnerId))).thenReturn(null);
        when(holidayTypeRepository.save(any(HolidayType.class))).thenAnswer(i -> i.getArgument(0));

        String result = holidayTypeService.createHolidayType(holidayTypeDTO, (User) auth.getPrincipal());

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.CREATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void saveHolidayType_CodeExists() {
        HolidayType holidayTypeDTO = new HolidayType();
        holidayTypeDTO.setHolidayTypeCode("TEST001");

        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(holidayTypeRepository.findByCode(eq("TEST001"), eq(partnerId))).thenReturn(sampleHolidayType);

        String result = holidayTypeService.createHolidayType(holidayTypeDTO, (User) auth.getPrincipal());

        assertEquals("error.HolidayTypeExists", result);
        verifyNoInteractions(logService);
    }

    @Test
    void updateHolidayType_Success() {
        HolidayType holidayTypeDTO = new HolidayType();
        holidayTypeDTO.setId(holidayTypeId);
        holidayTypeDTO.setHolidayTypeName("Updated Name");
        holidayTypeDTO.setHolidayTypeCode("UPDATED");
        holidayTypeDTO.setVersion(1L);
        holidayTypeDTO.setIsActive(false);

        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(eq(holidayTypeId))).thenReturn(sampleHolidayType);
        when(holidayTypeRepository.save(any(HolidayType.class))).thenReturn(sampleHolidayType);

        String result = holidayTypeService.updateHolidayType(holidayTypeDTO, (User) auth.getPrincipal());

        assertEquals("", result);
        assertEquals("Updated Name", sampleHolidayType.getHolidayTypeName());
        verify(logService).createLog(eq(ActionType.UPDATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void updateHolidayType_Error() {
        HolidayType holidayTypeDTO = new HolidayType();
        holidayTypeDTO.setId(holidayTypeId);
        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(eq(holidayTypeId))).thenReturn(null);

        String result = holidayTypeService.updateHolidayType(holidayTypeDTO, (User) auth.getPrincipal());

        assertEquals(Constants.errorResponse.DATA_CHANGED, result);
    }

    @Test
    void getListHolidayType_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<HolidayType> page = new PageImpl<>(List.of(sampleHolidayType));

        when(holidayTypeRepository.searchHolidayTypes(any(), any(), any(), any(), eq(partnerId), eq(pageable)))
                .thenReturn(page);

        Page<HolidayType> result = holidayTypeService.searchHolidayTypes("test", "code", "name", "desc", pageable, partnerId);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test HolidayType", result.getContent().get(0).getHolidayTypeName());
    }

    @Test
    void getAllHolidayType_ReturnsList() {
        when(holidayTypeRepository.getAllHolidayType(eq(partnerId))).thenReturn(List.of(sampleHolidayType));

        List<HolidayType> result = holidayTypeService.getAllHolidayType(partnerId);

        assertEquals(1, result.size());
    }

    @Test
    void getLogDetailHolidayType_LogsView() {
        when(holidayTypeRepository.getHolidayTypeByCode(eq("TEST001"))).thenReturn(sampleHolidayType);

        holidayTypeService.getLogDetailHolidayType("TEST001", mockUser);

        verify(logService).createLog(eq(ActionType.VIEW.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void checkDeleteMulti_HasError() {
        sampleHolidayType.setVersion(999L); // version không khớp
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(holidayTypeId,"name","code", 1L));

        when(holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(eq(holidayTypeId))).thenReturn(null);

        ErrorListResponse result = holidayTypeService.checkDeleteMulti(ids);

        assertTrue(result.getHasError());
        assertEquals(Constants.errorResponse.DATA_CHANGED, result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void checkDeleteMulti_NoError_ReturnsNull() {
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(holidayTypeId,"name","code", 1L));

        when(holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(eq(holidayTypeId))).thenReturn(sampleHolidayType);

        ErrorListResponse result = holidayTypeService.checkDeleteMulti(ids);

        assertNull(result);
    }
}
