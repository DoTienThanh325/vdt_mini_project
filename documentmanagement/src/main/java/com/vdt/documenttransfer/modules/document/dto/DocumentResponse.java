package com.vdt.documenttransfer.modules.document.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DocumentResponse {
    private Integer id;
    private String documentType;
    private String documentCode;
    private String summary;
    private String status;
    private LocalDateTime creatdAt;
    private LocalDateTime updatedAt;
    private String message;
}
