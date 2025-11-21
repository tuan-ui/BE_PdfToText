package com.noffice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "attachs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachs extends BaseEntity{

    @Column(name = "object_id")
    private UUID objectId;

    @Column(name = "object_type")
    private Integer objectType;

    @Column(name = "attach_name", length = 250)
    private String attachName;

    @Column(name = "attach_path", length = 500)
    private String attachPath;

    @Column(name = "creator_id")
    private UUID creatorId;

    @Column(name = "date_create")
    private LocalDateTime dateCreate;

    @Column(name = "attach_type")
    private Integer attachType;

    @Column(name = "save_path", length = 100)
    private String savePath;
}
