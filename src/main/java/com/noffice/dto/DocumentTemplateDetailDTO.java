package com.noffice.dto;

import com.noffice.entity.DocType;
import com.noffice.entity.FormSchema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentTemplateDetailDTO {
    private UUID id;
    private Long version;
    private Boolean isActive;
    private String documentTemplateCode;
    private String documentTemplateName;
    private String documentTemplateDescription;
    private UUID attachFileId;
    private String fileName;
    private String wopiUrl;
    private List<DocType> documentTypes;
    private FormSchema formSchema;
}
