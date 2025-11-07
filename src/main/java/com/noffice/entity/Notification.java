package com.noffice.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", nullable = false)
    private Long notificationId;

    @Column(name = "recipient_id")
    private Long recipientId;
    
    @Column(name = "content")
    private String content;
    
    @Column(name = "timestamp")
    @JsonFormat(pattern = "dd/MM/yyyy HH:mm", timezone = "Asia/Ho_Chi_Minh")
    private ZonedDateTime timeStamp;
    
    @Column(name = "type")
    private String type;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_name")
    private String actorName;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "target_name")
    private String targetName;
    
    @Column(name = "is_read")
    private Integer isRead;

    @Column(name = "target_id_encode")
    private String targetIdEncode;
}