package com.vdt.documenttransfer.modules.notification.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.vdt.documenttransfer.modules.notification.dto.NotificationRedisDto;
import com.vdt.documenttransfer.modules.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private String buildRedisKey(Integer userId, LocalDate date) {
        return "notifications:user:" + userId + ":date:" + date;
    }

    @Override
    public NotificationRedisDto createNotification(Integer userId, String title,
            String content) {

        LocalDateTime now = LocalDateTime.now();
        NotificationRedisDto notification = NotificationRedisDto.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .title(title)
                .content(content)
                .isRead(false)
                .createdAt(now)
                .build();

        String redisKey = buildRedisKey(userId, now.toLocalDate());

        redisTemplate.opsForList().leftPush(redisKey, notification);

        // Redis tự xóa notification sau 1 ngày
        redisTemplate.expire(redisKey, 1, TimeUnit.DAYS);

        return notification;
    }

    private List<Object> getNotificationsByDate(Integer userId, LocalDate date) {
        String redisKey = buildRedisKey(userId, date);

        List<Object> notifications = redisTemplate.opsForList().range(redisKey, 0, -1);

        if (notifications == null) {
            return List.of();
        }

        return notifications;
    }

    @Override
    public List<Object> getTodayNotifications(Integer userId) {
        return getNotificationsByDate(userId, LocalDate.now());
    }

    @Override
    public NotificationRedisDto updateNotificationStatus(Integer userId, String notificationId, Boolean isRead) {
        LocalDate today = LocalDate.now();
        String redisKey = buildRedisKey(userId, today);
        List<Object> notifications = getNotificationsByDate(userId, today);

        for (int index = 0; index < notifications.size(); index++) {
            NotificationRedisDto notification = toNotificationDto(notifications.get(index));

            if (notificationId.equals(notification.getId())) {
                notification.setIsRead(isRead);
                redisTemplate.opsForList().set(redisKey, index, notification);
                return notification;
            }
        }

        throw new IllegalArgumentException("Notification not found");
    }

    private NotificationRedisDto toNotificationDto(Object notification) {
        if (notification instanceof NotificationRedisDto notificationDto) {
            return notificationDto;
        }

        return objectMapper.convertValue(notification, NotificationRedisDto.class);
    }
}
