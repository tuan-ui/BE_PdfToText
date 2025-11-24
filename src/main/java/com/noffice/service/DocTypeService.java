package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.DocType;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.enumtype.FunctionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.DocTypeRepository;
import com.noffice.repository.DocumentTemplateDocumentTypesRepository;
import com.noffice.ultils.Constants;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DocTypeService {
    private final DocTypeRepository docTypeRepository;
    private final LogService logService;
    private final DocumentTemplateDocumentTypesRepository documentTemplateDocumentTypesRepository;

    @Transactional
    public String deleteDocType(UUID id, User user, Long version) {
        DocType docType = docTypeRepository.findByDocTypeIdIncludeDeleted(id);
        if (docType == null || !Objects.equals(docType.getVersion(), version)) {
            return "error.DataChangedReload";
        } else {
			if (documentTemplateDocumentTypesRepository.existsDocumentTemplateByDocumentTypeId(id)) {
				return "error.UnableToDeleteExistingDocTemplate";
			}
            docTypeRepository.deleteDocTypeByDocTypeId(id);
            logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_DOCTYPE.getFunction(), "object", docType.getDocTypeName()),
                    user.getId(), docType.getId(), user.getPartnerId());
        }
        return "";

    }

    @Transactional
    public String deleteMultiDocType(List<DeleteMultiDTO> ids, User user) {
        for (DeleteMultiDTO id : ids) {
            DocType docType = docTypeRepository.findByDocTypeIdIncludeDeleted(id.getId());
            if (docType == null || !Objects.equals(docType.getVersion(), id.getVersion())) {
                return "error.DataChangedReload";
            }  else {
                if (documentTemplateDocumentTypesRepository.existsDocumentTemplateByDocumentTypeId(id.getId())) {
                    return "error.UnableToDeleteExistingDocTemplate";
                }
                docTypeRepository.deleteDocTypeByDocTypeId(id.getId());
                logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_DOCTYPE.getFunction(), "object", docType.getDocTypeName()),
                        user.getId(), docType.getId(), user.getPartnerId());
            }
        }
        return "";
    }

    @Transactional
    public String lockUnlockDocType(UUID id, User user, Long version) {
        DocType docType = docTypeRepository.findByDocTypeIdIncludeDeleted(id);
        if (docType == null || !Objects.equals(docType.getVersion(), version)) {
            return "error.DataChangedReload";
        } else {
            Boolean newStatus = !docType.getIsActive();
            docType.setIsActive(newStatus);
            docType.setUpdateBy(user.getId());
            docType.setUpdateAt(LocalDateTime.now());
            DocType savedDocType = docTypeRepository.save(docType);
            logService.createLog(savedDocType.getIsActive() ? ActionType.UNLOCK.getAction() : ActionType.LOCK.getAction(),
                    Map.of("actor", user.getFullName(), "action", savedDocType.getIsActive() ? FunctionType.UNLOCK_DOCTYPE.getFunction() : FunctionType.LOCK_DOCTYPE.getFunction(), "object", savedDocType.getDocTypeName()),
                    user.getId(), savedDocType.getId(), user.getPartnerId());
        }
        return "";
    }

    @Transactional
    public String saveDocType(DocType docTypeDTO, Authentication authentication) {
        User token = (User) authentication.getPrincipal();
        if (docTypeRepository.findByCode(docTypeDTO.getDocTypeCode(), token.getPartnerId()) == null) {
            DocType DocType = new DocType();
            DocType.setDocTypeName(docTypeDTO.getDocTypeName());
            DocType.setDocTypeCode(docTypeDTO.getDocTypeCode());
            DocType.setDocTypeDescription(docTypeDTO.getDocTypeDescription());
            DocType.setCreateAt(LocalDateTime.now());
            DocType.setCreateBy(token.getId());
            DocType.setIsActive(docTypeDTO.getIsActive());
            DocType.setIsDeleted(Constants.isDeleted.ACTIVE);
            DocType.setPartnerId(token.getPartnerId());
            DocType savedDocType = docTypeRepository.save(DocType);
            logService.createLog(ActionType.CREATE.getAction(), Map.of("actor", token.getFullName(), "action", FunctionType.CREATE_DOCTYPE.getFunction(), "object", savedDocType.getDocTypeName()),
                    token.getId(), savedDocType.getId(), token.getPartnerId());

            return "";
        } else {
            return "error.DocTypeExists";
        }
    }

    @Transactional
    public String updateDocType(DocType docTypeDTO, Authentication authentication) {
        User token = (User) authentication.getPrincipal();
        DocType docType = docTypeRepository.findByDocTypeIdIncludeDeleted(docTypeDTO.getId());
        if (docType == null || !Objects.equals(docType.getVersion(), docTypeDTO.getVersion())) {
            return "error.DataChangedReload";
        } else {
            docType.setDocTypeName(docTypeDTO.getDocTypeName());
            docType.setDocTypeCode(docTypeDTO.getDocTypeCode());
            docType.setDocTypeDescription(docTypeDTO.getDocTypeDescription());
            docType.setIsActive(docTypeDTO.getIsActive());
            docType.setPartnerId(token.getPartnerId());
            docType.setUpdateAt(LocalDateTime.now());
            docType.setUpdateBy(token.getId());
            DocType savedDocType = docTypeRepository.save(docType);
            logService.createLog(ActionType.UPDATE.getAction(), Map.of("actor", token.getFullName(), "action", FunctionType.EDIT_DOCTYPE.getFunction(), "object", savedDocType.getDocTypeName()),
                    token.getId(), savedDocType.getId(), token.getPartnerId());
        }
        return "";
    }

    public Page<DocType> getListDocType(String searchString, String docTypeCode, String docTypeName, String docTypeDescription,
                                        Pageable pageable, UUID partnerId) {
        return docTypeRepository.getDocTypeWithPagination(searchString, docTypeCode, docTypeName, docTypeDescription, partnerId, pageable);
    }

    public List<DocType> getAllDocType(UUID partnerId) {
        return docTypeRepository.getAllDocType(partnerId);
    }

    public void getLogDetailDocType(String id, User user) {
        DocType docType = docTypeRepository.findByDocTypeCode(id);
        logService.createLog(ActionType.VIEW.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.VIEW_DETAIL_DOCTYPE.getFunction(), "object", docType.getDocTypeName()),
                user.getId(), docType.getId(), user.getPartnerId());
    }


    @Transactional
    public ErrorListResponse checkDeleteMulti(List<DeleteMultiDTO> ids) {
        ErrorListResponse response = new ErrorListResponse();
        List<ErrorListResponse.ErrorResponse> lstObject = new ArrayList<>();
        for (DeleteMultiDTO id : ids) {
            ErrorListResponse.ErrorResponse object = new ErrorListResponse.ErrorResponse();
            object.setId(id.getId());
            DocType docType = docTypeRepository.findByDocTypeIdIncludeDeleted(id.getId());
            if(docType == null) {
                object.setErrorMessage("error.DataChangedReload");
                object.setCode(id.getCode());
                object.setName(id.getName());
            }   else {
                object.setCode(docType.getDocTypeCode());
                object.setName(docType.getDocTypeName());;
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

}
