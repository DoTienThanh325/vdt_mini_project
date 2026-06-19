package com.vdt.documenttransfer.modules.notification.dto;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRedisDto implements Serializable {
    private String id;

    private Integer userId;

    private String title;

    private String content;

    private Boolean isRead;

    private LocalDateTime createdAt;
}