package com.vdt.documenttransfer.modules.document.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vdt.documenttransfer.modules.document.dto.NewDocumentRequest;
import com.vdt.documenttransfer.modules.document.service.DocumentService;
import com.vdt.documenttransfer.modules.user.entity.User;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/documents")
public class DocumentController {
    private final DocumentService documentService;

    @PostMapping("/new")
    public ResponseEntity<?> createNewDocument(@AuthenticationPrincipal(expression = "user") User user,
            @RequestBody NewDocumentRequest request) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            Integer userId = user.getId();
            Integer orgId = user.getOrganization().getId();

            return ResponseEntity.ok(documentService.createNewDocument(userId, orgId, request));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/{documentId}/approve")
    public ResponseEntity<?> approveNewDocument(@AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Integer documentId) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            return ResponseEntity.ok(documentService.approveNewDocument(documentId, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/{documentId}/reject")
    public ResponseEntity<?> rejectNewDocument(@AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Integer documentId) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            return ResponseEntity.ok(documentService.rejectNewDocument(documentId, user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/search")
    public ResponseEntity<?> getByStatusAndTypeAndSenderOrg(@AuthenticationPrincipal(expression = "user") User user,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestBody Map<String, String> request) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }
            Integer orgId = user.getOrganization().getId();
            return ResponseEntity.ok(documentService.findByStatusAndTypeAndSenderOrg(request, orgId, page, size));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<?> getDocumentInfo(@AuthenticationPrincipal(expression = "user") User user,
            @PathVariable Integer documentId) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }

            return ResponseEntity.ok(documentService.findById(documentId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("")
    public ResponseEntity<?> getAllBySenderOrg(@AuthenticationPrincipal(expression = "user") User user,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }
            Integer orgId = user.getOrganization().getId();
            return ResponseEntity.ok(documentService.findAllByOrg(page, size, orgId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/createdBy")
    public ResponseEntity<?> getAllDocumentCreatedByUser(@AuthenticationPrincipal(expression = "user") User staff,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            if (staff == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!staff.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }
            Integer staffId = staff.getId();
            return ResponseEntity.ok(documentService.findAllByStaff(staffId, page, size));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/delete/{documentId}")
    @Transactional
    public ResponseEntity<?> deleteById(@AuthenticationPrincipal(expression = "user") User user, @PathVariable Integer documentId) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }

            if (!user.getStatus().name().equals("ACTIVE")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản của bạn chưa được kích hoạt");
            }
            documentService.deleteByDocument_Id(documentId);
            return ResponseEntity.ok("Xóa văn bản, tài liệu thành công");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }
}
