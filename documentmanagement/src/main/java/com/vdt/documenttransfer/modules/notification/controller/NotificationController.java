package com.vdt.documenttransfer.modules.notification.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vdt.documenttransfer.modules.notification.dto.NotificationRedisDto;
import com.vdt.documenttransfer.modules.notification.dto.UpdateNotificationStatusRequest;
import com.vdt.documenttransfer.modules.notification.service.NotificationService;
import com.vdt.documenttransfer.modules.user.entity.User;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping("/today")
    public ResponseEntity<?> getAllNotificationsToday(@AuthenticationPrincipal(expression = "user") User user) {
        try {
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
            }
            Integer userId = user.getId();

            return ResponseEntity.ok(notificationService.getTodayNotifications(userId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/{notificationId}/status")
    public ResponseEntity<?> updateNotificationStatus(
            @PathVariable String notificationId,
            @Valid @RequestBody UpdateNotificationStatusRequest request,
            @AuthenticationPrincipal(expression = "user") User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User is not authenticated");
        }

        try {
            NotificationRedisDto notification = notificationService.updateNotificationStatus(
                    user.getId(),
                    notificationId,
                    request.getIsRead());
            return ResponseEntity.ok(notification);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
