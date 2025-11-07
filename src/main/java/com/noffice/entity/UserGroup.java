package com.noffice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_group")
public class UserGroup extends BaseEntity {
    @Column(name = "group_code", nullable = false, length = 50)
    private String groupCode;

    @Column(name = "group_name", nullable = false, length = 255)
    private String groupName;
}
