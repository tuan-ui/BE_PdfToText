package com.noffice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.noffice.entity.*;
import com.noffice.enumType.FunctionType;
import com.noffice.enumType.ActionType;
import com.noffice.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogService {
	private final LogRepository logRepository;

	public void createLog(String key, Map<String, Object> params, UUID userId, UUID objectId, UUID partnerId) {
		Logs log = new Logs();
		log.setCreateBy(userId);
		log.setActionKey(key);
		log.setParams(params);
		log.setCreateAt(LocalDateTime.now());
		log.setIsActive(true);
		log.setIsDeleted(false);
		log.setObjectId(objectId);
		log.setPartnerId(partnerId);
		logRepository.save(log);
	}

	public Page<Logs> getLogs(UUID userId, String actionKey, String functionKey,
							  String fromDateStr, String toDateStr,
							  Pageable pageable, UUID partnerId) throws Exception {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		LocalDateTime fromDateTime = null;
		LocalDateTime toDateTime = null;
		try {
			if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
				fromDateTime = LocalDate.parse(fromDateStr, formatter).atStartOfDay();
			}
			if (toDateStr != null && !toDateStr.trim().isEmpty()) {
				toDateTime = LocalDate.parse(toDateStr, formatter).atTime(LocalTime.MAX);
			}
		} catch (Exception e) {
			throw new Exception("Định dạng ngày không hợp lệ, yêu cầu: dd/MM/yyyy");
		}

		if (functionKey != null && !functionKey.trim().isEmpty()) {
			functionKey = FunctionType.fromFunction(functionKey).getFunction();
		} else {
			functionKey = null;
		}

		if (actionKey != null && !actionKey.trim().isEmpty()) {
			actionKey = ActionType.fromAction(actionKey).getAction();
		} else {
			actionKey = null;
		}

		return logRepository.getLogs(userId, fromDateTime, toDateTime,
				functionKey, actionKey, partnerId, pageable);
	}

}
