package com.noffice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Table(name = "form_schemas")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FormSchema extends BaseEntity {

    @Column(name = "form_code")
    private String formCode;

    @Column(name = "form_name")
    private String formName;

    @Column(name = "form_content")
    private String formContent;

    @Column(name = "doc_template_id")
    private UUID docTemplateId;
    
}
