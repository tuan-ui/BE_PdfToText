package com.noffice.controller;

import com.noffice.entity.User;
import com.noffice.reponse.ResponseAPI;
import com.noffice.repository.RolePermissionsRepository;
import com.noffice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
	@Autowired
	private RoleRepository rolesRepository;
	@Autowired
	private RolePermissionsRepository permissionsRolesRepository;
    
    @GetMapping("/others")
    public ResponseEntity<ResponseAPI> getFirstLeftLineChart() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();

            Map<String, Object> categories =new HashMap<>();

            Map<String, Object> stats = new HashMap<>();
            stats.put("others", categories);
            return ResponseEntity.ok(
                    ResponseAPI
                            .builder().status(200).message("SUCCESS").object(stats)
                            .build()
            );
        }
        catch(Exception ex) {
            System.out.println("Error : " + ex.getMessage());
            return ResponseEntity.ok(
                    ResponseAPI.builder().message("Internal error").status(500).build()
            );
        }
    }



}
