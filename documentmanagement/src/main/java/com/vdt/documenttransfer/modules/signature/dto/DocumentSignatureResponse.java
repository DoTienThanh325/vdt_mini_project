package com.vdt.documenttransfer.modules.signature.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentSignatureResponse {
	private Integer id;
	private String hashValue;
	private String algorithm;
	private String status;
	private String documentCode;
    private String documentStatus;
	private String signerUsername;
	private LocalDateTime signedAt;
    private String message;
}
