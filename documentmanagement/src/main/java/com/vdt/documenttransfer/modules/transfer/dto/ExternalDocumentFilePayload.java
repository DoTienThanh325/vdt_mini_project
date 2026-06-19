package com.vdt.documenttransfer.modules.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalDocumentFilePayload {

    private String originalFileName;

    private String storedFileName;

    private String fileType;

    private Long fileSize;

    private String base64Content;
}