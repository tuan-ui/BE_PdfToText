package com.noffice.controller;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.DocumentTemplateCreateDTO;
import com.noffice.dto.DocumentTemplateDTO;
import com.noffice.dto.DocumentTemplateDetailDTO;
import com.noffice.entity.DocumentTemplate;
import com.noffice.entity.User;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.ResponseAPI;
import com.noffice.service.DocumentTemplateService;
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
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documentTemplates")
public class DocumentTemplateController {

	private final DocumentTemplateService documentTemplateService;

	@GetMapping("/search")
	public ResponseAPI search(
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size,
			@RequestParam(value = "documentTemplateCode", required = false) String documentTemplateCode,
			@RequestParam(value = "documentTemplateName", required = false) String documentTemplateName,
			@RequestParam(value = "documentTemplateDescription", required = false) String documentTemplateDescription,
			@RequestParam(value = "searchString", required = false) String searchString) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			Pageable pageable = PageRequest.of(page, size);
			String docTypNameStr = FileUtils.removeAccent(documentTemplateName);
			String searchStringStr = FileUtils.removeAccent(searchString);
			String documentTemplateDescriptionStr = FileUtils.removeAccent(documentTemplateDescription);
			Page<DocumentTemplateDTO> documentTemplates = documentTemplateService.getListDocumentTemplate(searchStringStr, documentTemplateCode, docTypNameStr,documentTemplateDescriptionStr, pageable, userDetails.getPartnerId());
			return new ResponseAPI(documentTemplates, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

	@GetMapping("/delete")
	public ResponseEntity<ResponseAPI> deleteDocumentTemplate(@RequestParam(value = "id") UUID id,
													 @RequestParam(value = "version") Long version,
			HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			String result = documentTemplateService.deleteDocumentTemplate(id, token, version);
			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@PostMapping("/deleteMuti")
	public ResponseEntity<ResponseAPI> deleteMultiDocumentTemplate(@RequestBody List<DeleteMultiDTO> id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			String result = documentTemplateService.deleteMultiDocumentTemplate(id, token);
			if(StringUtils.isNotBlank(result))
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
			
			String result = documentTemplateService.lockUnlockDocumentTemplate(id,token, version);
			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@PostMapping("/add")
	public ResponseEntity<ResponseAPI> save(@Valid @RequestBody DocumentTemplateCreateDTO documentTemplate, HttpServletRequest request) { // Thêm @Valid
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			String result = documentTemplateService.saveDocumentTemplate(documentTemplate, authentication);
			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, result, 400)); // Trả về 400
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "Thêm mới thành công", 200));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}

	@PostMapping("/update")
	public ResponseEntity<ResponseAPI> updateDocumentTemplate(@Valid @RequestBody DocumentTemplateCreateDTO documentTemplate, HttpServletRequest request) { // Thêm @Valid
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();

			String result = documentTemplateService.updateDocumentTemplate(documentTemplate, authentication);

			if(StringUtils.isNotBlank(result))
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, result, 400)); // Trả về 400
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "Thêm mới thành công", 200));

		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		}
	}
	
	@GetMapping("/getAllDocumentTemplate")
	public ResponseAPI getAllDocumentTemplate() {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			List<DocumentTemplate> documentTemplates = documentTemplateService.getAllDocumentTemplate(userDetails.getPartnerId());
			return new ResponseAPI(documentTemplates, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

	@GetMapping("/getDocumentDetail")
	public ResponseAPI getDocumentDetail(@RequestParam UUID id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			DocumentTemplateDetailDTO response = documentTemplateService.getDocumentDetail(id, userDetails);
			return new ResponseAPI(response, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

	@PostMapping("/checkDeleteMulti")
	public ResponseAPI checkDeleteMulti(@RequestBody List<DeleteMultiDTO> ids) {
		try {
			ErrorListResponse message = documentTemplateService.checkDeleteMulti(ids);
			return new ResponseAPI(message, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

	@GetMapping("/getAllowspermission")
	public ResponseAPI getAllowspermission(@RequestParam UUID id) {
		try {
			Map<String,Object> message = documentTemplateService.getAllowspermission(id);
			return new ResponseAPI(message, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

}
