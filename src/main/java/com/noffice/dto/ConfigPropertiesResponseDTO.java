package com.noffice.dto;

import com.noffice.entity.ConfigProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigPropertiesResponseDTO {
    private Long total;
    private List<ConfigProperties> roles;
}
