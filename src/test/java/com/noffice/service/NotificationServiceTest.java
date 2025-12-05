package com.noffice.service;

import com.noffice.entity.Notification;
import com.noffice.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @InjectMocks private NotificationService notificationService;

    @Test
    void getNotificationsByRecipientId_ShouldReturnPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Notification> page = new PageImpl<>(List.of(new Notification()), pageable, 1);

        when(notificationRepository.findByRecipientId(99L, pageable)).thenReturn(page);

        Page<Notification> result = notificationService.getNotificationsByRecipientId(99L, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(notificationRepository).findByRecipientId(99L, pageable);
    }

    @Test
    void countNotificationsByRecipientId_ShouldReturnCount() {
        when(notificationRepository.countByRecipientId(100L)).thenReturn(5);

        Integer count = notificationService.countNotificationsByRecipientId(100L);

        assertThat(count).isEqualTo(5);
    }

    @Test
    void saveNotification_ShouldSaveCorrectData() {
        notificationService.saveNotification(
                200L, "Bạn có công việc mới", "TASK", 10L, "Admin",
                500L, "Hợp đồng XYZ"
        );

        verify(notificationRepository).save(argThat(noti ->
                noti.getRecipientId().equals(200L) &&
                        noti.getContent().equals("Bạn có công việc mới") &&
                        noti.getType().equals("TASK") &&
                        noti.getActorName().equals("Admin") &&
                        noti.getTargetId().equals(500L) &&
                        noti.getIsRead() == 0 &&
                        noti.getTimeStamp() != null
        ));
    }

    @Test
    void updateReadNotification_ShouldMarkAsRead() {
        Notification noti = Notification.builder().notificationId(1L).isRead(0).build();
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(noti));

        notificationService.updateReadNotification(1L);

        assertThat(noti.getIsRead()).isEqualTo(1);
        verify(notificationRepository).save(noti);
    }

    @Test
    void updateReadNotification_NotFound_ShouldThrowException() {
        when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.updateReadNotification(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Thông báo không tồn tại");
    }

    @Test
    void decodeAttachId_ValidBase64_ShouldReturnDecodedString() {
        String encoded = java.util.Base64.getEncoder().encodeToString("123QTDabc".getBytes());
        String decoded = notificationService.decodeAttachId(encoded);

        assertThat(decoded).isEqualTo("123QTDabc");
    }

    @Test
    void decodeAttachId_NullOrEmpty_ShouldReturnNull() {
        assertThat(notificationService.decodeAttachId(null)).isNull();
        assertThat(notificationService.decodeAttachId("")).isNull();
    }
}