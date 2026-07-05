package com.vdt.documenttransfer.modules.signature.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyDocumentSignatureResponse {
	private String algorithm;
    private String signatureStatus;
	private String documentCode;
    private String documentSummary;
	private String signerUsername;
    private String message;
}
