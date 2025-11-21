package com.noffice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "node_dept_user")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeDeptUser extends BaseEntity{

    @Column(name = "doc_id")
    private UUID docId;
    @Column(name = "step")
    private Integer step;
    @Column(name = "user_id")
    private UUID userId;
    @Column(name = "dept_id")
    private UUID deptId;
    @Column(name = "dept_name")
    private String deptName;

    @Column(name = "role_id")
    private UUID roleId;
    @Column(name = "approve_type")
    private String approveType;
    @Column(name = "note")
    private String note;
    @Column(name = "parallel_step")
    private Integer parallelStep;
}