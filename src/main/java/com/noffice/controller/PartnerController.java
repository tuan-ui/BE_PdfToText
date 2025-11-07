package com.noffice.controller;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.Response;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.noffice.dto.PartnerRequest;
import com.noffice.entity.Partners;
import com.noffice.entity.User;
import com.noffice.reponse.ResponseAPI;
import com.noffice.service.PartnerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/partner")
public class PartnerController {
    @Autowired
    private PartnerService partnerService;

    @PostMapping("/search")
    public Response getUsersWithPagination(@RequestBody PartnerRequest request) {
        try {
            int offset = request.getPage() * request.getSize();
            request.setOffset(offset);
            Pageable pageable = PageRequest.of( request.getPage(), request.getSize());
            Page<Partners> partnersPage = partnerService.searchPartners(request, pageable);
            return new Response(partnersPage, "success", 200);
        } catch (Exception e) {
            return new Response(e.toString(), "fail", 400);
        }
    }
    

    @PostMapping("/create")
    public Response createPartner(@Valid @RequestBody PartnerRequest partner) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User token = (User) authentication.getPrincipal();
            Partners partnerSave = partnerService.createPartner(partner, token);
            return new Response(partnerSave, "success", 200);
        } catch (Exception e) {
            return new Response("error", "fail", 400);
        }
    }

    @PostMapping("/update")
    public Response updatePartner(@Valid @RequestBody PartnerRequest partner) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User token = (User) authentication.getPrincipal();
            Partners partnerSave = partnerService.updatePartner(partner, token);
            return new Response(partnerSave, "success", 200);
        } catch (Exception e) {
            return new Response("error", "fail", 400);
        }
    }

    @GetMapping("/delete")
    public ResponseAPI deletePartner(@RequestParam(value = "id") UUID id,
                                     @RequestParam(value = "version") Long version) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            String message = partnerService.deletePartner(id,userDetails, version);
            return new ResponseAPI(null, message, 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }

    @PostMapping("/checkDeleteMulti")
    public ResponseAPI checkDeleteMulti(@RequestBody List<DeleteMultiDTO> items) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            ErrorListResponse message = partnerService.checkDeleteMulti(items, userDetails);
            return new ResponseAPI(message, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }

    @PostMapping("/deleteMulti")
    public ResponseEntity<ResponseAPI> deleteMultiPartner(@RequestBody List<DeleteMultiDTO> ids) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            String message = partnerService.deleteMultiPartner(ids, userDetails);

            if (message != null && !message.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseAPI(null, message, 400));
            }

            return ResponseEntity.ok(new ResponseAPI(null, "Xóa thành công", 200));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }

    @GetMapping("/lock")
    public Response lockPartner(@RequestParam(value = "id") UUID partner,
                                @RequestParam(value = "version") Long version) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            String message = partnerService.lockPartner(partner,userDetails, version);
            return new Response("", message, 201);
        } catch (Exception e) {
            return new Response("error", "Thao tác thất bại", 201);
        }
    }


    @PostMapping("/updateImage")
    public Response updateImage(@RequestBody PartnerRequest partner) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();

            Partners partnerSave = partnerService.UpdatePartnerImage(partner, userDetails);
            return new Response(partnerSave, "success", 200);
        } catch (Exception e) {
            return new Response("error", "Thêm mới đối tác thất bại", 201);
        }
    }

    @GetMapping("/LogDetailPartner")
    public ResponseAPI LogDetailPartner(@RequestParam UUID id) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            partnerService.LogDetailPartner(id, userDetails);
            return new ResponseAPI(null, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }
}
