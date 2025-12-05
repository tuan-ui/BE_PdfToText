package com.noffice.service;

import java.time.ZonedDateTime;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.noffice.entity.Notification;
import com.noffice.repository.NotificationRepository;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public Page<Notification> getNotificationsByRecipientId(Long recipientId, Pageable pageable) {
        return notificationRepository.findByRecipientId(recipientId, pageable);
    }

    public  String decodeAttachId(String attachIdEncode) {
        if (attachIdEncode == null || attachIdEncode.isEmpty()) return null;
        try {
            byte[] decodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(attachIdEncode);
            return new String(decodedBytes);
        } catch (Exception e) {
            return null;
        }
    }
    
    public Integer countNotificationsByRecipientId(Long recipientId) {
        return notificationRepository.countByRecipientId(recipientId);
    }

    public void saveNotification(Long recipientId, String content, String type, Long actorId, String actorName, Long targetId, String targetName) {
        Notification notification = Notification.builder()
                .recipientId(recipientId)
                .content(content)
                .type(type)
                .actorId(actorId)
                .actorName(actorName)
                .targetId(targetId)
                .targetName(targetName)
                .timeStamp(ZonedDateTime.now())
                .isRead(0)
                .build();

        notificationRepository.save(notification);
    }
    
    public void updateReadNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
					.orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));
        notification.setIsRead(1);
        notificationRepository.save(notification);
    }
}
