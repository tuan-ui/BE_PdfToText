package com.noffice.controller;

import com.noffice.dto.*;
import com.noffice.entity.FormSchema;
import com.noffice.entity.User;
import com.noffice.reponse.ResponseAPI;
import com.noffice.service.DnDService;
import com.noffice.ultils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/DnD")
public class DnDController {

    @Autowired
    private DnDService dndService;

    @PostMapping("/saveContent")
    public ResponseEntity<ResponseAPI> saveContent(@RequestBody DnDDTO DnDDTO) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            String response = dndService.saveContent(DnDDTO, userDetails);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(response, "Thành công", 200));
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }
    @PostMapping("/publishSchema")
    public ResponseEntity<ResponseAPI> publishSchema(@RequestBody DnDDTO DnDDTO) {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            String response = dndService.publishSchema(DnDDTO, userDetails);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(response, "Thành công", 200));
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }
    @GetMapping("/getContent/{id}")
    public ResponseEntity<ResponseAPI> getContent(@PathVariable String id) {
        try {

//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            User userDetails = (User) authentication.getPrincipal();
            FormSchema response = dndService.getContent(id);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(response.getFormContent(), "Thành công", 200));
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }

    @PostMapping("/getContent/{id}")
    public ResponseEntity<ResponseAPI> saveContent(@PathVariable String id) {
        try {

//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//            User userDetails = (User) authentication.getPrincipal();
            Map<String, List<String>> response = dndService.summarizeResponses(id);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(response, "Thành công", 200));
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }
    @PostMapping("/searchFormSchemas")
    public ResponseEntity<ResponseAPI> searchFormSchemas(@RequestBody FormSchemaSearchDTO request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();

            if (request.getFormName() != null && !request.getFormName().isEmpty()) {
                String textEscape = StringUtils.unAccent(request.getFormName()).trim();
                textEscape = StringUtils.toLikeAndLowerCaseString(textEscape);
                request.setFormName(textEscape);
            }
            if (request.getFormCode() != null && !request.getFormCode().isEmpty()) {
                String textEscape = StringUtils.unAccent(request.getFormCode()).trim();
                textEscape = StringUtils.toLikeAndLowerCaseString(textEscape);
                request.setFormCode(textEscape);
            }

            Page<FormSchema> result = dndService.searchFormSchemas(
                    request,
                    userDetails.getPartnerId()
            );

            return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(result, "Thành công", 200));
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }
    @GetMapping("/delete")
    public ResponseEntity<ResponseAPI> delete(
            @RequestParam(value = "id", required = true) UUID id,
            HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();

            String message = dndService.delete(id, userDetails);
            if (message != null && !message.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseAPI(null, message, 400));
            }

            return ResponseEntity.ok(new ResponseAPI(null, "Xóa thành công", 200));
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }
    @GetMapping("/deleteMuti")
    public ResponseEntity<ResponseAPI> deleteMuti(@RequestParam List<UUID> id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();

            String message = dndService.deleteMuti(id, userDetails);
            if (message != null && !message.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseAPI(null, message, 400));
            }

            return ResponseEntity.ok(new ResponseAPI(null, "Xóa thành công", 200));
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }
    @GetMapping("/lock")
    public ResponseEntity<ResponseAPI> lock(
            @RequestParam(value = "id", required = true) UUID id,
            HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            dndService.lockUser(id,userDetails);
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
        } catch (Exception e) {
            System.out.println("Error : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }
}
