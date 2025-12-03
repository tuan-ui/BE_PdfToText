package com.noffice.controller;

import com.noffice.ultils.Constants;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.noffice.entity.User;
import com.noffice.reponse.ResponseAPI;
import com.noffice.service.NotificationService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

//    @GetMapping("/getNotificationsByToken")
//    public ResponseEntity<ResponseAPI> getNotificationsByToken(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            HttpServletRequest request) {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//            if (authentication == null ||
//                authentication instanceof AnonymousAuthenticationToken ||
//                !authentication.isAuthenticated() ||
//                authentication.getPrincipal() == null) {
//                return ResponseEntity.status(HttpStatus.OK)
//                        .body(new ResponseAPI(null, "Không có token hoặc phiên đăng nhập hợp lệ", 401));
//            }
//
//            if (!(authentication.getPrincipal() instanceof User)) {
//                return ResponseEntity.status(HttpStatus.OK)
//                        .body(new ResponseAPI(null, "Thông tin người dùng không hợp lệ", 401));
//            }
//
//            User user = (User) authentication.getPrincipal();
//
//            Pageable pageable = PageRequest.of(page, size);
//            Page<Notification> notifications = notificationService.getNotificationsByRecipientId(user.getId(), pageable);
//
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseAPI(notifications, Constants.message.SUCCESS, 200));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseAPI(null, "Lỗi hệ thống: " + e.getMessage(), 500));
//        }
//    }
    
//    @GetMapping("/getNotificationsCountByToken")
//    public ResponseEntity<ResponseAPI> getNotificationsCountByToken(HttpServletRequest request) {
//        try {
//            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//            if (authentication == null ||
//                authentication instanceof AnonymousAuthenticationToken ||
//                !authentication.isAuthenticated() ||
//                authentication.getPrincipal() == null) {
//                return ResponseEntity.status(HttpStatus.OK)
//                        .body(new ResponseAPI(null, "Không có token hoặc phiên đăng nhập hợp lệ", 401));
//            }
//
//            if (!(authentication.getPrincipal() instanceof User)) {
//                return ResponseEntity.status(HttpStatus.OK)
//                        .body(new ResponseAPI(null, "Thông tin người dùng không hợp lệ", 401));
//            }
//
//            User user = (User) authentication.getPrincipal();
//            if(user.getUserId() == null) {
//            	return ResponseEntity.status(HttpStatus.OK)
//                        .body(new ResponseAPI(null, "Token không có Role User Dept Id", 401));
//            }
//            Long recipientId = user.getUserId();
//
//
//            int notificationCount = notificationService.countNotificationsByRecipientId(recipientId);
//
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseAPI(notificationCount, Constants.message.SUCCESS, 200));
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.OK)
//                    .body(new ResponseAPI(null, "Lỗi hệ thống: " + e.getMessage(), 500));
//        }
//    }
    
    @GetMapping("/toggleUpdateIsReadNotification")
    public ResponseEntity<ResponseAPI> toggleUpdateIsReadNotification(
            @RequestParam(required = true) Long notificationId,
            HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || 
                authentication instanceof AnonymousAuthenticationToken || 
                !authentication.isAuthenticated() || 
                authentication.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseAPI(null, "Không có token hoặc phiên đăng nhập hợp lệ", 401));
            }

            if (!(authentication.getPrincipal() instanceof User)) {
                return ResponseEntity.status(HttpStatus.OK)
                        .body(new ResponseAPI(null, "Thông tin người dùng không hợp lệ", 401));
            }
            notificationService.updateReadNotification(notificationId);
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, Constants.message.SUCCESS, 200));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(new ResponseAPI(null, "Lỗi hệ thống: " + e.getMessage(), 500));
        }
    }
}