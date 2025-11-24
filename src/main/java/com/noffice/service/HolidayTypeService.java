package com.noffice.service;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.HolidayType;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.enumtype.FunctionType;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.repository.HolidayTypeRepository;
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
public class HolidayTypeService {
    private final HolidayTypeRepository holidayTypeRepository;
    private final LogService logService;

    @Transactional
    public String deleteHolidayType(UUID id, User user, Long version) {
            // find and update is del
            HolidayType holidayType = holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(id);
            if (holidayType == null || !Objects.equals(holidayType.getVersion(), version)) {
                return "error.DataChangedReload";
            } else {
                holidayTypeRepository.deleteHolidayTypeByHolidayTypeId(id);
                logService.createLog(ActionType.DELETE.getAction(),
                        Map.of("actor", user.getFullName(),"action", FunctionType.DELETE_HOLIDAYTYPE.getFunction(),
                                "object", holidayType.getHolidayTypeName()), user.getId(), holidayType.getId(),
                        user.getPartnerId());
            }
            return "";
    }

    @Transactional
    public String deleteMultiHolidayType(List<DeleteMultiDTO> ids, User user) {
        for(DeleteMultiDTO id : ids) {
            HolidayType holidayType = holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(id.getId());
            if (holidayType == null || !Objects.equals(holidayType.getVersion(), id.getVersion())) {
                return "error.DataChangedReload";
            } else {
                holidayTypeRepository.deleteHolidayTypeByHolidayTypeId(id.getId());
                logService.createLog(ActionType.DELETE.getAction(),
                        Map.of("actor", user.getFullName(),"action", FunctionType.DELETE_HOLIDAYTYPE.getFunction(),
                                "object", holidayType.getHolidayTypeName()),
                        user.getId(), holidayType.getId(),user.getPartnerId());
            }
        }
        return "";
    }

    @Transactional
    public String lockHolidayType(UUID id, User user, Long version) {
        HolidayType holidayType = holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(id);
        if (holidayType == null || !Objects.equals(holidayType.getVersion(), version)) {
            return "error.DataChangedReload";
        } else {
            holidayType.setIsActive(!holidayType.getIsActive());
            holidayType.setUpdateAt(LocalDateTime.now());
            holidayType.setUpdateBy(user.getId());
            holidayTypeRepository.save(holidayType);
            logService.createLog(holidayType.getIsActive() ? ActionType.UNLOCK.getAction() : ActionType.LOCK.getAction(),
                    Map.of("actor", user.getFullName(), "action", holidayType.getIsActive() ? FunctionType.UNLOCK_HOLIDAYTYPE.getFunction() : FunctionType.LOCK_HOLIDAYTYPE.getFunction(),
                            "object", holidayType.getHolidayTypeName()),
                    user.getId(), holidayType.getId(), user.getPartnerId());
        }
        return "";
    }

    @Transactional
    public String createHolidayType(HolidayType holidayTypeRequest, User token) {
        if (holidayTypeRepository.findByCode(holidayTypeRequest.getHolidayTypeCode(), token.getPartnerId()) == null) {
            HolidayType holidayType = new HolidayType();
            holidayType.setHolidayTypeCode(holidayTypeRequest.getHolidayTypeCode());
            holidayType.setHolidayTypeName(holidayTypeRequest.getHolidayTypeName());
            holidayType.setDescription(holidayTypeRequest.getDescription());

            holidayType.setCreateAt(LocalDateTime.now());
            holidayType.setCreateBy(token.getId());
            holidayType.setIsActive(true);
            holidayType.setIsDeleted(Constants.isDeleted.ACTIVE);
            holidayType.setPartnerId(token.getPartnerId());
            holidayTypeRepository.save(holidayType);
            logService.createLog(ActionType.CREATE.getAction(),
                    Map.of("actor", token.getFullName(), "action", FunctionType.CREATE_HOLIDAYTYPE.getFunction(),
                            "object", holidayType.getHolidayTypeName()),
                    token.getId(), holidayType.getId(), token.getPartnerId());
            return "";
        } else {
            return "error.HolidayTypeExists";
        }
    }

    @Transactional
    public String updateHolidayType(HolidayType holidayTypeRequest, User token) {
        HolidayType holiday = holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(holidayTypeRequest.getId());
        if (holiday == null || !Objects.equals(holiday.getVersion(), holidayTypeRequest.getVersion())) {
            return "error.DataChangedReload";
        }else {
            holiday.setHolidayTypeCode(holidayTypeRequest.getHolidayTypeCode());
            holiday.setHolidayTypeName(holidayTypeRequest.getHolidayTypeName());
            holiday.setDescription(holidayTypeRequest.getDescription());
            holiday.setIsActive(holidayTypeRequest.getIsActive());
            holiday.setUpdateAt(LocalDateTime.now());
            holiday.setUpdateBy(token.getId());
            holiday.setPartnerId(token.getPartnerId());
            holidayTypeRepository.save(holiday);
            logService.createLog(ActionType.UPDATE.getAction(),
                    Map.of("actor", token.getFullName(), "action",FunctionType.EDIT_HOLIDAYTYPE.getFunction(),
                            "object", holiday.getHolidayTypeName()),
                    token.getId(), holiday.getId(), token.getPartnerId());
        }
        return "";
    }

    public Page<HolidayType> searchHolidayTypes(String searchString, String holidayTypeCode, String holidayTypeName, String description,
                                                Pageable pageable, UUID partnerId) {
        return holidayTypeRepository.searchHolidayTypes(searchString,
                holidayTypeCode, holidayTypeName,
                description, partnerId, pageable);
    }

    public List<HolidayType> getAllHolidayType(UUID partnerId) {
        return holidayTypeRepository.getAllHolidayType(partnerId);
    }

    public void getLogDetailHolidayType(String holidayTypeCode, User user) {
        HolidayType domain = holidayTypeRepository.getHolidayTypeByCode(holidayTypeCode);
        logService.createLog(ActionType.VIEW.getAction(), Map.of("actor", user.getFullName(),"action",
                        FunctionType.VIEW_DETAIL_HOLIDAYTYPE.getFunction(), "object", domain.getHolidayTypeName()),
                user.getId(), domain.getId(),user.getPartnerId());
    }

    @Transactional
    public ErrorListResponse checkDeleteMulti(List<DeleteMultiDTO> ids) {
        ErrorListResponse response = new ErrorListResponse();
        List<ErrorListResponse.ErrorResponse> lstObject = new ArrayList<>();
        for (DeleteMultiDTO id : ids) {
            ErrorListResponse.ErrorResponse object = new ErrorListResponse.ErrorResponse();
            object.setId(id.getId());
            HolidayType holidayType = holidayTypeRepository.findByHolidayTypeIdIncludeDeleted(id.getId());
            if(holidayType == null) {
                object.setErrorMessage("error.DataChangedReload");
                object.setCode(id.getCode());
                object.setName(id.getName());
            }   else {
                object.setCode(holidayType.getHolidayTypeCode());
                object.setName(holidayType.getHolidayTypeName());
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
