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
public class DocumentTemplateCreateDTO {
    private UUID id;
    private Long version;
    private Boolean isActive;
    private String documentTemplateCode;
    private String documentTemplateName;
    private String documentTemplateDescription;
    private List<UUID> documentTypeIds;
    private UUID attachFileId;
    private String fileName;
    private String wopiUrl;
    private List<UUID> allowedEditors;
    private List<UUID> allowedViewers;

}
