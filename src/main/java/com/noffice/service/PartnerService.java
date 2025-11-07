package com.noffice.service;

import java.time.LocalDateTime;
import java.util.*;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.*;
import com.noffice.enumType.ActionType;
import com.noffice.enumType.FunctionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.noffice.dto.PartnerRequest;
import com.noffice.repository.PartnerRepository;
import com.noffice.ultils.Constants;

import jakarta.transaction.Transactional;

@Service
public class PartnerService {
	@Autowired
	private PartnerRepository partnerRepository;
	@Autowired
	private LogService logService;
    @Autowired
    private UserRepository userRepository;

	public Page<Partners> searchPartners(PartnerRequest partnerRequest, Pageable pageable) {
        return partnerRepository.searchPartners(partnerRequest.getSearchString(),partnerRequest.getPartnerName(),partnerRequest.getEmail(),
				partnerRequest.getPhone(),partnerRequest.getAddress(), pageable);
    }

	@Transactional
	public Partners createPartner(PartnerRequest partner, User token) {
		 Partners save = new Partners();
		 save.setAddress(partner.getAddress());
		 save.setPartnerName(partner.getPartnerName());
		 save.setEmail(partner.getEmail());
		 save.setWebsite(partner.getWebsite());
		 save.setPhone(partner.getPhone());
		 save.setTaxCode(partner.getTaxCode());
		 save.setPartnerCode(partner.getPartnerCode());
		 save.setImgLogo(partner.getBase64Image());
		 save.setFax(partner.getFax());

		save.setCreateAt(LocalDateTime.now());
		save.setCreateBy(token.getId());
		save.setIsActive(true);
		save.setIsDeleted(Constants.IS_DELETED.ACTIVE);
		 partnerRepository.save(save);
         logService.createLog(ActionType.CREATE.getAction(),
				 Map.of("actor", token.getFullName(), "action",FunctionType.CREATE_PARTNER.getFunction(),
						 "object", save.getPartnerName()),
				 token.getId(), save.getId(), token.getPartnerId());
        return save;
    }

	@Transactional
	public String deletePartner(UUID partnerId,User userDetails, Long version) {
		Partners partner = partnerRepository.getPartnerByIdIncluideDeleted(partnerId);
		if (!Objects.equals(partner.getVersion(), version)) {
			return  "error.DataChangedReload";
		}
		if(partner.getIsDeleted())
			return	"error.PartnerDoesNotExist";
		else {
			if (userRepository.existsUserByPartnerId(partnerId) != 0) {
				return "error.PartnerAlreadyUseOnUser";
			}
			partner.setIsDeleted(Constants.IS_DELETED.DELETED);
			partner.setDeletedAt(LocalDateTime.now());
			partner.setDeletedBy(userDetails.getId());
			partnerRepository.save(partner);
			logService.createLog(ActionType.DELETE.getAction(),
					Map.of("actor", userDetails.getFullName(), "action", FunctionType.DELETE_PARTNER.getFunction(), "object", partner.getPartnerName()),
					userDetails.getId(), partner.getId(), userDetails.getPartnerId());
		}
		return "";
	}
	@Transactional
	public String lockPartner(UUID partnerId,User userDetails,  Long version) {
		Partners partner = partnerRepository.getPartnerByIdIncluideDeleted(partnerId);
		if (!Objects.equals(partner.getVersion(), version)) {
			return  "error.DataChangedReload";
		}
		if(partner.getIsDeleted())
			return	"error.PartnerDoesNotExist";
		else {
			if (userRepository.existsUserByPartnerId(partnerId) != 0) {
				return "error.PartnerAlreadyUseOnUser";
			}
				partner.setIsActive(!partner.getIsActive());
				partner.setUpdateAt(LocalDateTime.now());
				partner.setUpdateBy(userDetails.getId());
				partnerRepository.save(partner);
				logService.createLog(partner.getIsActive() ? ActionType.UNLOCK.getAction() : ActionType.LOCK.getAction(),
						Map.of("actor", userDetails.getFullName(), "action", partner.getIsActive() ? FunctionType.UNLOCK_PARTNER.getFunction() : FunctionType.LOCK_PARTNER.getFunction(), "object", partner.getPartnerName()),
						userDetails.getId(), partner.getId(), userDetails.getPartnerId());
		}
			return "";
	}


