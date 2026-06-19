package com.vdt.documenttransfer.modules.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalDocumentSignaturePayload {

    private String hashValue;

    private String algorithm;

    private Integer signerId;

    private String signerName;
}