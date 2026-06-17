package com.vdt.documenttransfer.modules.documentfile.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DocumentFileResponse {
    private Integer id;
    private String originalFileName;
    private String storedFileName;
    private String filePath;
    private String fileType;
    private String documentCode;
    private String documentSummary;
    private Long fileSize;
}
