package com.noffice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sys_doc_type")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocType extends BaseEntity{

    @Column(name = "doc_type_code", nullable = false, length = 50)
    private String docTypeCode;

    @Column(name = "doc_type_name", nullable = false, length = 255)
    private String docTypeName;

    @Column(name = "doc_type_description", length = 500)
    private String docTypeDescription;
}
