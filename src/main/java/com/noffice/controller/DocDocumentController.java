package com.noffice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noffice.dto.DeleteMultiDTO;
import com.noffice.dto.DocDocumentDTO;
import com.noffice.entity.*;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.ResponseAPI;
import com.noffice.service.DocDocumentService;
import com.noffice.ultils.Constants;
import com.noffice.ultils.FileUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/doc-document")
@RequiredArgsConstructor
@Tag(name = "DocDocumentController", description = "Quản lý Văn bản cá nhân")
public class DocDocumentController {
	private final DocDocumentService docDocumentService;
	private final ObjectMapper objectMapper;

	@GetMapping("/search")
	public ResponseAPI search(
			@RequestParam(value = "page", required = false, defaultValue = "0") int page,
			@RequestParam(value = "size", required = false, defaultValue = "10") int size,
			@RequestParam(value = "status", required = false) String status,
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
			Page<DocDocumentDTO> docTypes = docDocumentService.getListDoc(searchStringStr, docTypeCode, docTypNameStr,docTypeDescriptionStr, pageable, userDetails.getPartnerId());
			return new ResponseAPI(docTypes, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

	@GetMapping("/delete")
	public ResponseEntity<ResponseAPI> deleteDocType(@RequestParam(value = "id") UUID id,
			HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			Boolean result = docDocumentService.delete(id, token);
			if(!result)
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "error", 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@PostMapping("/deleteMuti")
	public ResponseEntity<ResponseAPI> deleteMultiDocType(@RequestBody List<DeleteMultiDTO> id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			String result = docDocumentService.deleteMulti(id, token);
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
			
			String deptName = docDocumentService.lockUnlock(id,token, version);
			if(deptName == null || !deptName.trim().isEmpty())
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, deptName, 400));
			else
				return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@PostMapping("/createOrUpdate")
	public ResponseEntity<ResponseAPI> save(
			@RequestPart("docDocument") String docDocumentJson,
			@RequestPart(name = "files", required = false) MultipartFile[] files,
			HttpServletRequest request) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			DocDocumentDTO docDocument = null;
			try {
				docDocument = objectMapper.readValue(docDocumentJson, DocDocumentDTO.class);
			} catch (JsonProcessingException e) {
				System.out.println(e.toString());
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new ResponseAPI(null, "Lỗi dữ liệu đầu vào", 400));
			}
			DocDocument result = docDocumentService.save(docDocument,files, userDetails);
			if (result==null)
				return ResponseEntity.status(HttpStatus.BAD_REQUEST)
						.body(new ResponseAPI(null, "Thao tác thất bại", 400));
			else
				return ResponseEntity.status(HttpStatus.OK)
						.body(new ResponseAPI(result, "Thêm mới thành công", 200));
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, "error", 400));
		}
	}


	
	@GetMapping("/getAllDocType")
	public ResponseAPI getAllDocType() {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			List<DocType> docTypes = docDocumentService.getAllDocType(userDetails.getPartnerId());
			return new ResponseAPI(docTypes, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

	@GetMapping("/LogDetailDocType")
	public ResponseAPI getLogDetailDocType(@RequestParam String id) {

		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User userDetails = (User) authentication.getPrincipal();
			docDocumentService.getLogDetailDocType(id, userDetails);
			return new ResponseAPI(null, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}

	@PostMapping("/checkDeleteMulti")
	public ResponseAPI checkDeleteMulti(@RequestBody List<DeleteMultiDTO> ids) {
		try {
			ErrorListResponse message = docDocumentService.checkDeleteMulti(ids);
			return new ResponseAPI(message, "success", 200);
		} catch (Exception e) {
			return new ResponseAPI(null, "fail", 400);
		}
	}
	@GetMapping("/attachs")
	public ResponseEntity<ResponseAPI> attach(@RequestParam(value = "id", required = false, defaultValue = "0") UUID id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			List<Attachs> lstAttach = docDocumentService.getAttachsByDocument(id, Constants.OBJECT_TYPE.DOC_DOCUMENT);
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(lstAttach, "success", 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}

	@GetMapping("/users-process")
	public ResponseEntity<ResponseAPI> getUsersProcess(@RequestParam(value = "id", required = false, defaultValue = "0") UUID id) {
		try {
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			User token = (User) authentication.getPrincipal();
			List<NodeDeptUser> lstAttach = docDocumentService.getByDocId(id);
			return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(lstAttach, "success", 200));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
		}
	}
}
