package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.*;
import com.noffice.enumtype.ActionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.DomainRepository;
import com.noffice.ultils.Constants;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DomainServiceTest {

    @Mock
    private DomainRepository domainRepository;

    @Mock
    private LogService logService;

    @InjectMocks
    private DomainService domainService;

    private User mockUser;
    private Domain sampleDomain;
    private UUID domainId;
    private UUID partnerId;

    @BeforeEach
    void setUp() {
        partnerId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        domainId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setUsername("testuser");
        mockUser.setFullName("Nguyễn Văn Test");
        mockUser.setPartnerId(partnerId);

        sampleDomain = new Domain();
        sampleDomain.setId(domainId);
        sampleDomain.setDomainName("Test Domain");
        sampleDomain.setDomainCode("TEST001");
        sampleDomain.setIsActive(true);
        sampleDomain.setIsDeleted(Constants.isDeleted.ACTIVE);
        sampleDomain.setVersion(1L);
        sampleDomain.setPartnerId(partnerId);
    }

    @Test
    void deleteDomain_Success() {
        when(domainRepository.findByDomainIdIncludeDeleted(eq(domainId))).thenReturn(sampleDomain);
        Mockito.doNothing().when(domainRepository).deleteDomainByDomainId(any(UUID.class));

        String result = domainService.deleteDomain(domainId, mockUser, 1L);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.DELETE.getAction()), anyMap(), eq(mockUser.getId()), eq(domainId), eq(partnerId));
    }

    @Test
    void deleteDomain_VersionMismatch() {
        when(domainRepository.findByDomainIdIncludeDeleted(eq(domainId))).thenReturn(sampleDomain);

        String result = domainService.deleteDomain(domainId, mockUser, 999L);

        assertEquals("error.DataChangedReload", result);
        verifyNoInteractions(logService);
    }

    @Test
    void deleteMultiDomain_Success() {
        UUID id2 = UUID.randomUUID();
        Domain domain2 = new Domain();
        domain2.setId(id2);
        domain2.setDomainName("Domain 2");
        domain2.setDomainCode("TEST002");
        domain2.setVersion(1L);
        domain2.setIsDeleted(Constants.isDeleted.ACTIVE);
        domain2.setPartnerId(partnerId);

        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(domainId,"name","code", 1L),
                new DeleteMultiDTO(id2,"name","code", 1L)
        );

        when(domainRepository.findByDomainIdIncludeDeleted(eq(domainId))).thenReturn(sampleDomain);
        when(domainRepository.findByDomainIdIncludeDeleted(eq(id2))).thenReturn(domain2);
        Mockito.doNothing().when(domainRepository).deleteDomainByDomainId(any(UUID.class));

        String result = domainService.deleteMultiDomain(ids, mockUser);

        assertEquals("", result);
        verify(logService, times(2)).createLog(anyString(), anyMap(), any(), any(), any());
    }

    @Test
    void deleteMultiDomain_OneVersionMismatch() {
        List<DeleteMultiDTO> ids = List.of(
                new DeleteMultiDTO(domainId,"name","code", 999L)
        );

        when(domainRepository.findByDomainIdIncludeDeleted(eq(domainId))).thenReturn(sampleDomain);

        String result = domainService.deleteMultiDomain(ids, mockUser);

        assertEquals("error.DataChangedReload", result);
        verifyNoInteractions(logService);
    }

    @Test
    void lockUnlockDomain_LockSuccess() {
        sampleDomain.setIsActive(true);
        when(domainRepository.findByDomainIdIncludeDeleted(eq(domainId))).thenReturn(sampleDomain);
        when(domainRepository.save(any(Domain.class))).thenReturn(sampleDomain);

        String result = domainService.lockUnlockDomain(domainId, mockUser, 1L);

        assertEquals("", result);
        assertFalse(sampleDomain.getIsActive());
        verify(logService).createLog(eq(ActionType.LOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockDomain_UnlockSuccess() {
        sampleDomain.setIsActive(false);
        when(domainRepository.findByDomainIdIncludeDeleted(eq(domainId))).thenReturn(sampleDomain);
        when(domainRepository.save(any())).thenReturn(sampleDomain);

        domainService.lockUnlockDomain(domainId, mockUser, 1L);

        assertTrue(sampleDomain.getIsActive());
        verify(logService).createLog(eq(ActionType.UNLOCK.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void lockUnlockDomain_Error() {
        sampleDomain.setIsActive(false);
        when(domainRepository.findByDomainIdIncludeDeleted(eq(domainId))).thenReturn(null);

        String result = domainService.lockUnlockDomain(domainId, mockUser, 1L);

        assertEquals("error.DataChangedReload", result);
    }

    @Test
    void saveDomain_CreateSuccess() {
        Domain domainDTO = new Domain();
        domainDTO.setDomainName("New Domain");
        domainDTO.setDomainCode("NEW001");
        domainDTO.setIsActive(true);

        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(domainRepository.findByCode(eq("NEW001"), eq(partnerId))).thenReturn(null);
        when(domainRepository.save(any(Domain.class))).thenAnswer(i -> i.getArgument(0));

        String result = domainService.saveDomain(domainDTO, auth);

        assertEquals("", result);
        verify(logService).createLog(eq(ActionType.CREATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void saveDomain_CodeExists() {
        Domain domainDTO = new Domain();
        domainDTO.setDomainCode("TEST001");

        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(domainRepository.findByCode(eq("TEST001"), eq(partnerId))).thenReturn(sampleDomain);

        String result = domainService.saveDomain(domainDTO, auth);

        assertEquals("error.DomainExists", result);
        verifyNoInteractions(logService);
    }

    @Test
    void updateDomain_Success() {
        Domain domainDTO = new Domain();
        domainDTO.setId(domainId);
        domainDTO.setDomainName("Updated Name");
        domainDTO.setDomainCode("UPDATED");
        domainDTO.setVersion(1L);
        domainDTO.setIsActive(false);

        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(domainRepository.findByDomainIdIncludeDeleted(eq(domainId))).thenReturn(sampleDomain);
        when(domainRepository.save(any(Domain.class))).thenReturn(sampleDomain);

        String result = domainService.updateDomain(domainDTO, auth);

        assertEquals("", result);
        assertEquals("Updated Name", sampleDomain.getDomainName());
        verify(logService).createLog(eq(ActionType.UPDATE.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void updateDomain_Error() {
        Domain domainDTO = new Domain();
        domainDTO.setId(domainId);
        Authentication auth = new TestingAuthenticationToken(mockUser, null);

        when(domainRepository.findByDomainIdIncludeDeleted(eq(domainId))).thenReturn(null);

        String result = domainService.updateDomain(domainDTO, auth);

        assertEquals("error.DataChangedReload", result);
    }

    @Test
    void getListDomain_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Domain> page = new PageImpl<>(List.of(sampleDomain));

        when(domainRepository.getDomainWithPagination(any(), any(), any(), any(), eq(partnerId), eq(pageable)))
                .thenReturn(page);

        Page<Domain> result = domainService.getListDomain("test", "code", "name", "desc", pageable, partnerId);

        assertEquals(1, result.getTotalElements());
        assertEquals("Test Domain", result.getContent().get(0).getDomainName());
    }

    @Test
    void getAllDomain_ReturnsList() {
        when(domainRepository.getAllDomain(eq(partnerId))).thenReturn(List.of(sampleDomain));

        List<Domain> result = domainService.getAllDomain(partnerId);

        assertEquals(1, result.size());
    }

    @Test
    void getLogDetailDomain_LogsView() {
        when(domainRepository.findByDomainCode(eq("TEST001"))).thenReturn(sampleDomain);

        domainService.getLogDetailDomain("TEST001", mockUser);

        verify(logService).createLog(eq(ActionType.VIEW.getAction()), anyMap(), any(), any(), any());
    }

    @Test
    void checkDeleteMulti_HasError() {
        sampleDomain.setVersion(999L); // version không khớp
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(domainId,"name","code", 1L));

        when(domainRepository.findByDomainIdIncludeDeleted(eq(domainId))).thenReturn(null);

        ErrorListResponse result = domainService.checkDeleteMulti(ids);

        assertTrue(result.getHasError());
        assertEquals("error.DataChangedReload", result.getErrors().get(0).getErrorMessage());
    }

    @Test
    void checkDeleteMulti_NoError_ReturnsNull() {
        List<DeleteMultiDTO> ids = List.of(new DeleteMultiDTO(domainId,"name","code", 1L));

        when(domainRepository.findByDomainIdIncludeDeleted(eq(domainId))).thenReturn(sampleDomain);

        ErrorListResponse result = domainService.checkDeleteMulti(ids);

        assertNull(result);
    }
}
