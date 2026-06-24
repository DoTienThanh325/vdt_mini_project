package com.vdt.documenttransfer.modules.document.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.vdt.documenttransfer.modules.documentfile.dto.DocumentFileResponse;
import com.vdt.documenttransfer.modules.transfer.dto.DocumentTransferResponse;

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
    private List<DocumentFileResponse> files;
    private List<DocumentTransferResponse> tranfers;
    private LocalDateTime creatdAt;
    private LocalDateTime updatedAt;
    private String message;
}
