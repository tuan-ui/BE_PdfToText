package com.noffice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "doc_document")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocDocument extends BaseEntity{
    @Column(name = "doc_template_id")
    private UUID docTemplateId;

    @Column(name = "doc_type_id")
    private UUID docTypeId;
    @Column(name = "document_title")
    private String documentTitle;

    @Column(name = "dept_name")
    private String deptName;
    @Column(name = "purpose")
    private String purpose;
    @Column(name = "form_data")
    private String formData;


    @Transient
    private String docTypeName;

}
