package com.noffice.entity;

import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Table(name = "form_datas")
@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class FormData extends BaseEntity {

    @Column(name = "form_code")
    private String formCode;

    @Column(name = "form_content")
    private String formContent;

}
