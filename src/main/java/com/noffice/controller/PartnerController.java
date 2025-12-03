package com.noffice.controller;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.Response;
import com.noffice.ultils.Constants;
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
@RequiredArgsConstructor
@RequestMapping("/api/partner")
public class PartnerController {
    private final PartnerService partnerService;

    @PostMapping("/search")
    public Response getUsersWithPagination(@RequestBody PartnerRequest request) {
        try {
            int offset = request.getPage() * request.getSize();
            request.setOffset(offset);
            Pageable pageable = PageRequest.of( request.getPage(), request.getSize());
            Page<Partners> partnersPage = partnerService.searchPartners(request, pageable);
            return new Response(partnersPage, Constants.message.SUCCESS, 200);
        } catch (Exception e) {
            return new Response(e.toString(), Constants.message.SYSTEM_ERROR, 500);
        }
    }
    

    @PostMapping("/create")
    public Response createPartner(@Valid @RequestBody PartnerRequest partner) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User token = (User) authentication.getPrincipal();
            String partnerSave = partnerService.createPartner(partner, token);
            if(StringUtils.isNotBlank(partnerSave))
                return new Response(partnerSave, partnerSave, 400);
            else
                return new Response(null, Constants.message.ADD_SUCCESS, 200);
        } catch (Exception e) {
            return new Response(null, Constants.message.SYSTEM_ERROR, 500);
        }
    }

    @PostMapping("/update")
    public Response updatePartner(@Valid @RequestBody PartnerRequest partner) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User token = (User) authentication.getPrincipal();
            String partnerSave = partnerService.updatePartner(partner, token);
            if(StringUtils.isNotBlank(partnerSave))
                return new Response(partnerSave, partnerSave, 400);
            else
                return new Response(null, Constants.message.UPDATE_SUCCESS, 200);
        } catch (Exception e) {
            return new Response(null, Constants.message.SYSTEM_ERROR, 500);
        }
    }

    @GetMapping("/delete")
    public ResponseAPI deletePartner(@RequestParam(value = "id") UUID id,
                                     @RequestParam(value = "version") Long version) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            String message = partnerService.deletePartner(id,userDetails, version);
            if(StringUtils.isNotBlank(message))
                return new ResponseAPI(message, message, 400);
            else
                return new ResponseAPI(null, Constants.message.SUCCESS, 200);
        } catch (Exception e) {
            return new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 500);
        }
    }

    @PostMapping("/checkDeleteMulti")
    public ResponseAPI checkDeleteMulti(@RequestBody List<DeleteMultiDTO> items) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            ErrorListResponse message = partnerService.checkDeleteMulti(items, userDetails);
            return new ResponseAPI(message, Constants.message.SUCCESS, 200);
        } catch (Exception e) {
            return new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 500);
        }
    }

    @PostMapping("/deleteMulti")
    public ResponseEntity<ResponseAPI> deleteMultiPartner(@RequestBody List<DeleteMultiDTO> ids) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            String message = partnerService.deleteMultiPartner(ids, userDetails);

            if(StringUtils.isNotBlank(message))
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(message, message, 400));
            else
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, Constants.message.SUCCESS, 200));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 500));
        }
    }

    @GetMapping("/lock")
    public Response lockPartner(@RequestParam(value = "partner") UUID partner,
                                @RequestParam(value = "version") Long version) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            String message = partnerService.lockPartner(partner,userDetails, version);
            if(StringUtils.isNotBlank(message))
                return new Response(message, message, 400);
            else
                return new Response(message, Constants.message.SUCCESS, 200);
        } catch (Exception e) {
            return new Response(null, Constants.message.SYSTEM_ERROR, 500);
        }
    }


    @PostMapping("/updateImage")
    public Response updateImage(@RequestBody PartnerRequest partner) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();

            Partners partnerSave = partnerService.updatePartnerImage(partner, userDetails);
            return new Response(partnerSave, Constants.message.SUCCESS, 200);
        } catch (Exception e) {
            return new Response(null, Constants.message.SYSTEM_ERROR, 500);
        }
    }

    @GetMapping("/LogDetailPartner")
    public ResponseAPI getLogDetailPartner(@RequestParam UUID id) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            partnerService.getLogDetailPartner(id, userDetails);
            return new ResponseAPI(null, Constants.message.SUCCESS, 200);
        } catch (Exception e) {
            return new ResponseAPI(null, Constants.message.SYSTEM_ERROR, 500);
        }
    }
}
