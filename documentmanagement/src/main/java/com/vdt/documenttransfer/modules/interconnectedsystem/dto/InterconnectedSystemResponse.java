package com.vdt.documenttransfer.modules.interconnectedsystem.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@Builder
public class InterconnectedSystemResponse implements Serializable {
    private Integer id;
    private String systemCode;
    private String systemName;
    private String endpointUrl;
    private String apiKey;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
}
