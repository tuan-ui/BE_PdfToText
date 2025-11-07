package com.noffice.entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "config_properties")
public class ConfigProperties {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_properties_id")
    private Long id;  // ID tự động tăng

    @Column(name = "key", nullable = false)
    private String key; // Mã config

    @Column(name = "value", nullable = false)
    private String value; // Giá trị config

    @Column(name = "dept_id")
    private Long deptId; // ID phòng ban

    @Column(name = "user_id")
    private Long userId; // ID user tạo

    @Column(name = "type")
    private Integer type; // Loại config

    @Column(name = "value_type")
    private String valueType; // Giá trị config
    
    @Column(name = "title")
    private String title; // Giá trị config
    
    @Column(name = "description")
    private String description; // Giá trị config
}
