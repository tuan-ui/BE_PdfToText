package com.noffice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor // <- thêm cái này
@JsonIgnoreProperties(ignoreUnknown = true)
public class NodeDeptUserDTO {
    private UUID id;
    private UUID userId;        // thêm dòng này
    private String approvalType;
    private String deptName;
    private String note;
    private UUID roleId;
    private String step;
}