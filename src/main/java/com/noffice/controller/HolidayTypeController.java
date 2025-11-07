package com.noffice.controller;

import com.noffice.dto.DeleteMultiDTO;
import com.noffice.entity.HolidayType;
import com.noffice.entity.User;
import com.noffice.reponse.ErrorListResponse;
import com.noffice.reponse.ResponseAPI;
import com.noffice.service.HolidayTypeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/api/holiday-type")
public class HolidayTypeController {
    @Autowired
    private HolidayTypeService holidayTypeService;

    @GetMapping("/search")
    public ResponseAPI getHolidayTypesWithPagination(
            @RequestParam(value = "page", required = false, defaultValue = "0") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "holidayTypeCode", required = false) String holidayTypeCode,
            @RequestParam(value = "holidayTypeName", required = false) String holidayTypeName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "searchString", required = false) String searchString
    ) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            Pageable pageable = PageRequest.of(page, size);
            Page<HolidayType> holidayTypePage = holidayTypeService.searchHolidayTypes(searchString, holidayTypeCode, holidayTypeName, description, pageable, userDetails.getPartnerId());
            return new ResponseAPI(holidayTypePage, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<ResponseAPI> deleteHolidayType(@RequestParam(value = "id") UUID id,
                                                         @RequestParam(value = "version") Long version) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User token = (User) authentication.getPrincipal();
            String message = holidayTypeService.deleteHolidayType(id, token, version);
            if(message!=null && !message.isEmpty())
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, message, 400));
            else
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }

    @PostMapping("/deleteMulti")
    public ResponseEntity<ResponseAPI> deleteMultiHolidayType(@RequestBody List<DeleteMultiDTO> id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User token = (User) authentication.getPrincipal();
            String result = holidayTypeService.deleteMultiHolidayType(id, token);
            if(result!=null && !result.isEmpty())
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, result, 400));
            else
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }

    @PostMapping("/lock")
    public ResponseEntity<ResponseAPI> lockHolidayType(@RequestParam UUID id,
                                                       @RequestParam(value = "version") Long version) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User token = (User) authentication.getPrincipal();
            String message = holidayTypeService.lockHolidayType(id,token, version);
            if(message==null)
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "false", 400));
            else
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "success", 200));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseAPI(null, "Lỗi hệ thống", 500));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseAPI> createHolidayType(@Valid @RequestBody HolidayType request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User token = (User) authentication.getPrincipal();
            String result = holidayTypeService.createHolidayType(request, token);
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
    public ResponseEntity<ResponseAPI> updateHolidayType(@Valid @RequestBody HolidayType request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User token = (User) authentication.getPrincipal();
            String result = holidayTypeService.updateHolidayType(request, token);
            if (result == null || !result.trim().isEmpty())
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseAPI(null, result, 400)); // Trả về 400
            else
                return ResponseEntity.status(HttpStatus.OK).body(new ResponseAPI(null, "Thêm mới thành công", 200));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @GetMapping("/getAllHolidayType")
    public ResponseAPI getAllHolidayType() {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            List<HolidayType> holidayTypes = holidayTypeService.getAllHolidayType(userDetails.getPartnerId());
            return new ResponseAPI(holidayTypes, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }

    @GetMapping("/LogDetailHolidayType")
    public ResponseAPI LogDetailHolidayType(@RequestParam String id) {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User userDetails = (User) authentication.getPrincipal();
            holidayTypeService.LogDetailHolidayType(id, userDetails);
            return new ResponseAPI(null, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }

    @PostMapping("/checkDeleteMulti")
    public ResponseAPI checkDeleteMulti(@RequestBody List<DeleteMultiDTO> ids) {
        try {
            ErrorListResponse message = holidayTypeService.checkDeleteMulti(ids);
            return new ResponseAPI(message, "success", 200);
        } catch (Exception e) {
            return new ResponseAPI(null, "fail", 400);
        }
    }
}
