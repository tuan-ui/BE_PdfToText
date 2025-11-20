package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.Domain;
import com.noffice.entity.User;
import com.noffice.enumType.ActionType;
import com.noffice.enumType.FunctionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.DomainRepository;
import com.noffice.ultils.Constants;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class DomainService {
	private final DomainRepository domainRepository;
	private final LogService logService;


	@Transactional
	public String deleteDomain(UUID id, User user, Long version) {
		Domain domain = domainRepository.findByDomainIdIncludeDeleted(id);
		if (!Objects.equals(domain.getVersion(), version)) {
			return "error.DataChangedReload";
		}
		if (domain.getIsDeleted())
			return "error.RoleCodeNotExists";
		else {
			//			Long countChild = domainRepository.countChildDomains(domain.getDomainId(), domain.getPartnerId());
//			if (countChild > 0) {
//				return "error.UnableToDeleteExistingUnitOfSubordinateDomain";
//			}
			domain.setIsDeleted(Constants.IS_DELETED.DELETED);
			domain.setDeletedBy(user.getId());
			domain.setDeletedAt(LocalDateTime.now());
			Domain savedDomain = domainRepository.save(domain);
			logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_DOMAIN.getFunction(), "object", savedDomain.getDomainName()),
					user.getId(), savedDomain.getId(), user.getPartnerId());
		}
		return"";
	}

	@Transactional
	public String deleteMultiDomain(List<DeleteMultiDTO> ids, User user) {
		for(DeleteMultiDTO id :ids) {
			Domain domain = domainRepository.findByDomainIdIncludeDeleted(id.getId());
			if (!Objects.equals(domain.getVersion(), id.getVersion())) {
				return "error.DataChangedReload";
			}
			if (domain.getIsDeleted())
				return "error.RoleCodeNotExists";
			else {
				//				Long countChild = domainRepository.countChildDomains(domain.getDomainId(), domain.getPartnerId());
//				if (countChild > 0) {
//					return "error.UnableToDeleteExistingUnitOfSubordinateDomain";
//				}

				domain.setIsDeleted(Constants.IS_DELETED.DELETED);
				domain.setDeletedBy(user.getId());
				domain.setDeletedAt(LocalDateTime.now());
				Domain savedDomain = domainRepository.save(domain);
				logService.createLog(ActionType.DELETE.getAction(), Map.of("actor", user.getFullName(), "action", FunctionType.DELETE_DOMAIN.getFunction(), "object", savedDomain.getDomainName()),
						user.getId(), savedDomain.getId(), user.getPartnerId());
			}
		}
		return "";
	}

	@Transactional
	public String lockUnlockDomain(UUID id, User user, Long version) {
		Domain domain = domainRepository.findByDomainIdIncludeDeleted(id);
		if (!Objects.equals(domain.getVersion(), version)) {
			return "error.DataChangedReload";
		}
		if (domain.getIsDeleted())
			return "error.RoleCodeNotExists";
		else {
			Boolean newStatus = !domain.getIsActive();
			domain.setIsActive(newStatus);

			domain.setUpdateBy(user.getId());
			domain.setUpdateAt(LocalDateTime.now());
			Domain savedDomain = domainRepository.save(domain);
			logService.createLog(savedDomain.getIsActive() ? ActionType.UNLOCK.getAction() : ActionType.LOCK.getAction(),
					Map.of("actor", user.getFullName(), "action", savedDomain.getIsActive() ? FunctionType.UNLOCK_DOMAIN.getFunction() : FunctionType.LOCK_DOMAIN.getFunction(), "object", savedDomain.getDomainName()),
					user.getId(), savedDomain.getId(), user.getPartnerId());
		}
		return "";
	}

	@Transactional
	public String saveDomain(Domain domainDTO, Authentication authentication) {
		User token = (User) authentication.getPrincipal();
		if(domainRepository.findByCode(domainDTO.getDomainCode(), token.getPartnerId())==null){
			Domain domain = new Domain();
			domain.setDomainName(domainDTO.getDomainName());
			domain.setDomainCode(domainDTO.getDomainCode());
			domain.setDomainDescription(domainDTO.getDomainDescription());
			domain.setCreateAt(LocalDateTime.now());
			domain.setCreateBy(token.getId());
			domain.setIsActive(domainDTO.getIsActive());
			domain.setIsDeleted(Constants.IS_DELETED.ACTIVE);
			domain.setPartnerId(token.getPartnerId());
			Domain savedDomain = domainRepository.save(domain);
			logService.createLog(ActionType.CREATE.getAction(), Map.of("actor", token.getFullName(),"action", FunctionType.CREATE_DOMAIN.getFunction(), "object", savedDomain.getDomainName()),
					token.getId(), savedDomain.getId(),token.getPartnerId());

			return "";
		} else {
			return "error.DomainNotExists";
		}
	}

	@Transactional
	public String updateDomain(Domain domainDTO, Authentication authentication) {
		User token = (User) authentication.getPrincipal();
		Domain domain = domainRepository.findByDomainIdIncludeDeleted(domainDTO.getId());
		if (!Objects.equals(domain.getVersion(), domainDTO.getVersion())) {
			return  "error.DataChangedReload";
		}
		if(domain.getIsDeleted())
			return	"error.DomainNotExists";
		else {
			domain.setDomainName(domainDTO.getDomainName());
			domain.setDomainCode(domainDTO.getDomainCode());
			domain.setDomainDescription(domainDTO.getDomainDescription());
			domain.setIsActive(domainDTO.getIsActive());
			domain.setPartnerId(token.getPartnerId());
			domain.setUpdateAt(LocalDateTime.now());
			domain.setUpdateBy(token.getId());
			Domain savedDomain = domainRepository.save(domain);
			logService.createLog(ActionType.UPDATE.getAction(), Map.of("actor", token.getFullName(),"action", FunctionType.EDIT_DOMAIN.getFunction(), "object", savedDomain.getDomainName()),
					token.getId(), savedDomain.getId(),token.getPartnerId());
		}
		return "";
	}

	public Page<Domain> getListDomain(String searchString, String domainCode, String domainName, String domainDescription,
														 Pageable pageable, UUID partnerId) {
        return domainRepository.getDomainWithPagination(searchString, domainCode, domainName, domainDescription, partnerId, pageable);
	}

	public List<Domain> getAllDomain(UUID partnerId) {
		return domainRepository.getAllDomain(partnerId);
	}

	public void LogDetailDomain(String id, User user) {
		Domain domain = domainRepository.findByDomainCode(id);
		logService.createLog(ActionType.VIEW.getAction(), Map.of("actor", user.getFullName(),"action", FunctionType.VIEW_DETAIL_DOMAIN.getFunction(), "object", domain.getDomainName()),
				user.getId(), domain.getId(),user.getPartnerId());
	}

	@Transactional
	public ErrorListResponse checkDeleteMulti(List<DeleteMultiDTO> ids) {
		ErrorListResponse response = new ErrorListResponse();
		List<ErrorListResponse.ErrorResponse> lstObject = new ArrayList<>();
		for(DeleteMultiDTO id : ids) {
			ErrorListResponse.ErrorResponse object = new ErrorListResponse.ErrorResponse();
			object.setId(id.getId());
			Domain role = domainRepository.findByDomainIdIncludeDeleted(id.getId());
			if (!Objects.equals(role.getVersion(), id.getVersion())) {
				object.setErrorMessage("error.DataChangedReload");
			} else if (role.getIsDeleted()) {
				object.setErrorMessage("error.DomainNotExists");
			}
			object.setCode(role.getDomainCode());
			object.setName(role.getDomainName());
			lstObject.add(object);
		}
		response.setErrors(lstObject);
		response.setTotal(ids.size());
		long countNum = response.getErrors().stream()
				.filter(item -> item.getErrorMessage()!=null)
				.count();
		response.setHasError(countNum != 0);
		if(!response.getHasError())
		{
			return null;
		}
		return response;
	}

}
