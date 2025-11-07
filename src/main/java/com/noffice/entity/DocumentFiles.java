package com.noffice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_files")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentFiles extends BaseEntity{

    @Column(name = "attach_name", length = 250)
    private String attachName;

    @Column(name = "attach_path", length = 500)
    private String attachPath;

}
