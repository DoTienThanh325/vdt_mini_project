package com.vdt.documenttransfer.modules.notification.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import com.vdt.documenttransfer.modules.notification.dto.NotificationRedisDto;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTests {
    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ListOperations<String, Object> listOperations;

    private NotificationServiceImpl notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationServiceImpl(redisTemplate);
        when(redisTemplate.opsForList()).thenReturn(listOperations);
    }

    @Test
    void updateNotificationStatusUpdatesOwnedNotification() {
        String redisKey = "notifications:user:7:date:" + LocalDate.now();
        NotificationRedisDto notification = NotificationRedisDto.builder()
                .id("notification-id")
                .userId(7)
                .title("Title")
                .content("Content")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        when(listOperations.range(redisKey, 0, -1)).thenReturn(List.of(notification));

        NotificationRedisDto result = notificationService.updateNotificationStatus(7, "notification-id", true);

        assertTrue(result.getIsRead());
        verify(listOperations).set(redisKey, 0, notification);
    }

    @Test
    void updateNotificationStatusRejectsUnknownNotification() {
        String redisKey = "notifications:user:7:date:" + LocalDate.now();
        when(listOperations.range(redisKey, 0, -1)).thenReturn(List.of());

        assertThrows(
                IllegalArgumentException.class,
                () -> notificationService.updateNotificationStatus(7, "unknown-id", true));
    }
}
