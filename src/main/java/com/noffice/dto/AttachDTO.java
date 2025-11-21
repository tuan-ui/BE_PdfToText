package com.noffice.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttachDTO {

    private UUID attachId;
    private UUID objectId;
    private Integer objectType;
    private String attachName;
    private String attachPath;
    private UUID creatorId;
    private LocalDateTime dateCreate;
    private Integer attachType;
    private String savePath;
    private Long versionNumber;
    private Boolean isActive;
    private Boolean isDeleted;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private LocalDateTime deletedAt;
    private UUID partnerId;
    private UUID createBy;
    private UUID updateBy;
    private UUID deletedBy;
}
