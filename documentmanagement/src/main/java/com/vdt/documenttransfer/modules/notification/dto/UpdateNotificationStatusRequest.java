package com.vdt.documenttransfer.modules.notification.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateNotificationStatusRequest {
    @NotNull(message = "isRead is required")
    private Boolean isRead;
}
