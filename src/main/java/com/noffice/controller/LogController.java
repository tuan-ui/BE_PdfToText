package com.noffice.controller;

import java.util.List;
import java.util.UUID;

import com.noffice.entity.Logs;
import com.noffice.entity.User;
import com.noffice.enumtype.ActionType;
import com.noffice.enumtype.FunctionType;
import com.noffice.ultils.Constants;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.noffice.reponse.ResponseAPI;
import com.noffice.service.LogService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/log")
public class LogController {
	private final LogService logService;

	@GetMapping("/list")
	public ResponseAPI getLogs(
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size,
			@RequestParam(value = "userId", required = false) UUID userId,
			@RequestParam(value = "actionKey", required = false) String actionKey,
			@RequestParam(value = "functionKey", required = false) String functionKey,
			@RequestParam(value = "fromDateStr", required = false) String fromDateStr,
			@RequestParam(value = "toDateStr", required = false) String toDateStr) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			Pageable pageable = PageRequest.of(page, size);
			Page<Logs> listLogs = logService.getLogs(userId, actionKey, functionKey,
					fromDateStr, toDateStr, pageable, userDetails.getPartnerId());
			return new ResponseAPI(listLogs, Constants.messageResponse.SUCCESS, 200);
		} catch (Exception e) {

			return new ResponseAPI(null, Constants.messageResponse.ERROR + e.getMessage(), 500);
		}

	}

	@GetMapping("/getListFunction")
	public ResponseAPI getListFunction() {
		try {
			List<String> listFunction = FunctionType.getAllFunction();
			return new ResponseAPI(listFunction, Constants.messageResponse.SUCCESS, 200);
		} catch (Exception e) {
			return new ResponseAPI(null, Constants.messageResponse.ERROR + e.getMessage(), 500);
		}
	}

	@GetMapping("/getListAction")
	public ResponseAPI getListAction() {
		try {
			List<String> listAction = ActionType.getAllActions();
			return new ResponseAPI(listAction, Constants.messageResponse.SUCCESS, 200);
		} catch (Exception e) {
			return new ResponseAPI(null, Constants.messageResponse.ERROR + e.getMessage(), 500);
		}
	}

}
