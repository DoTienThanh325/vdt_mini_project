package com.vdt.documenttransfer.modules.interconnectedsystem.controller;

import com.vdt.documenttransfer.modules.interconnectedsystem.dto.NewInterconnectedSystemRequest;
import com.vdt.documenttransfer.modules.interconnectedsystem.dto.UpdateInterconnectedSystemRequest;
import com.vdt.documenttransfer.modules.interconnectedsystem.service.InterconnectedSystemService;
import com.vdt.documenttransfer.modules.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interconnected-systems")
public class InterconnectedSystemController {
    private final InterconnectedSystemService interconnectedSystemService;

    @GetMapping("")
    public ResponseEntity<?> getAll(@AuthenticationPrincipal(expression = "user") User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            return ResponseEntity.ok(interconnectedSystemService.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/new")
    public ResponseEntity<?> createNewEntity(@RequestBody NewInterconnectedSystemRequest request, 
            @AuthenticationPrincipal(expression = "user") User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            return ResponseEntity.ok(interconnectedSystemService.createNew(request, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Integer id,
            @RequestBody UpdateInterconnectedSystemRequest request, 
            @AuthenticationPrincipal(expression = "user") User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            return ResponseEntity.ok(interconnectedSystemService.update(id, request, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

}
