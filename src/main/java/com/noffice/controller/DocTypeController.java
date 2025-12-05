package com.noffice.controller;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.DocType;
import com.noffice.entity.User;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.ResponseAPI;
import com.noffice.service.DocTypeService;
import com.noffice.ultils.Constants;
import com.noffice.ultils.FileUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/doc-type")
@RequiredArgsConstructor
@Tag(name = "DocTypeController", description = "Quản lý Loại Văn Bản")
public class DocTypeController {

	private final DocTypeService docTypeService;

	@GetMapping("/search")
	public ResponseAPI search(
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size,
			@RequestParam(value = "docTypeCode", required = false) String docTypeCode,
			@RequestParam(value = "docTypeName", required = false) String docTypeName,
			@RequestParam(value = "docTypeDescription", required = false) String docTypeDescription,
			@RequestParam(value = "searchString", required = false) String searchString) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			Pageable pageable = PageRequest.of(page, size);
			String docTypNameStr = FileUtils.removeAccent(docTypeName);
			String searchStringStr = FileUtils.removeAccent(searchString);
			String docTypeDescriptionStr = FileUtils.removeAccent(docTypeDescription);
			Page<DocType> docTypes = docTypeService.getListDocType(searchStringStr, docTypeCode, docTypNameStr,docTypeDescriptionStr, pageable, userDetails.getPartnerId());
			return new ResponseAPI(docTypes, Constants.messageResponse.SUCCESS, 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 500);
		}
	}

	@GetMapping("/delete")
	public ResponseEntity<ResponseAPI> deleteDocType(@RequestParam(value = "id") UUID id,
													 @RequestParam(value = "version") Long version,
			HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			String result = docTypeService.deleteDocType(id, token, version);
			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, Constants.messageResponse.SUCCESS, 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, Constants.messageResponse.ERROR + e.getMessage(), 500));
		}
	}

	@PostMapping("/deleteMuti")
	public ResponseEntity<ResponseAPI> deleteMultiDocType(@RequestBody List<DeleteMultiDTO> id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			String result = docTypeService.deleteMultiDocType(id, token);
			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, Constants.messageResponse.SUCCESS, 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, Constants.messageResponse.ERROR + e.getMessage(), 500));
		}
	}

	@GetMapping("/lock")
	public ResponseEntity<ResponseAPI> lockUser(@RequestParam UUID id,
												@RequestParam(value = "version") Long version,
			HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			
			String result = docTypeService.lockUnlockDocType(id,token, version);
			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, Constants.messageResponse.SUCCESS, 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, Constants.messageResponse.ERROR + e.getMessage(), 500));
		}
	}

	@PostMapping("/add")
	public ResponseEntity<ResponseAPI> save(@Valid @RequestBody DocType docType, HttpServletRequest request) { // Thêm @Valid
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			String result = docTypeService.saveDocType(docType, userDetails);
			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, result, 400)); // Trả về 400
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "Thêm mới thành công", 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, Constants.messageResponse.ERROR + e.getMessage(), 500));

		}
	}

	@PostMapping("/update")
	public ResponseEntity<ResponseAPI> updateDocType(@Valid @RequestBody DocType docType, HttpServletRequest request) { // Thêm @Valid
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();

			String result = docTypeService.updateDocType(docType, userDetails);

			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, result, 400)); // Trả về 400
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "Cập nhật thành công", 200));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, Constants.messageResponse.ERROR + e.getMessage(), 500));

		}
	}
	
	@GetMapping("/getAllDocType")
	public ResponseAPI getAllDocType() {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			List<DocType> docTypes = docTypeService.getAllDocType(userDetails.getPartnerId());
			return new ResponseAPI(docTypes, Constants.messageResponse.SUCCESS, 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 500);
		}
	}

	@GetMapping("/LogDetailDocType")
	public ResponseAPI getLogDetailDocType(@RequestParam String id) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			docTypeService.getLogDetailDocType(id, userDetails);
			return new ResponseAPI(null, Constants.messageResponse.SUCCESS, 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 500);
		}
	}

	@PostMapping("/checkDeleteMulti")
	public ResponseAPI checkDeleteMulti(@RequestBody List<DeleteMultiDTO> ids) {
		try {
			ErrorListResponse message = docTypeService.checkDeleteMulti(ids);
			return new ResponseAPI(message, Constants.messageResponse.SUCCESS, 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 500);
		}
	}

}
