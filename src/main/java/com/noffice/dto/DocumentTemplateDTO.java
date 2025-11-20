package com.noffice.dto;

import com.noffice.entity.DocumentFiles;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTemplateDTO {
    private UUID id;
    private Integer version;
    private UUID  partnerId;
    private Boolean isActive;
    private String documentTemplateCode;
    private String documentTemplateName;
    private String documentTemplateDescription;
    private List<UUID> documentTypeIds;
    private DocumentFiles attachFile;

}
