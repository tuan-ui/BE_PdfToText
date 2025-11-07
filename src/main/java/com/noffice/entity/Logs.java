package com.noffice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Logs {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "action_key", length = 255)
    private String actionKey;

    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(name = "create_by")
    private UUID createBy;

    @Column(name = "params", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> params;

    @Column(name = "object_id")
    private UUID objectId;

    @Column(name = "partner_id")
    private UUID partnerId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createAt == null) {
            createAt = LocalDateTime.now();
        }
    }

}