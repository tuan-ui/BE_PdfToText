package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.PartnerRequest;
import com.noffice.entity.Partners;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.PartnerRepository;
import com.noffice.repository.UserRepository;
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
public class PartnerServiceTest {
    @Mock
    private PartnerRepository partnerRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private PartnerService partnerService;
    @Mock
    private UserRepository userRepository;

    private User mockUser;
    private Partners samplePartners;
    private UUID partnerId;
    private PartnerRequest partnerRequest;

    @BeforeEach
    void setUp() {
        partnerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        partnerId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setFullName("Nguyễn Văn Test");
        mockUser.setPartnerId(partnerId);

        samplePartners = new Partners();
        samplePartners.setId(partnerId);
        samplePartners.setPartnerName("Test Partners");
        samplePartners.setPartnerCode("TEST001");
        samplePartners.setIsActive(true);
        samplePartners.setIsDeleted(Constants.isDeleted.ACTIVE);
        samplePartners.setVersion(1L);
        samplePartners.setPartnerId(partnerId);

        partnerRequest = new PartnerRequest();
        partnerRequest.setId(partnerId);
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

    @Test
    void deletePartners_Success() {
        when(partnerRepository.getPartnerByIdIncluideDeleted(eq(partnerId))).thenReturn(samplePartners);
        Mockito.doNothing().when(partnerRepository).deletePartnersByPartnersId(any(UUID.class));

        String result = partnerService.deletePartner(partnerId, mockUser, 1L);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.DELETE.getAction()), anyMap(), eq(mockUser.getId()), eq(partnerId), eq(partnerId));
    }

    @Test
    void deletePartners_VersionMismatch() {
        when(partnerRepository.getPartnerByIdIncluideDeleted(eq(partnerId))).thenReturn(samplePartners);

        String result = partnerService.deletePartner(partnerId, mockUser, 999L);

        assertEquals("error.DataChangedReload", result);
        verifyNoInteractions(logService);
    }
    @Test
    void deletePartners_existsUser() {
        when(partnerRepository.getPartnerByIdIncluideDeleted(eq(partnerId))).thenReturn(samplePartners);
        when(userRepository.existsUserByPartnerId(any())).thenReturn(1);
        String result = partnerService.deletePartner(partnerId, mockUser, 1L);

        assertEquals("error.PartnerAlreadyUseOnUser", result);
        verifyNoInteractions(logService);
    }


    @Test
    void deleteMultiPartners_Success() {
        UUID id2 = UUID.randomUUID();
        Partners partner2 = new Partners();
        partner2.setId(id2);
        partner2.setPartnerName("Partners 2");
        partner2.setPartnerCode("TEST002");
        partner2.setVersion(1L);
        partner2.setIsDeleted(Constants.isDeleted.ACTIVE);
        partner2.setPartnerId(partnerId);

        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(partnerId,"name","code", 1L),
                new DeleteMultiDTO(id2,"name","code", 1L)
        );

        when(partnerRepository.getPartnerByIdIncluideDeleted(eq(partnerId))).thenReturn(samplePartners);
        when(partnerRepository.getPartnerByIdIncluideDeleted(eq(id2))).thenReturn(partner2);
        Mockito.doNothing().when(partnerRepository).deletePartnersByPartnersId(any(UUID.class));

        String result = partnerService.deleteMultiPartner(ids, mockUser);

        assertEquals("", result);
        verify(logService, times(2)).createLog(anyString(), anyMap(), any(), any(), any());
    }

    @Test
    void deleteMultiPartners_OneVersionMismatch() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(partnerId,"name","code", 999L)
        );

        when(partnerRepository.getPartnerByIdIncluideDeleted(eq(partnerId))).thenReturn(samplePartners);

        String result = partnerService.deleteMultiPartner(ids, mockUser);

        assertEquals("error.DataChangedReload", result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteMultiPartners_existsUser() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(partnerId,"name","code", 1L)
        );

        when(partnerRepository.getPartnerByIdIncluideDeleted(eq(partnerId))).thenReturn(samplePartners);
        when(userRepository.existsUserByPartnerId(any())).thenReturn(1);
        String result = partnerService.deleteMultiPartner(ids, mockUser);

        assertEquals("error.PartnerAlreadyUseOnUser", result);
        verifyNoInteractions(logService);
    }

    @Test
    void lockUnlockPartners_LockSuccess() {
        samplePartners.setIsActive(true);
        when(partnerRepository.getPartnerByIdIncluideDeleted(eq(partnerId))).thenReturn(samplePartners);
        when(partnerRepository.save(any(Partners.class))).thenReturn(samplePartners);

        String result = partnerService.lockPartner(partnerId, mockUser, 1L);

        assertEquals("", result);
        assertFalse(samplePartners.getIsActive());
        verify(logService).createLog(eq(ActionType.LOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockPartners_UnlockSuccess() {
        samplePartners.setIsActive(false);
        when(partnerRepository.getPartnerByIdIncluideDeleted(eq(partnerId))).thenReturn(samplePartners);
        when(partnerRepository.save(any())).thenReturn(samplePartners);

        partnerService.lockPartner(partnerId, mockUser, 1L);

        assertTrue(samplePartners.getIsActive());
        verify(logService).createLog(eq(ActionType.UNLOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockPartners_Error() {
        samplePartners.setIsActive(false);
        when(partnerRepository.getPartnerByIdIncluideDeleted(eq(partnerId))).thenReturn(null);

        String result = partnerService.lockPartner(partnerId, mockUser, 1L);

        assertEquals("error.DataChangedReload", result);
    }

    @Test
    void savePartners_CreateSuccess() {

        when(partnerRepository.getPartnerByCode(any())).thenReturn(null);
        when(partnerRepository.save(any(Partners.class))).thenAnswer(i -> i.getArgument(0));

        String result = partnerService.createPartner(partnerRequest, mockUser);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.CREATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void savePartners_CodeExists() {

        when(partnerRepository.getPartnerByCode(any())).thenReturn(samplePartners);

        String result = partnerService.createPartner(partnerRequest, mockUser);

        assertEquals("error.PartnerIsExist", result);
        verifyNoInteractions(logService);
    }

    @Test
    void updatePartners_Success() {

        when(partnerRepository.getPartnerByCode(any())).thenReturn(samplePartners);
        when(partnerRepository.save(any(Partners.class))).thenReturn(samplePartners);

        String result = partnerService.updatePartner(partnerRequest, mockUser);

        assertEquals("", result);
        assertEquals("Hành chính", samplePartners.getPartnerName());
        verify(logService).createLog(eq(ActionType.UPDATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void updatePartners_Error() {

        when(partnerRepository.getPartnerByCode(any())).thenReturn(null);

        String result = partnerService.updatePartner(partnerRequest, mockUser);

        assertEquals("error.DataChangedReload", result);
    }

    @Test
    void getListPartners_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Partners> page = new PageImpl<>(List.of(samplePartners));

        when(partnerRepository.searchPartners(any(), any(), any(), any(), any(), eq(pageable)))
                .thenReturn(page);

        Page<Partners> result = partnerService.searchPartners(partnerRequest, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Partners", result.getContent().get(0).getPartnerName());
    }

    @Test
    void getLogDetailPartners_LogsView() {
        when(partnerRepository.getPartnerById(eq(partnerId))).thenReturn(samplePartners);

        partnerService.getLogDetailPartner(partnerId, mockUser);

        verify(logService).createLog(eq(ActionType.VIEW.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void checkDeleteMulti_HasError() {
        samplePartners.setVersion(999L); // version không khớp
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(partnerId,"name","code", 1L));

        when(partnerRepository.getPartnerByIdIncluideDeleted(eq(partnerId))).thenReturn(null);

        ErrorListResponse result = partnerService.checkDeleteMulti(ids, mockUser);

        assertTrue(result.getHasError());
        assertEquals("error.DataChangedReload", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void checkDeleteMulti_NoError_ReturnsNull() {
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(partnerId,"name","code", 1L));

        when(partnerRepository.getPartnerByIdIncluideDeleted(eq(partnerId))).thenReturn(samplePartners);

        ErrorListResponse result = partnerService.checkDeleteMulti(ids, mockUser);

        assertNull(result);
    }

    @Test
    void checkDeleteMulti_existsUserByPartnerIdl() {
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(partnerId,"name","code", 1L));

        when(partnerRepository.getPartnerByIdIncluideDeleted(eq(partnerId))).thenReturn(samplePartners);
        when(userRepository.existsUserByPartnerId(any())).thenReturn(1);
        ErrorListResponse result = partnerService.checkDeleteMulti(ids, mockUser);

        assertTrue(result.getHasError());
        assertEquals("error.PartnerAlreadyUseOnUser", result.getErrors().get(0).getErrorMessage());

    }

    @Test
    void updatePartnerImage_Success() {

        when(partnerRepository.getPartnerById(any())).thenReturn(samplePartners);
        when(partnerRepository.save(any())).thenReturn(samplePartners);

        Partners result = partnerService.updatePartnerImage(partnerRequest, mockUser);

        assertEquals(partnerRequest.getBase64Image(), result.getImgLogo());
            }

    @Test
    void updatePartnerImage_Faild() {
        partnerRequest.setBase64Image(null);

        Partners result = partnerService.updatePartnerImage(partnerRequest, mockUser);

        assertNull(result);
    }
}
