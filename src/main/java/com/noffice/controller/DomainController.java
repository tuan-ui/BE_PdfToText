package com.noffice.controller;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.Domain;
import com.noffice.entity.User;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.ResponseAPI;
import com.noffice.service.DomainService;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/domains")
@Tag(name = "DomainController", description = "Quản lý Phòng ban")
public class DomainController {

	private final DomainService domainService;

	@GetMapping("/searchDomains")
	public ResponseAPI getListDomain(
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size,
			@RequestParam(value = "domainCode", required = false) String domainCode,
			@RequestParam(value = "domainName", required = false) String domainName,
			@RequestParam(value = "domainDescription", required = false) String domainDescription,
			@RequestParam(value = "searchString", required = false) String searchString) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			Pageable pageable = PageRequest.of(page, size);
			Page<Domain> domains = domainService.getListDomain(searchString, domainCode, domainName,domainDescription, pageable, userDetails.getPartnerId());
			return new ResponseAPI(domains, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

	@GetMapping("/delete")
	public ResponseEntity<ResponseAPI> deleteDomain(@RequestParam(value = "id") UUID id,
													@RequestParam(value = "version") Long version,
			HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			String result = domainService.deleteDomain(id, token, version);
			if(result == null || !result.trim().isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@PostMapping("/deleteMuti")
	public ResponseEntity<ResponseAPI> deleteMultiDomain(@RequestBody List<DeleteMultiDTO> id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			String result = domainService.deleteMultiDomain(id, token);
			if(result == null || !result.trim().isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@PostMapping("/checkDeleteMulti")
	public ResponseAPI checkDeleteMulti(@RequestBody List<DeleteMultiDTO> ids) {
		try {
			ErrorListResponse message = domainService.checkDeleteMulti(ids);
			return new ResponseAPI(message, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

	@GetMapping("/lock")
	public ResponseEntity<ResponseAPI> lockUser(@RequestParam UUID id,
												@RequestParam(value = "version") Long version,
			HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			
			String deptName = domainService.lockUnlockDomain(id,token, version);
			if(deptName == null || !deptName.trim().isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, deptName, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@PostMapping("/add")
	public ResponseEntity<ResponseAPI> save(@Valid @RequestBody Domain domain, HttpServletRequest request) { // Thêm @Valid
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			String result = domainService.saveDomain(domain, authentication);
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
	public ResponseEntity<ResponseAPI> updateDomain(@Valid @RequestBody Domain domain, HttpServletRequest request) { // Thêm @Valid
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();

			String result = domainService.updateDomain(domain, authentication);

			if (result == null || !result.trim().isEmpty())
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, result, 400)); // Trả về 400
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "Cập nhật thành công", 200));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}
	
	@GetMapping("/getAllDomain")
	public ResponseAPI getAllDomain() {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			List<Domain> domains = domainService.getAllDomain(userDetails.getPartnerId());
			return new ResponseAPI(domains, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

	@GetMapping("/LogDetailDomain")
	public ResponseAPI LogDetailDomain(@RequestParam String id) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			domainService.LogDetailDomain(id, userDetails);
			return new ResponseAPI(null, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

}
