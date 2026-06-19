package com.vdt.documenttransfer.modules.documentfile.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.vdt.documenttransfer.modules.documentfile.service.DocumentFileService;
import com.vdt.documenttransfer.modules.user.entity.User;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentFileController {
    private final DocumentFileService documentFileService;

    @PostMapping("/{documentId}/files")
    public ResponseEntity<?> uploadFiles(@AuthenticationPrincipal(expression = "user") User user,
            @RequestParam("files") MultipartFile files[], @PathVariable Integer documentId) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            return ResponseEntity.ok(documentFileService.uploadFiles(documentId, files, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

}
