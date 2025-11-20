package com.noffice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "document_template")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTemplate extends BaseEntity{

    @Column(name = "document_template_code", nullable = false, length = 50)
    private String documentTemplateCode;

    @Column(name = "document_template_name", nullable = false, length = 255)
    private String documentTemplateName;

    @Column(name = "document_template_description", length = 500)
    private String documentTemplateDescription;

    @Column(name = "attach_file_id", nullable = false)
    private UUID attachFileId;
}
