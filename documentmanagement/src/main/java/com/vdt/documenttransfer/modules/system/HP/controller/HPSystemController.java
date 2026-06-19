package com.vdt.documenttransfer.modules.system.HP.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// import com.vdt.documenttransfer.modules.system.HP.service.HPSystemService;
import com.vdt.documenttransfer.modules.transfer.dto.ExternalDocumentPayload;
import com.vdt.documenttransfer.modules.transfer.service.DocumentTransferService;
import com.vdt.documenttransfer.modules.user.entity.User;

import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interconnect/hp/receive")
public class HPSystemController {
    private final DocumentTransferService documentTransferService;

    @PostMapping("")
    public ResponseEntity<?> receiveDocument(@RequestHeader("X-API-KEY") String apiKey,
            @RequestBody ExternalDocumentPayload payload, @AuthenticationPrincipal(expression = "user") User clerk) {
        try {
            if (clerk == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            String message = documentTransferService.receiveDocument(payload, apiKey);
            return ResponseEntity.ok(Map.of(
                    "status", "RECEIVED",
                    "message", message));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
