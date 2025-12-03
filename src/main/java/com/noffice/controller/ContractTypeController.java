package com.noffice.controller;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.ContractType;
import com.noffice.entity.User;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.ResponseAPI;
import com.noffice.service.ContractTypeService;
import com.noffice.ultils.Constants;
import com.noffice.ultils.FileUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/contractType")
public class ContractTypeController {

	private final ContractTypeService contractTypeService;

	@GetMapping("/search")
	public ResponseAPI search(
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size,
			@RequestParam(value = "contractTypeCode", required = false) String contractTypeCode,
			@RequestParam(value = "contractTypeName", required = false) String contractTypeName,
			@RequestParam(value = "contractTypeDescription", required = false) String contractTypeDescription,
			@RequestParam(value = "searchString", required = false) String searchString) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			Pageable pageable = PageRequest.of(page, size);
			String docTypNameStr = FileUtils.removeAccent(contractTypeName);
			String searchStringStr = FileUtils.removeAccent(searchString);
			String contractTypeDescriptionStr = FileUtils.removeAccent(contractTypeDescription);
			Page<ContractType> contractTypes = contractTypeService.getListContractType(searchStringStr, contractTypeCode, docTypNameStr,contractTypeDescriptionStr, pageable, userDetails.getPartnerId());
			return new ResponseAPI(contractTypes, Constants.message.SUCCESS, 200);
		} catch (Exception e) {
			return new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 500);
		}
	}

	@GetMapping("/delete")
	public ResponseEntity<ResponseAPI> deleteContractType(@RequestParam(value = "id") UUID id,
													 @RequestParam(value = "version") Long version,
			HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			String result = contractTypeService.deleteContractType(id, token, version);
			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, Constants.message.SUCCESS, 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 500));
		}
	}

	@PostMapping("/deleteMuti")
	public ResponseEntity<ResponseAPI> deleteMultiContractType(@RequestBody List<DeleteMultiDTO> id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			String result = contractTypeService.deleteMultiContractType(id, token);
			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, Constants.message.SUCCESS, 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 500));
		}
	}

	@GetMapping("/lock")
	public ResponseEntity<ResponseAPI> lockUser(@RequestParam UUID id,
												@RequestParam(value = "version") Long version,
			HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			
			String result = contractTypeService.lockUnlockContractType(id,token, version);
			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, Constants.message.SUCCESS, 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 500));
		}
	}

	@PostMapping("/add")
	public ResponseEntity<ResponseAPI> save(@Valid @RequestBody ContractType contractType, HttpServletRequest request) { // Thêm @Valid
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			String result = contractTypeService.saveContractType(contractType, authentication);
			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, result, 400)); // Trả về 400
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, Constants.message.ADD_SUCCESS, 200));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}

	@PostMapping("/update")
	public ResponseEntity<ResponseAPI> updateContractType(@Valid @RequestBody ContractType contractType, HttpServletRequest request) { // Thêm @Valid
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();

			String result = contractTypeService.updateContractType(contractType, authentication);

			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, result, 400)); // Trả về 400
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, Constants.message.UPDATE_SUCCESS, 200));

		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}
	
	@GetMapping("/getAllContractType")
	public ResponseAPI getAllContractType() {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			List<ContractType> contractTypes = contractTypeService.getAllContractType(userDetails.getPartnerId());
			return new ResponseAPI(contractTypes, Constants.message.SUCCESS, 200);
		} catch (Exception e) {
			return new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 500);
		}
	}

	@GetMapping("/LogDetailContractType")
	public ResponseAPI getLogDetailContractType(@RequestParam String id) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			contractTypeService.getLogDetailContractType(id, userDetails);
			return new ResponseAPI(null, Constants.message.SUCCESS, 200);
		} catch (Exception e) {
			return new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 500);
		}
	}

	@PostMapping("/checkDeleteMulti")
	public ResponseAPI checkDeleteMulti(@RequestBody List<DeleteMultiDTO> ids) {
		try {
			ErrorListResponse message = contractTypeService.checkDeleteMulti(ids);
			return new ResponseAPI(message, Constants.message.SUCCESS, 200);
		} catch (Exception e) {
			return new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 500);
		}
	}

}
