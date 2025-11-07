package com.noffice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sys_domain")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Domain extends BaseEntity{

    @Column(name = "domain_code", nullable = false, length = 50)
    private String domainCode;

    @Column(name = "domain_name", nullable = false, length = 255)
    private String domainName;

    @Column(name = "domain_description", length = 500)
    private String domainDescription;
}
