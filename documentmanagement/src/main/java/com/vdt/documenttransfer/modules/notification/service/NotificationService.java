package com.vdt.documenttransfer.modules.notification.service;

import java.util.List;

import com.vdt.documenttransfer.modules.notification.dto.NotificationRedisDto;

public interface NotificationService {
    NotificationRedisDto createNotification(Integer userId, String title, String content);

    List<Object> getTodayNotifications(Integer userId);
}
