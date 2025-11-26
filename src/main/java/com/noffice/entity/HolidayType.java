package com.noffice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sys_holiday_type")
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Data
@AllArgsConstructor
public class HolidayType extends BaseEntity {
    @Column(name = "holiday_type_code", nullable = false, length = 50)
    private String holidayTypeCode;
    @Column(name = "holiday_type_name", nullable = false, length = 255)
    private String holidayTypeName;
    @Column(name = "description", length = 500)
    private String description;
}