	public Partners updatePartner(PartnerRequest partner, User token) {
		Partners save = partnerRepository.getPartnerByCode(partner.getPartnerCode());
		save.setAddress(partner.getAddress());
		save.setPartnerName(partner.getPartnerName());
		save.setEmail(partner.getEmail());
		save.setWebsite(partner.getWebsite());
		save.setPhone(partner.getPhone());
		save.setTaxCode(partner.getTaxCode());
		save.setPartnerCode(partner.getPartnerCode());
		save.setImgLogo(partner.getBase64Image());
		save.setFax(partner.getFax());
		save.setIsActive(save.getIsActive());
		save.setUpdateAt(LocalDateTime.now());
		save.setUpdateBy(token.getId());
		partnerRepository.save(save);
		logService.createLog(ActionType.UPDATE.getAction(),
				Map.of("actor", token.getFullName(), "action",FunctionType.UPDATE_PARTNER.getFunction(),
						"object", save.getPartnerName()),
				token.getId(), save.getId(), token.getPartnerId());
		return save;
	}
	public Partners UpdatePartnerImage(PartnerRequest partner,  User userDetails) {
		if(partner.getBase64Image() != null) {
			Partners save = partnerRepository.getPartnerById(userDetails.getId());
			save.setImgLogo(partner.getBase64Image());
			return partnerRepository.save(save);
		}
		return null;
	}

	public void LogDetailPartner(UUID id, User user) {
		Partners domain = partnerRepository.getPartnerById(id);
		logService.createLog(ActionType.VIEW.getAction(), Map.of("actor", user.getFullName(),"action", FunctionType.VIEW_DETAIL_PARTNER.getFunction(), "object", domain.getPartnerName()),
				user.getId(), domain.getId(),user.getPartnerId());
	}

	@Transactional
	public ErrorListResponse checkDeleteMulti(List<DeleteMultiDTO> ids, User userDetails) {
		ErrorListResponse response = new ErrorListResponse();
		List<ErrorListResponse.ErrorResponse> lstObject = new ArrayList<>();
		for(DeleteMultiDTO id : ids) {
			ErrorListResponse.ErrorResponse object = new ErrorListResponse.ErrorResponse();
			object.setId(id.getId());
			Partners partner = partnerRepository.getPartnerByIdIncluideDeleted(id.getId());
			if (!Objects.equals(partner.getVersion(), id.getVersion())) {
				object.setErrorMessage("error.DataChangedReload");
			}
			else if(partner.getIsDeleted())
				object.setErrorMessage("error.PartnerDoesNotExist");
			else if (userRepository.existsUserByPartnerId(id.getId()) != 0) {
				object.setErrorMessage("error.PartnerAlreadyUseOnUser");
			}
                object.setCode(partner.getPartnerCode());
                object.setName(partner.getPartnerName());
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

	@Transactional
	public String deleteMultiPartner(List<DeleteMultiDTO> ids, User userDetails) {
		for(DeleteMultiDTO id : ids) {
			Partners partner = partnerRepository.getPartnerByIdIncluideDeleted(id.getId());
			if (!Objects.equals(partner.getVersion(), id.getVersion())) {
				return  "error.DataChangedReload";
			}
			if(partner.getIsDeleted())
				return	"error.PartnerDoesNotExist";
			else {
				if (userRepository.existsUserByPartnerId(id.getId()) != 0) {
					return "error.PartnerAlreadyUseOnUser";
				}
				partner.setIsDeleted(Constants.IS_DELETED.DELETED);
				partner.setDeletedAt(LocalDateTime.now());
				partner.setDeletedBy(userDetails.getId());
				partnerRepository.save(partner);
				logService.createLog(ActionType.DELETE.getAction(),
						Map.of("actor", userDetails.getFullName(), "action", FunctionType.DELETE_PARTNER.getFunction(), "object", partner.getPartnerName()),
						userDetails.getId(), partner.getId(), userDetails.getPartnerId());
			}
		}
		return "";
	}

}
