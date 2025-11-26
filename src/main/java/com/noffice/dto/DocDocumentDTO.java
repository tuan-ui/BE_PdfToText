package com.noffice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor // <- thêm cái này
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocDocumentDTO {
    private UUID id;
    private String documentTitle;
    private String docTemplateName;
    private String deptName;
    private String formData;
    private UUID docTypeId;
    private UUID docTemplateId;
    private UUID formSchemaId;
    private String docTypeName;
    private String purpose;
    private Boolean isActive = true;
    private UUID[]removedFiles;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createAt;
    @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime updateAt;
    private List<NodeDeptUserDTO> approvalSteps;
    public DocDocumentDTO(UUID documentId, String documentTitle, String deptName,String purpose,String formData, UUID docTypeId,UUID docTemplateId, String docTypeName,LocalDateTime createAt, LocalDateTime updateAt,UUID formSchemaId,String docTemplateName) {
        this.id = documentId;
        this.documentTitle = documentTitle;
        this.deptName = deptName;
        this.purpose=purpose;
        this.formData  =formData;
        this.docTypeId = docTypeId;
        this.docTemplateId = docTemplateId;
        this.docTypeName = docTypeName;
        this.createAt = createAt;
        this.updateAt = updateAt;
        this.formSchemaId = formSchemaId;
        this.docTemplateName = docTemplateName;
    }
}
