package com.noffice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sys_task_type")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Data
@AllArgsConstructor
public class TaskType extends BaseEntity {
    @Column(name = "task_type_code", nullable = false, length = 50)
    private String taskTypeCode;
    @Column(name = "task_type_name", nullable = false, length = 255)
    private String taskTypeName;
    @Column(name = "task_type_description", length = 500)
    private String taskTypeDescription;
    @Column(name = "task_type_priority", nullable = false)
    private Integer taskTypePriority;
}
