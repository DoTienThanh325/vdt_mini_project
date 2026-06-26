package com.vdt.documenttransfer.modules.role.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vdt.documenttransfer.modules.role.service.RoleService;
import com.vdt.documenttransfer.modules.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;

    @GetMapping("")
    public ResponseEntity<?> getAll(@AuthenticationPrincipal(expression = "user") User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            return ResponseEntity.ok(roleService.getAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

}
