package com.noffice.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "partners")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Data
@AllArgsConstructor
public class Partners extends BaseEntity implements Cloneable  {

    @Column(name = "partner_code", nullable = false, length = 50)
    private String partnerCode;
    @Column(name = "partner_name", nullable = false, length = 255)
    private String partnerName;
    @Column(name = "address")
    private String address;
    @Column(name = "fax")
    private String fax;
    @Column(name = "email", length = 50)
    private String email;
    @Column(name = "phone", length = 12)
    private String phone;
    @Column(name = "website", length = 255)
    private String website;
    @Column(name = "img_logo")
    private String imgLogo;
    @Column(name = "tax_code", length = 50)
    private String taxCode;

    @Override
    public Partners clone() {
        try {
        	Partners cloned = (Partners) super.clone();
            cloned.setPartnerId(null); 
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Failed to clone Department", e);
        }
    }

}
