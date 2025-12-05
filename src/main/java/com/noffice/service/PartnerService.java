package com.noffice.service;

import java.time.LocalDateTime;
import java.util.*;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.*;
import com.noffice.enumtype.ActionType;
import com.noffice.enumtype.FunctionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.noffice.dto.PartnerRequest;
import com.noffice.repository.PartnerRepository;
import com.noffice.ultils.Constants;

import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
public class PartnerService {
	private final PartnerRepository partnerRepository;
	private final LogService logService;
    private final UserRepository userRepository;

	public Page<Partners> searchPartners(PartnerRequest partnerRequest, Pageable pageable) {
        return partnerRepository.searchPartners(partnerRequest.getSearchString(),partnerRequest.getPartnerName(),partnerRequest.getEmail(),
				partnerRequest.getPhone(),partnerRequest.getAddress(), pageable);
    }

	@Transactional
	public String createPartner(PartnerRequest partner, User token) {
		if(partnerRepository.getPartnerByCode(partner.getPartnerCode())==null){
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
			save.setIsDeleted(Constants.isDeleted.ACTIVE);
			 partnerRepository.save(save);
			 logService.createLog(ActionType.CREATE.getAction(),
					 Map.of(Constants.logResponse.ACTOR, token.getFullName(), Constants.logResponse.ACTION,FunctionType.CREATE_PARTNER.getFunction(),
							 Constants.logResponse.OBJECT, save.getPartnerName()),
					 token.getId(), save.getId(), token.getPartnerId());
			return "";
		} else {
			return "error.PartnerIsExist";
		}
    }

	@Transactional
	public String deletePartner(UUID partnerId,User userDetails, Long version) {
		Partners partner = partnerRepository.getPartnerByIdIncluideDeleted(partnerId);
		if (partner == null || !Objects.equals(partner.getVersion(), version)) {
			return  Constants.errorResponse.DATA_CHANGED;
		} else {
			if (userRepository.existsUserByPartnerId(partnerId) != 0) {
				return "error.PartnerAlreadyUseOnUser";
			}
			partnerRepository.deletePartnersByPartnersId(partnerId);
			logService.createLog(ActionType.DELETE.getAction(),
					Map.of(Constants.logResponse.ACTOR, userDetails.getFullName(), Constants.logResponse.ACTION, FunctionType.DELETE_PARTNER.getFunction(), Constants.logResponse.OBJECT, partner.getPartnerName()),
					userDetails.getId(), partner.getId(), userDetails.getPartnerId());
		}
		return "";
	}
	@Transactional
	public String lockPartner(UUID partnerId,User userDetails,  Long version) {
		Partners partner = partnerRepository.getPartnerByIdIncluideDeleted(partnerId);
		if (partner == null || !Objects.equals(partner.getVersion(), version)) {
			return  Constants.errorResponse.DATA_CHANGED;
		} else {
			partner.setIsActive(!partner.getIsActive());
			partner.setUpdateAt(LocalDateTime.now());
			partner.setUpdateBy(userDetails.getId());
			partnerRepository.save(partner);
			logService.createLog(Boolean.TRUE.equals(partner.getIsActive()) ? ActionType.UNLOCK.getAction() : ActionType.LOCK.getAction(),
					Map.of(Constants.logResponse.ACTOR, userDetails.getFullName(), Constants.logResponse.ACTION, Boolean.TRUE.equals(partner.getIsActive()) ? FunctionType.UNLOCK_PARTNER.getFunction() : FunctionType.LOCK_PARTNER.getFunction(), Constants.logResponse.OBJECT, partner.getPartnerName()),
					userDetails.getId(), partner.getId(), userDetails.getPartnerId());
		}
			return "";
	}


	public String updatePartner(PartnerRequest partner, User token) {
		Partners save = partnerRepository.getPartnerByCode(partner.getPartnerCode());
		if (save == null || !Objects.equals(save.getVersion(), partner.getVersion())) {
			return  Constants.errorResponse.DATA_CHANGED;
		} else {
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
				Map.of(Constants.logResponse.ACTOR, token.getFullName(), Constants.logResponse.ACTION,FunctionType.UPDATE_PARTNER.getFunction(),
						Constants.logResponse.OBJECT, save.getPartnerName()),
				token.getId(), save.getId(), token.getPartnerId());
		}
		return "";
	}
	public Partners updatePartnerImage(PartnerRequest partner,  User userDetails) {
		if(partner.getBase64Image() != null) {
			Partners save = partnerRepository.getPartnerById(userDetails.getId());
			save.setImgLogo(partner.getBase64Image());
			return partnerRepository.save(save);
		}
		return null;
	}

	public void getLogDetailPartner(UUID id, User user) {
		Partners domain = partnerRepository.getPartnerById(id);
		logService.createLog(ActionType.VIEW.getAction(), Map.of(Constants.logResponse.ACTOR, user.getFullName(),Constants.logResponse.ACTION, FunctionType.VIEW_DETAIL_PARTNER.getFunction(), Constants.logResponse.OBJECT, domain.getPartnerName()),
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
			if(partner == null) {
				object.setErrorMessage(Constants.errorResponse.DATA_CHANGED);
				object.setCode(id.getCode());
				object.setName(id.getName());
			} else if (userRepository.existsUserByPartnerId(id.getId()) != 0) {
				object.setErrorMessage("error.PartnerAlreadyUseOnUser");
				object.setCode(partner.getPartnerCode());
				object.setName(partner.getPartnerName());
			} else {
				object.setCode(partner.getPartnerCode());
				object.setName(partner.getPartnerName());
			}
			lstObject.add(object);
		}
		response.setErrors(lstObject);
		response.setTotal(ids.size());
		long countNum = response.getErrors().stream()
				.filter(item -> item.getErrorMessage()!=null)
				.count();
		response.setHasError(countNum != 0);
		if(Boolean.FALSE.equals(response.getHasError()))
		{
			return null;
		}
		return response;
	}

	@Transactional
	public String deleteMultiPartner(List<DeleteMultiDTO> ids, User userDetails) {
		for(DeleteMultiDTO id : ids) {
			Partners partner = partnerRepository.getPartnerByIdIncluideDeleted(id.getId());
			if (partner == null || !Objects.equals(partner.getVersion(), id.getVersion())) {
				return  Constants.errorResponse.DATA_CHANGED;
			} else {
				if (userRepository.existsUserByPartnerId(id.getId()) != 0) {
					return "error.PartnerAlreadyUseOnUser";
				}
				partnerRepository.deletePartnersByPartnersId(id.getId());
				logService.createLog(ActionType.DELETE.getAction(),
						Map.of(Constants.logResponse.ACTOR, userDetails.getFullName(), Constants.logResponse.ACTION, FunctionType.DELETE_PARTNER.getFunction(), Constants.logResponse.OBJECT, partner.getPartnerName()),
						userDetails.getId(), partner.getId(), userDetails.getPartnerId());
			}
		}
		return "";
	}

}
