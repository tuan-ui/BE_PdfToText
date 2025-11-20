package com.noffice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "document_files")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFiles extends BaseEntity{

    @Column(name = "attach_name", length = 250)
    private String attachName;

    @Column(name = "attach_path", length = 500)
    private String attachPath;

    @Column(name = "original_file_id")
    private UUID originalFileId;

    @Column(name = "is_temp")
    private Boolean isTemp = false;

    @Column(name = "lock_value")
    private String lockValue;
    @Column(name = "lock_expires_at")
    private LocalDateTime lockExpiresAt;
    @Column(name = "lock_user_id")
    private UUID lockUserId;

    @Column(name = "is_modified")
    private Boolean isModified = false;

}
