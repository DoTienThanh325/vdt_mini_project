package com.vdt.documenttransfer.modules.organization.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vdt.documenttransfer.modules.organization.dto.NewOrgRequest;
import com.vdt.documenttransfer.modules.organization.dto.UpdateOrgRequest;
import com.vdt.documenttransfer.modules.organization.service.OrganizationService;
import com.vdt.documenttransfer.modules.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationController {
    private final OrganizationService organizationService;

    @GetMapping("")
    public ResponseEntity<?> getAll(@AuthenticationPrincipal(expression = "user") User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            return ResponseEntity.ok(organizationService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/new")
    public ResponseEntity<?> signNewOrg(@RequestBody NewOrgRequest request,
            @AuthenticationPrincipal(expression = "user") User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            return ResponseEntity.ok(organizationService.createNew(request, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/")
    public ResponseEntity<?> updateOrg(@RequestBody UpdateOrgRequest request, @PathVariable("id") Integer id,
            @AuthenticationPrincipal(expression = "user") User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            return ResponseEntity.ok(organizationService.updateOrg(request, id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> activeOrg(@PathVariable("id") Integer id,
            @AuthenticationPrincipal(expression = "user") User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            return ResponseEntity.ok(organizationService.accessNewOrg(id, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
