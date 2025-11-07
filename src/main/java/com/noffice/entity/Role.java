package com.noffice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity implements Cloneable{


    @Column(name = "role_name", nullable = false, length = 255)
    private String roleName;

    @Column(name = "role_code", nullable = false, length = 50)
    private String roleCode;

    @Column(name = "role_description", length = 2000)
    private String roleDescription;

    @Column(name = "priority")
    private Long priority;

    @Override
    public Role clone() {
        try {
        	Role cloned = (Role) super.clone();
            cloned.setId(null);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone Role", e);
        }
    }
}
