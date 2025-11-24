package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.DocumentTemplateCreateDTO;
import com.noffice.dto.DocumentTemplateDTO;
import com.noffice.dto.DocumentTemplateDetailDTO;
import com.noffice.entity.*;
import com.noffice.enumtype.ActionType;
import com.noffice.enumtype.FunctionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.*;
import com.noffice.ultils.Constants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentTemplateService {

    @Value("${URL.COLLABORA.OFFICE}")
    private String urlCollaboraOffice;

    @Value("${API.URL}")
    private String apiUrl;


    private final DocumentTemplateRepository documentTemplateRepository;


    private final LogService logService;

    private final DocumentFileRepository documentFileRepository;

    private final DocumentTemplateDocumentTypesRepository documentTemplateDocumentTypesRepository;

    private final ModelMapper mapper;

    private final DocumentAllowedEditorsRepository documentAllowedEditorsRepository;

    private final DocumentAllowedViewersRepository documentAllowedViewersRepository;

    private final JwtService jwtService;

    private final FormSchemaRepository formSchemaRepository;

    @Transactional
    public String deleteDocumentTemplate(UUID id, User user, Long version) {
        DocumentTemplate documentTemplate = documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(id);
        if (documentTemplate == null || !Objects.equals(documentTemplate.getVersion(), version)) {
            return "error.DataChangedReload";
        } else {
            documentTemplateRepository.deleteDocumentTemplateByDocumentTemplateId(id);
            documentTemplateDocumentTypesRepository.deleteByDocumentTemplateId(documentTemplate.getId());
            logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_DOC_TEMPLATE.getFunction(), "object", documentTemplate.getDocumentTemplateName()),
                    user.getId(), documentTemplate.getId(), user.getPartnerId());
        }
        return "";

    }

    @Transactional
    public String deleteMultiDocumentTemplate(List<DeleteMultiDTO> ids, User user) {
        for (DeleteMultiDTO id : ids) {
            DocumentTemplate documentTemplate = documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(id.getId());
            if (documentTemplate == null || !Objects.equals(documentTemplate.getVersion(), id.getVersion())) {
                return "error.DataChangedReload";
            } else {
                documentTemplateRepository.deleteDocumentTemplateByDocumentTemplateId(id.getId());
                documentTemplateDocumentTypesRepository.deleteByDocumentTemplateId(id.getId());
                logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_DOC_TEMPLATE.getFunction(), "object", documentTemplate.getDocumentTemplateName()),
                        user.getId(), documentTemplate.getId(), user.getPartnerId());
            }
        }
        return "";
    }

    @Transactional
    public String lockUnlockDocumentTemplate(UUID id, User user, Long version) {
        DocumentTemplate documentTemplate = documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(id);
        if (documentTemplate == null || !Objects.equals(documentTemplate.getVersion(), version)) {
            return "error.DataChangedReload";
        } else {
            Boolean newStatus = !documentTemplate.getIsActive();
            documentTemplate.setIsActive(newStatus);

            documentTemplate.setUpdateBy(user.getId());
            documentTemplate.setUpdateAt(LocalDateTime.now());
            DocumentTemplate savedDocumentTemplate = documentTemplateRepository.save(documentTemplate);
            logService.createLog(savedDocumentTemplate.getIsActive() ? ActionType.UNLOCK.getAction() : ActionType.LOCK.getAction(),
                    Map.of("actor", user.getFullName(), "action", savedDocumentTemplate.getIsActive() ? FunctionType.UNLOCK_DOC_TEMPLATE.getFunction() : FunctionType.LOCK_DOC_TEMPLATE.getFunction(), "object", savedDocumentTemplate.getDocumentTemplateName()),
                    user.getId(), savedDocumentTemplate.getId(), user.getPartnerId());
        }
        return "";
    }

    @Transactional
    public String saveDocumentTemplate(DocumentTemplateCreateDTO documentTemplateDTO, Authentication authentication) {
        User token = (User) authentication.getPrincipal();
        if (documentTemplateRepository.findByCode(documentTemplateDTO.getDocumentTemplateCode(), token.getPartnerId()) == null) {
            DocumentTemplate documentTemplate = new DocumentTemplate();
            documentTemplate.setDocumentTemplateName(documentTemplateDTO.getDocumentTemplateName());
            documentTemplate.setDocumentTemplateCode(documentTemplateDTO.getDocumentTemplateCode());
            documentTemplate.setDocumentTemplateDescription(documentTemplateDTO.getDocumentTemplateDescription());
            documentTemplate.setAttachFileId(documentTemplateDTO.getAttachFileId());
            documentTemplate.setCreateAt(LocalDateTime.now());
            documentTemplate.setCreateBy(token.getId());
            documentTemplate.setIsActive(documentTemplateDTO.getIsActive());
            documentTemplate.setIsDeleted(Constants.isDeleted.ACTIVE);
            documentTemplate.setPartnerId(token.getPartnerId());
            DocumentTemplate savedDocumentTemplate = documentTemplateRepository.save(documentTemplate);
            List<DocTemplateDocTypes> documentTemplateDocumentTypes = documentTemplateDTO.getDocumentTypeIds().stream()
                    .map(docTypeId -> {
                        DocTemplateDocTypes dtdt = new DocTemplateDocTypes();
                        dtdt.setDocumentTemplateId(savedDocumentTemplate.getId());
                        dtdt.setDocumentTypeId(docTypeId);
                        return dtdt;
                    })
                    .collect(Collectors.toList());
            documentTemplateDocumentTypesRepository.saveAll(documentTemplateDocumentTypes);
            // Xóa cũ
            documentAllowedEditorsRepository.deleteAllByDocumentId(documentTemplateDTO.getAttachFileId());
            documentAllowedViewersRepository.deleteAllByDocumentId(documentTemplateDTO.getAttachFileId());
            documentTemplateDTO.getAllowedEditors().forEach(id -> {
                DocumentAllowedEditors e = new DocumentAllowedEditors();
                e.setDocumentId(documentTemplateDTO.getAttachFileId());
                e.setEditorId(id);
                documentAllowedEditorsRepository.save(e);
            });

            documentTemplateDTO.getAllowedViewers().forEach(id -> {
                DocumentAllowedViewers v = new DocumentAllowedViewers();
                v.setDocumentId(documentTemplateDTO.getAttachFileId());
                v.setViewerId(id);
                documentAllowedViewersRepository.save(v);
            });
            logService.createLog(ActionType.CREATE.getAction(), Map.of("actor", token.getFullName(), "action", FunctionType.CREATE_DOC_TEMPLATE.getFunction(), "object", savedDocumentTemplate.getDocumentTemplateName()),
                    token.getId(), savedDocumentTemplate.getId(), token.getPartnerId());

            return "";
        } else {
            return "error.DocumentTemplateExists";
        }
    }

    @Transactional
    public String updateDocumentTemplate(DocumentTemplateCreateDTO documentTemplateDTO, Authentication authentication) {
        User token = (User) authentication.getPrincipal();
        DocumentTemplate documentTemplate = documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(documentTemplateDTO.getId());
        if (documentTemplate == null || !Objects.equals(documentTemplate.getVersion(), documentTemplateDTO.getVersion())) {
            return "error.DataChangedReload";
        } else {
            documentTemplate.setDocumentTemplateName(documentTemplateDTO.getDocumentTemplateName());
            documentTemplate.setDocumentTemplateCode(documentTemplateDTO.getDocumentTemplateCode());
            documentTemplate.setDocumentTemplateDescription(documentTemplateDTO.getDocumentTemplateDescription());
            documentTemplate.setIsActive(documentTemplateDTO.getIsActive());
            documentTemplate.setPartnerId(token.getPartnerId());
            documentTemplate.setUpdateAt(LocalDateTime.now());
            documentTemplate.setUpdateBy(token.getId());
            DocumentTemplate savedDocumentTemplate = documentTemplateRepository.save(documentTemplate);

            documentTemplateDocumentTypesRepository.deleteByDocumentTemplateId(savedDocumentTemplate.getId());
            List<DocTemplateDocTypes> documentTemplateDocumentTypes = documentTemplateDTO.getDocumentTypeIds().stream()
                    .map(docTypeId -> {
                        DocTemplateDocTypes dtdt = new DocTemplateDocTypes();
                        dtdt.setDocumentTemplateId(savedDocumentTemplate.getId());
                        dtdt.setDocumentTypeId(docTypeId);
                        return dtdt;
                    })
                    .collect(Collectors.toList());
            documentTemplateDocumentTypesRepository.saveAll(documentTemplateDocumentTypes);
            // Xóa cũ
            documentAllowedEditorsRepository.deleteAllByDocumentId(documentTemplateDTO.getAttachFileId());
            documentAllowedViewersRepository.deleteAllByDocumentId(documentTemplateDTO.getAttachFileId());
            documentTemplateDTO.getAllowedEditors().forEach(id -> {
                DocumentAllowedEditors e = new DocumentAllowedEditors();
                e.setDocumentId(documentTemplateDTO.getAttachFileId());
                e.setEditorId(id);
                documentAllowedEditorsRepository.save(e);
            });

            documentTemplateDTO.getAllowedViewers().forEach(id -> {
                DocumentAllowedViewers v = new DocumentAllowedViewers();
                v.setDocumentId(documentTemplateDTO.getAttachFileId());
                v.setViewerId(id);
                documentAllowedViewersRepository.save(v);
            });
            logService.createLog(ActionType.UPDATE.getAction(), Map.of("actor", token.getFullName(), "action", FunctionType.EDIT_DOC_TEMPLATE.getFunction(), "object", savedDocumentTemplate.getDocumentTemplateName()),
                    token.getId(), savedDocumentTemplate.getId(), token.getPartnerId());
        }
        return "";
    }

    public Page<DocumentTemplateDTO> getListDocumentTemplate(String searchString, String documentTemplateCode, String documentTemplateName, String documentTemplateDescription,
                                                             Pageable pageable, UUID partnerId) {
        Page<DocumentTemplate> response = documentTemplateRepository.getDocumentTemplateWithPagination(searchString, documentTemplateCode, documentTemplateName, documentTemplateDescription, partnerId, pageable);
        return response.map(template -> {
            DocumentTemplateDTO dto = mapper.map(template, DocumentTemplateDTO.class);

            try {
                if (template.getAttachFileId() != null) {
                    DocumentFiles file = documentFileRepository
                            .findById(template.getAttachFileId())
                            .orElse(null);
                    dto.setAttachFile(file);
                } else {
                    dto.setAttachFile(null);
                }
                dto.setDocumentTypeIds(documentTemplateDocumentTypesRepository.getDocumentTypeIdByDocumentTemplateId(dto.getId()));
            } catch (Exception e) {
                dto.setAttachFile(null);
            }

            return dto;
        });
    }

    public List<DocumentTemplate> getAllDocumentTemplate(UUID partnerId) {
        return documentTemplateRepository.getAllDocumentTemplate(partnerId);
    }

    public DocumentTemplateDetailDTO getDocumentDetail(UUID id, User user) {
        DocumentTemplateDetailDTO dto = new DocumentTemplateDetailDTO();
        DocumentTemplate documentTemplate = documentTemplateRepository.findById(id);

        dto.setId(documentTemplate.getId());
        dto.setVersion(documentTemplate.getVersion());
        dto.setIsActive(documentTemplate.getIsActive());
        dto.setDocumentTemplateCode(documentTemplate.getDocumentTemplateCode());
        dto.setDocumentTemplateName(documentTemplate.getDocumentTemplateName());
        dto.setDocumentTemplateDescription(documentTemplate.getDocumentTemplateDescription());
        DocumentFiles docFile = documentFileRepository.findById(documentTemplate.getAttachFileId())
                .orElseThrow(() -> new RuntimeException("File not found"));
        dto.setAttachFileId(docFile.getId());
        dto.setFileName(docFile.getAttachName());
        String wopiToken = jwtService.generateWopiToken(
                user.getId(),
                docFile.getId(),
                "view",
                user.getFullName()
        );

        String filenameForWopi = docFile.getAttachName();
        String wopiSrc = apiUrl + "/wopi/files/" + filenameForWopi + "?access_token=" + wopiToken;
        String encodedWopiSrc = URLEncoder.encode(wopiSrc, StandardCharsets.UTF_8);
        String url = urlCollaboraOffice + "/browser/dist/cool.html?WOPISrc=" + encodedWopiSrc;
        dto.setWopiUrl(url);

        dto.setDocumentTypes(documentTemplateDocumentTypesRepository.getDocumentTypesByDocumentTemplateId(id));

        dto.setFormSchema(formSchemaRepository.getFormSchemaByTemplateID(id));

        logService.createLog(ActionType.VIEW.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.VIEW_DETAIL_DOC_TEMPLATE.getFunction(), "object", documentTemplate.getDocumentTemplateName()),
                user.getId(), documentTemplate.getId(), user.getPartnerId());
        return dto;
    }


    @Transactional
    public ErrorListResponse checkDeleteMulti(List<DeleteMultiDTO> ids) {
        ErrorListResponse response = new ErrorListResponse();
        List<ErrorListResponse.ErrorResponse> lstObject = new ArrayList<>();
        for (DeleteMultiDTO id : ids) {
            ErrorListResponse.ErrorResponse object = new ErrorListResponse.ErrorResponse();
            object.setId(id.getId());
            DocumentTemplate documentTemplate = documentTemplateRepository.findByDocumentTemplateIdIncludeDeleted(id.getId());
            if(documentTemplate == null) {
                object.setErrorMessage("error.DataChangedReload");
                object.setCode(id.getCode());
                object.setName(id.getName());
            }   else {
                object.setCode(documentTemplate.getDocumentTemplateCode());
                object.setName(documentTemplate.getDocumentTemplateName());
            }
            lstObject.add(object);
        }
        response.setErrors(lstObject);
        response.setTotal(ids.size());
        long countNum = response.getErrors().stream()
                .filter(item -> item.getErrorMessage() != null)
                .count();
        response.setHasError(countNum != 0);
        if (!response.getHasError()) {
            return null;
        }
        return response;
    }

    @Transactional
    public Map<String,Object> getAllowspermission(UUID id) {

        List<DocumentAllowedEditors>  lstEditor = documentAllowedEditorsRepository.findByDocumentId(id);
        List<DocumentAllowedViewers>  lstViewer = documentAllowedViewersRepository.findByDocumentId(id);

        return Map.of("Editors", lstEditor, "Viewers", lstViewer);
    }

}
