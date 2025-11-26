package com.noffice.service;

import com.noffice.entity.Logs;
import com.noffice.repository.LogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private LogService logService;
    @Test
    void testCreateLog_Success() {

        Map<String, Object> params = new HashMap<>();
        params.put("key", "value");

        UUID userId = UUID.randomUUID();
        UUID objectId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();

        // Act
        logService.createLog("TEST_ACTION", params, userId, objectId, partnerId);

        // Capture entity được lưu
        ArgumentCaptor<Logs> logCaptor = ArgumentCaptor.forClass(Logs.class);
        verify(logRepository).save(logCaptor.capture());

        Logs saved = logCaptor.getValue();

        assertEquals("TEST_ACTION", saved.getActionKey());
        assertEquals(params, saved.getParams());
        assertEquals(userId, saved.getCreateBy());
        assertEquals(objectId, saved.getObjectId());
        assertEquals(partnerId, saved.getPartnerId());
        assertTrue(saved.getIsActive());
        assertFalse(saved.getIsDeleted());
        assertNotNull(saved.getCreateAt());
    }

    @Test
    void testGetLogs_SuccessWithValidDate() throws Exception {
        UUID partnerId = UUID.randomUUID();
        PageRequest pageable = PageRequest.of(0, 10);

        Logs log = new Logs();
        Page<Logs> mockPage = new PageImpl<>(List.of(log));

        when(logRepository.getLogs(
                any(), any(), any(), anyString(), anyString(), any(UUID.class), any(PageRequest.class)
        )).thenReturn(mockPage);

        Page<Logs> result = logService.getLogs(
                null,
                "log.create",
                "log.action.auth.login",
                "01/01/2025",
                "05/01/2025",
                pageable,
                partnerId
        );

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }


    @Test
    void testGetLogs_InvalidDate_ThrowsException() {

        Exception exception = assertThrows(Exception.class, () ->
                logService.getLogs(
                        null,
                        null,
                        null,
                        "2025-01-01",       // sai format
                        "",
                        PageRequest.of(0, 10),
                        UUID.randomUUID()
                )
        );

        assertEquals("Định dạng ngày không hợp lệ, yêu cầu: dd/MM/yyyy", exception.getMessage());
    }


    @Test
    void testGetLogs_ActionKey_And_FunctionKey_Converted() throws Exception {

        Page<Logs> mockPage = new PageImpl<>(List.of(new Logs()));

        when(logRepository.getLogs(
                any(), any(), any(), anyString(), anyString(), any(), any()
        )).thenReturn(mockPage);

        logService.getLogs(
                null,
                "log.create",
                "log.action.auth.login",
                "",
                "",
                PageRequest.of(0, 10),
                UUID.randomUUID()
        );

        verify(logRepository, times(1))
                .getLogs(
                        isNull(),
                        isNull(),
                        isNull(),
                        eq("log.action.auth.login"),
                        eq("log.create"),
                        any(UUID.class),
                        any(PageRequest.class)
                );
    }

}
