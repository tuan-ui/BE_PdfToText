package com.noffice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import java.util.UUID;

@Entity
@Table(name = "doctemplates_doctypes")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocTemplateDocTypes {

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "document_template_id")
    private UUID documentTemplateId;

    @Column(name = "document_type_id")
    private UUID documentTypeId;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }
}