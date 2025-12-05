package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.ContractType;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.enumtype.FunctionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.ContractTypeRepository;
import com.noffice.ultils.Constants;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
        if (contractType == null || !Objects.equals(contractType.getVersion(), version)) {
            return Constants.errorResponse.DATA_CHANGED;
        }else {

            contractTypeRepository.deleteContractTypeByContractTypeId(id);
            logService.createLog(ActionType.DELETE.getAction(), Map.of(Constants.logResponse.ACTOR, user.getFullName(), Constants.logResponse.ACTION, FunctionType.DELETE_CONTRACTTYPE.getFunction(), Constants.logResponse.OBJECT, contractType.getContractTypeName()),
                    user.getId(), contractType.getId(), user.getPartnerId());
        }
        return "";

    }

    @Transactional
    public String deleteMultiContractType(List<DeleteMultiDTO> ids, User user) {
        for (DeleteMultiDTO id : ids) {
            ContractType contractType = contractTypeRepository.findByContractTypeIdIncludeDeleted(id.getId());
            if (contractType == null || !Objects.equals(contractType.getVersion(), id.getVersion())) {
                return Constants.errorResponse.DATA_CHANGED;
            } else {
                contractTypeRepository.deleteContractTypeByContractTypeId(id.getId());
                logService.createLog(ActionType.DELETE.getAction(), Map.of(Constants.logResponse.ACTOR, user.getFullName(), Constants.logResponse.ACTION, FunctionType.DELETE_CONTRACTTYPE.getFunction(), Constants.logResponse.OBJECT, contractType.getContractTypeName()),
                        user.getId(), contractType.getId(), user.getPartnerId());
            }
        }
        return "";
    }

    @Transactional
    public String lockUnlockContractType(UUID id, User user, Long version) {
        ContractType contractType = contractTypeRepository.findByContractTypeIdIncludeDeleted(id);
        if (contractType == null || !Objects.equals(contractType.getVersion(), version)) {
            return Constants.errorResponse.DATA_CHANGED;
        } else {
            Boolean newStatus = !contractType.getIsActive();
            contractType.setIsActive(newStatus);

            contractType.setUpdateBy(user.getId());
            contractType.setUpdateAt(LocalDateTime.now());
            ContractType savedContractType = contractTypeRepository.save(contractType);
            logService.createLog(Boolean.TRUE.equals(savedContractType.getIsActive()) ? ActionType.UNLOCK.getAction() : ActionType.LOCK.getAction(),
                    Map.of(Constants.logResponse.ACTOR, user.getFullName(), Constants.logResponse.ACTION, Boolean.TRUE.equals(savedContractType.getIsActive()) ? FunctionType.UNLOCK_CONTRACTTYPE.getFunction() : FunctionType.LOCK_CONTRACTTYPE.getFunction(), Constants.logResponse.OBJECT, savedContractType.getContractTypeName()),
                    user.getId(), savedContractType.getId(), user.getPartnerId());
        }
        return "";
    }

    @Transactional
    public String saveContractType(ContractType contractTypeDTO, User token) {
        if (contractTypeRepository.findByCode(contractTypeDTO.getContractTypeCode(), token.getPartnerId()) == null) {
            ContractType contractType = new ContractType();
            contractType.setContractTypeName(contractTypeDTO.getContractTypeName());
            contractType.setContractTypeCode(contractTypeDTO.getContractTypeCode());
            contractType.setContractTypeDescription(contractTypeDTO.getContractTypeDescription());
            contractType.setCreateAt(LocalDateTime.now());
            contractType.setCreateBy(token.getId());
            contractType.setIsActive(contractTypeDTO.getIsActive());
            contractType.setIsDeleted(Constants.isDeleted.ACTIVE);
            contractType.setPartnerId(token.getPartnerId());
            ContractType savedContractType = contractTypeRepository.save(contractType);
            logService.createLog(ActionType.CREATE.getAction(), Map.of(Constants.logResponse.ACTOR, token.getFullName(), Constants.logResponse.ACTION, FunctionType.CREATE_CONTRACTTYPE.getFunction(), Constants.logResponse.OBJECT, savedContractType.getContractTypeName()),
                    token.getId(), savedContractType.getId(), token.getPartnerId());

            return "";
        } else {
            return "error.ContractTypeExists";
        }
    }

    @Transactional
    public String updateContractType(ContractType contractTypeDTO, User token) {
        ContractType contractType = contractTypeRepository.findByContractTypeIdIncludeDeleted(contractTypeDTO.getId());
        if (contractType == null || !Objects.equals(contractType.getVersion(), contractTypeDTO.getVersion())) {
            return Constants.errorResponse.DATA_CHANGED;
        } else {
            contractType.setContractTypeName(contractTypeDTO.getContractTypeName());
            contractType.setContractTypeCode(contractTypeDTO.getContractTypeCode());
            contractType.setContractTypeDescription(contractTypeDTO.getContractTypeDescription());
            contractType.setIsActive(contractTypeDTO.getIsActive());
            contractType.setPartnerId(token.getPartnerId());
            contractType.setUpdateAt(LocalDateTime.now());
            contractType.setUpdateBy(token.getId());
            ContractType savedContractType = contractTypeRepository.save(contractType);
            logService.createLog(ActionType.UPDATE.getAction(), Map.of(Constants.logResponse.ACTOR, token.getFullName(), Constants.logResponse.ACTION, FunctionType.EDIT_CONTRACTTYPE.getFunction(), Constants.logResponse.OBJECT, savedContractType.getContractTypeName()),
                    token.getId(), savedContractType.getId(), token.getPartnerId());
        }
        return "";
    }

    public Page<ContractType> getListContractType(String searchString, String contractTypeCode, String contractTypeName, String contractTypeDescription,
                                        Pageable pageable, UUID partnerId) {
        return contractTypeRepository.getContractTypeWithPagination(searchString, contractTypeCode, contractTypeName, contractTypeDescription, partnerId, pageable);
    }

    public List<ContractType> getAllContractType(UUID partnerId) {
        return contractTypeRepository.getAllContractType(partnerId);
    }

    public void getLogDetailContractType(String id, User user) {
        ContractType contractType = contractTypeRepository.findByContractTypeCode(id);
        logService.createLog(ActionType.VIEW.getAction(), Map.of(Constants.logResponse.ACTOR, user.getFullName(), Constants.logResponse.ACTION, FunctionType.VIEW_DETAIL_CONTRACTTYPE.getFunction(), Constants.logResponse.OBJECT, contractType.getContractTypeName()),
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
            if(contractType == null) {
                object.setErrorMessage(Constants.errorResponse.DATA_CHANGED);
                object.setCode(id.getCode());
                object.setName(id.getName());
            }   else {
                object.setCode(contractType.getContractTypeCode());
                object.setName(contractType.getContractTypeName());
            }

            lstObject.add(object);
        }
        response.setErrors(lstObject);
        response.setTotal(ids.size());
        long countNum = response.getErrors().stream()
                .filter(item -> item.getErrorMessage() != null)
                .count();
        response.setHasError(countNum != 0);
        if (Boolean.FALSE.equals(response.getHasError())) {
            return null;
        }
        return response;
    }

}
