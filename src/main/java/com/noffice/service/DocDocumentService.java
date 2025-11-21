package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.DocDocumentDTO;
import com.noffice.dto.NodeDeptUserDTO;
import com.noffice.entity.*;
import com.noffice.enumtype.ActionType;
import com.noffice.enumtype.FunctionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.AttachRepository;
import com.noffice.repository.DocDocumentRepository;
import com.noffice.repository.NodeDeptUserRepository;
import com.noffice.ultils.Constants;
import com.noffice.ultils.FileUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DocDocumentService {
    private final DocDocumentRepository docDocumentRepository;
    private final NodeDeptUserRepository nodeDeptUserRepository;
    private final LogService logService;
    private final AttachRepository attachRepository;

    @Transactional
    public String delete(UUID id, User user, Long version) {
        DocDocument docDocument = docDocumentRepository.findByDocumentId(id);
        if (!Objects.equals(docDocument.getVersion(), version)) {
            return "error.DataChangedReload";
        }
        if (docDocument.getIsDeleted())
            return "error.DocTypeNotExists";
        else {
//			Long countChild = docTypeRepository.countChildDocTypes(DocType.getDocTypeId(), DocType.getPartnerId());
//			if (countChild > 0) {
//				return "error.UnableToDeleteExistingUnitOfSubordinateDocType";
//			}

            docDocument.setIsDeleted(Constants.isDeleted.DELETED);
            docDocument.setDeletedBy(user.getId());
            docDocument.setDeletedAt(LocalDateTime.now());
            DocDocument savedDocType = docDocumentRepository.save(docDocument);
            logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_DOCUMENT.getFunction(), "object", savedDocType.getDocumentTitle()),
                    user.getId(), savedDocType.getId(), user.getPartnerId());
        }
        return "";

    }

    @Transactional
    public String deleteMulti(List<DeleteMultiDTO> ids, User user) {
        for (DeleteMultiDTO id : ids) {
            DocDocument docDocument = docDocumentRepository.findByDocumentId(id.getId());
            if (!Objects.equals(docDocument.getVersion(), id.getVersion())) {
                return "error.DataChangedReload";
            }
            if (docDocument.getIsDeleted()) {
                return "error.DocTypeNotExists";
            } else {
                docDocument.setIsDeleted(Constants.isDeleted.DELETED);
                docDocument.setDeletedBy(user.getId());
                docDocument.setDeletedAt(LocalDateTime.now());
                DocDocument savedDocType = docDocumentRepository.save(docDocument);
                logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_DOCUMENT.getFunction(), "object", savedDocType.getDocumentTitle()),
                        user.getId(), savedDocType.getId(), user.getPartnerId());
            }
        }
        return "";
    }

    @Transactional
    public String lockUnlock(UUID id, User user, Long version) {
        DocDocument docDocument = docDocumentRepository.findByDocumentId(id);
        if (!Objects.equals(docDocument.getVersion(), version)) {
            return "error.DataChangedReload";
        }
        if (docDocument.getIsDeleted())
            return "error.DocTypeNotExists";
        else {
            Boolean newStatus = !docDocument.getIsActive();
            docDocument.setIsActive(newStatus);

            docDocument.setUpdateBy(user.getId());
            docDocument.setUpdateAt(LocalDateTime.now());
            DocDocument savedDoc = docDocumentRepository.save(docDocument);
            logService.createLog(savedDoc.getIsActive() ? ActionType.UNLOCK.getAction() : ActionType.LOCK.getAction(),
                    Map.of("actor", user.getFullName(), "action", savedDoc.getIsActive() ? FunctionType.UNLOCK_DOCUMENT.getFunction() : FunctionType.LOCK_DOCTYPE.getFunction(), "object", savedDoc.getDocumentTitle()),
                    user.getId(), savedDoc.getId(), user.getPartnerId());
        }
        return "";
    }

    @Transactional
    public boolean save(DocDocumentDTO docDocumentDTO, MultipartFile[] files, User token) {
        try {
            DocDocument docDocument =docDocumentRepository.findByDocumentId(docDocumentDTO.getId());
            if(docDocument==null){
                docDocument=new DocDocument();
                docDocument.setCreateAt(LocalDateTime.now());
                docDocument.setCreateBy(token.getId());
            }else {
                docDocument.setUpdateAt(LocalDateTime.now());
                docDocument.setUpdateBy(token.getId());
            }
            docDocument.setDocumentTitle(docDocumentDTO.getDocumentTitle());
            docDocument.setDocTemplateId(docDocumentDTO.getDocTemplateId());
            docDocument.setDocTypeId(docDocumentDTO.getDocTypeId());
            docDocument.setDeptName(docDocumentDTO.getDeptName());
            docDocument.setPurpose(docDocumentDTO.getPurpose());
            docDocument.setFormData(docDocumentDTO.getFormData());
            docDocument.setIsActive(docDocumentDTO.getIsActive());
            docDocument.setIsDeleted(Constants.isDeleted.ACTIVE);
            docDocument.setPartnerId(token.getPartnerId());
            DocDocument saveDocument = docDocumentRepository.save(docDocument);
            if(docDocumentDTO.getApprovalSteps()!=null){
                for (NodeDeptUserDTO ndu:docDocumentDTO.getApprovalSteps()) {
                    NodeDeptUser nodeDeptUser = new NodeDeptUser();
                    if(ndu.getId()!=null){
                        Optional<NodeDeptUser> optional = nodeDeptUserRepository.findById(ndu.getId());
                        if(optional.isPresent()){
                            nodeDeptUser=optional.get();
                        }
                    }
                    nodeDeptUser.setDocId(saveDocument.getId());
                    nodeDeptUser.setStep(Integer.valueOf(ndu.getStep()));
                    nodeDeptUser.setUserId(ndu.getUserId());
                    nodeDeptUser.setDeptName(ndu.getDeptName());
                    nodeDeptUser.setRoleId(ndu.getRoleId());
                    nodeDeptUser.setApproveType(ndu.getApprovalType());
                    nodeDeptUser.setNote(ndu.getNote());
                    nodeDeptUserRepository.save(nodeDeptUser);
                }
            }
            if(files!=null&&files.length>0){
               List<Attachs>lstAttach= FileUtils.saveFile(files,token);
               for(Attachs attachs:lstAttach){
                   attachs.setObjectId(docDocument.getId());
                   attachs.setObjectType(Constants.OBJECT_TYPE.DOC_DOCUMENT);
                   attachs.setCreatorId(token.getId());
                   attachs.setIsActive(docDocumentDTO.getIsActive());
                   attachs.setIsDeleted(Constants.isDeleted.ACTIVE);
                   attachs.setPartnerId(token.getPartnerId());
                   attachs.setCreateBy(token.getId());
                   attachRepository.save(attachs);
               }
            }
            if(docDocumentDTO.getRemovedFiles() != null){
                for(UUID attachId:docDocumentDTO.getRemovedFiles()){
                    Optional<Attachs>optional=attachRepository.findById(attachId);
                    if(optional.isPresent()){
                        Attachs attachs=optional.get();
                        attachs.setIsDeleted(true);
                        attachRepository.save(attachs);
                    }
                }
            }

            logService.createLog(ActionType.CREATE.getAction(), Map.of("actor", token.getFullName(), "action", FunctionType.CREATE_DOCUMENT.getFunction(), "object", saveDocument.getDocumentTitle()),
                    token.getId(), saveDocument.getId(), token.getPartnerId());
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Transactional
    public String update(DocDocument docDocumentDTO, Authentication authentication) {
        User token = (User) authentication.getPrincipal();
        DocDocument document = docDocumentRepository.findByDocumentId(docDocumentDTO.getId());
        if (!Objects.equals(docDocumentDTO.getVersion(), document.getVersion())) {
            return "error.DataChangedReload";
        }
        if (document.getIsDeleted())
            return "error.DocTypeNotExists";
        else {
            document.setDocumentTitle(docDocumentDTO.getDocumentTitle());
            document.setIsActive(docDocumentDTO.getIsActive());
            document.setPartnerId(token.getPartnerId());
            document.setUpdateAt(LocalDateTime.now());
            document.setUpdateBy(token.getId());
            DocDocument savedDocDocument = docDocumentRepository.save(document);
            logService.createLog(ActionType.UPDATE.getAction(), Map.of("actor", token.getFullName(), "action", FunctionType.EDIT_DOCUMENT.getFunction(), "object", savedDocDocument.getDocumentTitle()),
                    token.getId(), savedDocDocument.getId(), token.getPartnerId());
        }
        return "";
    }

    public Page<DocDocumentDTO> getListDoc(String searchString, String DocTypeCode, String DocTypeName, String DocTypeDescription,
                                        Pageable pageable, UUID partnerId) {

        return docDocumentRepository.getDocWithPagination(searchString, DocTypeCode, DocTypeName, partnerId, pageable);
    }

    public List<DocType> getAllDocType(UUID partnerId) {
        return docDocumentRepository.getAllDocType(partnerId);
    }

    public void getLogDetailDocType(String id, User user) {
        DocType docType = docDocumentRepository.findByDocTypeCode(id);
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
            DocDocument document = docDocumentRepository.findByDocumentId(id.getId());
            if (!Objects.equals(document.getVersion(), id.getVersion())) {
                object.setErrorMessage("error.DataChangedReload");
            } else if (document.getIsDeleted()) {
                object.setErrorMessage("error.DocTypeNotExists");
            }
            object.setCode(document.getDocumentTitle());
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

    public List<Attachs> getAttachsByDocument(UUID docId,Integer objectType){
       return attachRepository.getListAttachs(docId,objectType);
    }

    public List<NodeDeptUser> getByDocId(UUID docId){
        return nodeDeptUserRepository.getByDocId(docId);
    }
}
