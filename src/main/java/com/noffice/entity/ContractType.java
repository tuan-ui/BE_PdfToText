package com.noffice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sys_contract_type")
@EqualsAndHashCode(callSuper = false)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractType extends BaseEntity{

    @Column(name = "contract_type_code", nullable = false, length = 50)
    private String contractTypeCode;

    @Column(name = "contract_type_name", nullable = false, length = 255)
    private String contractTypeName;

    @Column(name = "contract_type_description", length = 500)
    private String contractTypeDescription;
}
