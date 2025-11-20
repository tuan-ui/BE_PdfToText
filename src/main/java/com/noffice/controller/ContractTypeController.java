package com.noffice.controller;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.ContractType;
import com.noffice.entity.User;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.ResponseAPI;
import com.noffice.service.ContractTypeService;
import com.noffice.ultils.FileUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
			return new ResponseAPI(contractTypes, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
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
			if(result!=null && !result.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@PostMapping("/deleteMuti")
	public ResponseEntity<ResponseAPI> deleteMultiContractType(@RequestBody List<DeleteMultiDTO> id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			String result = contractTypeService.deleteMultiContractType(id, token);
			if(result!=null && !result.isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@GetMapping("/lock")
	public ResponseEntity<ResponseAPI> lockUser(@RequestParam UUID id,
												@RequestParam(value = "version") Long version,
			HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			
			String deptName = contractTypeService.lockUnlockContractType(id,token, version);
			if(deptName == null || !deptName.trim().isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, deptName, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@PostMapping("/add")
	public ResponseEntity<ResponseAPI> save(@Valid @RequestBody ContractType contractType, HttpServletRequest request) { // Thêm @Valid
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			String result = contractTypeService.saveContractType(contractType, authentication);
			if (result == null || !result.trim().isEmpty())
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, result, 400)); // Trả về 400
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "Thêm mới thành công", 200));
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

			if (result == null || !result.trim().isEmpty())
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, result, 400)); // Trả về 400
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "Thêm mới thành công", 200));

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
			return new ResponseAPI(contractTypes, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

	@GetMapping("/LogDetailContractType")
	public ResponseAPI LogDetailContractType(@RequestParam String id) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			contractTypeService.LogDetailContractType(id, userDetails);
			return new ResponseAPI(null, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

	@PostMapping("/checkDeleteMulti")
	public ResponseAPI checkDeleteMulti(@RequestBody List<DeleteMultiDTO> ids) {
		try {
			ErrorListResponse message = contractTypeService.checkDeleteMulti(ids);
			return new ResponseAPI(message, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

}
