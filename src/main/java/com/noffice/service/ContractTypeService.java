package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.ContractType;
import com.noffice.entity.User;
import com.noffice.enumType.ActionType;
import com.noffice.enumType.FunctionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.ContractTypeRepository;
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
public class ContractTypeService {
    private final ContractTypeRepository contractTypeRepository;
    private final LogService logService;

    @Transactional
    public String deleteContractType(UUID id, User user, Long version) {
        ContractType contractType = contractTypeRepository.findByContractTypeIdIncludeDeleted(id);
        if (!Objects.equals(contractType.getVersion(), version)) {
            return "error.DataChangedReload";
        }
        if (contractType.getIsDeleted())
            return "error.ContractTypeNotExists";
        else {
//			Long countChild = contractTypeRepository.countChildContractTypes(ContractType.getContractTypeId(), ContractType.getPartnerId());
//			if (countChild > 0) {
//				return "error.UnableToDeleteExistingUnitOfSubordinateContractType";
//			}

            contractType.setIsDeleted(Constants.IS_DELETED.DELETED);
            contractType.setDeletedBy(user.getId());
            contractType.setDeletedAt(LocalDateTime.now());
            ContractType savedContractType = contractTypeRepository.save(contractType);
            logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_CONTRACTTYPE.getFunction(), "object", savedContractType.getContractTypeName()),
                    user.getId(), savedContractType.getId(), user.getPartnerId());
        }
        return "";

    }

    @Transactional
    public String deleteMultiContractType(List<DeleteMultiDTO> ids, User user) {
        for (DeleteMultiDTO id : ids) {
            ContractType contractType = contractTypeRepository.findByContractTypeIdIncludeDeleted(id.getId());
            if (!Objects.equals(contractType.getVersion(), id.getVersion())) {
                return "error.DataChangedReload";
            }
            if (contractType.getIsDeleted()) {
                return "error.ContractTypeNotExists";
            } else {
                contractType.setIsDeleted(Constants.IS_DELETED.DELETED);
                contractType.setDeletedBy(user.getId());
                contractType.setDeletedAt(LocalDateTime.now());
                ContractType savedContractType = contractTypeRepository.save(contractType);
                logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_CONTRACTTYPE.getFunction(), "object", savedContractType.getContractTypeName()),
                        user.getId(), savedContractType.getId(), user.getPartnerId());
            }
        }
        return "";
    }

    @Transactional
    public String lockUnlockContractType(UUID id, User user, Long version) {
        ContractType contractType = contractTypeRepository.findByContractTypeIdIncludeDeleted(id);
        if (!Objects.equals(contractType.getVersion(), version)) {
            return "error.DataChangedReload";
        }
        if (contractType.getIsDeleted())
            return "error.ContractTypeNotExists";
        else {
            Boolean newStatus = !contractType.getIsActive();
            contractType.setIsActive(newStatus);

            contractType.setUpdateBy(user.getId());
            contractType.setUpdateAt(LocalDateTime.now());
            ContractType savedContractType = contractTypeRepository.save(contractType);
            logService.createLog(savedContractType.getIsActive() ? ActionType.UNLOCK.getAction() : ActionType.LOCK.getAction(),
                    Map.of("actor", user.getFullName(), "action", savedContractType.getIsActive() ? FunctionType.UNLOCK_CONTRACTTYPE.getFunction() : FunctionType.LOCK_CONTRACTTYPE.getFunction(), "object", savedContractType.getContractTypeName()),
                    user.getId(), savedContractType.getId(), user.getPartnerId());
        }
        return "";
    }

    @Transactional
    public String saveContractType(ContractType ContractTypeDTO, Authentication authentication) {
        User token = (User) authentication.getPrincipal();
        if (contractTypeRepository.findByCode(ContractTypeDTO.getContractTypeCode(), token.getPartnerId()) == null) {
            ContractType ContractType = new ContractType();
            ContractType.setContractTypeName(ContractTypeDTO.getContractTypeName());
            ContractType.setContractTypeCode(ContractTypeDTO.getContractTypeCode());
            ContractType.setContractTypeDescription(ContractTypeDTO.getContractTypeDescription());
            ContractType.setCreateAt(LocalDateTime.now());
            ContractType.setCreateBy(token.getId());
            ContractType.setIsActive(ContractTypeDTO.getIsActive());
            ContractType.setIsDeleted(Constants.IS_DELETED.ACTIVE);
            ContractType.setPartnerId(token.getPartnerId());
            ContractType savedContractType = contractTypeRepository.save(ContractType);
            logService.createLog(ActionType.CREATE.getAction(), Map.of("actor", token.getFullName(), "action", FunctionType.CREATE_CONTRACTTYPE.getFunction(), "object", savedContractType.getContractTypeName()),
                    token.getId(), savedContractType.getId(), token.getPartnerId());

            return "";
        } else {
            return "error.ContractTypeNotExists";
        }
    }

    @Transactional
    public String updateContractType(ContractType contractTypeDTO, Authentication authentication) {
        User token = (User) authentication.getPrincipal();
        ContractType contractType = contractTypeRepository.findByContractTypeIdIncludeDeleted(contractTypeDTO.getId());
        if (!Objects.equals(contractType.getVersion(), contractTypeDTO.getVersion())) {
            return "error.DataChangedReload";
        }
        if (contractType.getIsDeleted())
            return "error.ContractTypeNotExists";
        else {
            contractType.setContractTypeName(contractTypeDTO.getContractTypeName());
            contractType.setContractTypeCode(contractTypeDTO.getContractTypeCode());
            contractType.setContractTypeDescription(contractTypeDTO.getContractTypeDescription());
            contractType.setIsActive(contractTypeDTO.getIsActive());
            contractType.setPartnerId(token.getPartnerId());
            contractType.setUpdateAt(LocalDateTime.now());
            contractType.setUpdateBy(token.getId());
            ContractType savedContractType = contractTypeRepository.save(contractType);
            logService.createLog(ActionType.UPDATE.getAction(), Map.of("actor", token.getFullName(), "action", FunctionType.EDIT_CONTRACTTYPE.getFunction(), "object", savedContractType.getContractTypeName()),
                    token.getId(), savedContractType.getId(), token.getPartnerId());
        }
        return "";
    }

    public Page<ContractType> getListContractType(String searchString, String ContractTypeCode, String ContractTypeName, String ContractTypeDescription,
                                        Pageable pageable, UUID partnerId) {
        return contractTypeRepository.getContractTypeWithPagination(searchString, ContractTypeCode, ContractTypeName, ContractTypeDescription, partnerId, pageable);
    }

    public List<ContractType> getAllContractType(UUID partnerId) {
        return contractTypeRepository.getAllContractType(partnerId);
    }

    public void LogDetailContractType(String id, User user) {
        ContractType contractType = contractTypeRepository.findByContractTypeCode(id);
        logService.createLog(ActionType.VIEW.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.VIEW_DETAIL_CONTRACTTYPE.getFunction(), "object", contractType.getContractTypeName()),
                user.getId(), contractType.getId(), user.getPartnerId());
    }


    @Transactional
    public ErrorListResponse checkDeleteMulti(List<DeleteMultiDTO> ids) {
        ErrorListResponse response = new ErrorListResponse();
        List<ErrorListResponse.ErrorResponse> lstObject = new ArrayList<>();
        for (DeleteMultiDTO id : ids) {
            ErrorListResponse.ErrorResponse object = new ErrorListResponse.ErrorResponse();
            object.setId(id.getId());
            ContractType contractType = contractTypeRepository.findByContractTypeIdIncludeDeleted(id.getId());
            if (!Objects.equals(contractType.getVersion(), id.getVersion())) {
                object.setErrorMessage("error.DataChangedReload");
            } else if (contractType.getIsDeleted()) {
                object.setErrorMessage("error.ContractTypeNotExists");
            }
            object.setCode(contractType.getContractTypeCode());
            object.setName(contractType.getContractTypeName());
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
