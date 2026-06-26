package com.vdt.documenttransfer.modules.transfer.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vdt.documenttransfer.modules.transfer.service.DocumentTransferService;
import com.vdt.documenttransfer.modules.user.entity.User;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentTransferController {
    private final DocumentTransferService documentTransferService;

    @PostMapping("/{documentId}/transfer")
    public ResponseEntity<?> transferDocument(@PathVariable Integer documentId,
            @AuthenticationPrincipal(expression = "user") User clerk, @RequestBody Map<String, Integer> request,
            @RequestHeader("Authorization") String authorizationHeader) {
        try {
            if (clerk == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!clerk.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            Integer orgId = request.get("receiverOrgId");
            return ResponseEntity
                    .ok(documentTransferService.transferDocument(documentId, orgId, clerk, authorizationHeader));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/{documentId}/receive")
    public ResponseEntity<?> accessReceiveDocument(@PathVariable Integer documentId,
            @AuthenticationPrincipal(expression = "user") User clerk) {
        try {
            if (clerk == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!clerk.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            Integer orgId = clerk.getOrganization().getId();
            return ResponseEntity
                    .ok(documentTransferService.accessReceiveDocument(documentId, clerk, orgId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/{documentId}/response")
    public ResponseEntity<?> responseReceiveDocument(@PathVariable Integer documentId,
            @AuthenticationPrincipal(expression = "user") User manager, @RequestBody Map<String, String> request) {
        try {
            if (manager == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }
            if (!manager.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }
            Integer orgId = manager.getOrganization().getId();
            return ResponseEntity
                    .ok(documentTransferService.responseReceiveDocument(documentId, manager, orgId, request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/receive")
    public ResponseEntity<?> getAllReceiveDocumentByOrg(@AuthenticationPrincipal(expression = "user") User user,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }
            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }
            Integer orgId = user.getOrganization().getId();
            return ResponseEntity
                    .ok(documentTransferService.findAllByReceiverOrgId(orgId, page, size));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/receive/status/{status}")
    public ResponseEntity<?> getAllReceiveDocumentByStatusAndOrg(
            @AuthenticationPrincipal(expression = "user") User user,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @PathVariable String status) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }
            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }
            Integer orgId = user.getOrganization().getId();
            return ResponseEntity
                    .ok(documentTransferService.findAllByStatusAndReceiverOrgId(status, orgId, page, size));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    
}
