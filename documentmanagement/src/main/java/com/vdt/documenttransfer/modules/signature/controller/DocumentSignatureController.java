package com.vdt.documenttransfer.modules.signature.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vdt.documenttransfer.modules.signature.service.DocumentSignatureService;
import com.vdt.documenttransfer.modules.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentSignatureController {
    private final DocumentSignatureService documentSignatureService;

    @PostMapping("/{documentId}/sign")
    public ResponseEntity<?> signDocument(@AuthenticationPrincipal(expression = "user") User leader,
            @PathVariable Integer documentId) {
        try {
            if (leader == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!leader.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            return ResponseEntity.ok(documentSignatureService.signDocument(documentId, leader));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/{documentId}/sign/check")
    public ResponseEntity<?> checkSignDocument(@AuthenticationPrincipal(expression = "user") User clerk,
            @PathVariable Integer documentId) {
        try {
            if (clerk == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!clerk.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            return ResponseEntity.ok(documentSignatureService.checkSignatureDocument(documentId, clerk));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
