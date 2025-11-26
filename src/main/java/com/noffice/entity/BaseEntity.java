package com.noffice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "partner_id")
    private UUID partnerId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "create_at", nullable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(name = "update_at", nullable = false)
    private LocalDateTime updateAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "create_by")
    private UUID createBy;

    @Column(name = "update_by")
    private UUID updateBy;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (isActive == null) {
            isActive = true;
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
        if (createAt == null) {
            createAt = LocalDateTime.now();
        }
        if (updateAt == null) {
            updateAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updateAt = LocalDateTime.now();
        if (Boolean.TRUE.equals(isDeleted) && deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }
}
