package com.vdt.documenttransfer.modules.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalDocumentPayload {
    private Integer documentTransferId;
    
    private String documentCode;

    private String documentType;

    private String summary;

    private String senderOrgCode;

    private String receiverOrgCode;

    private List<ExternalDocumentFilePayload> files;

    private ExternalDocumentSignaturePayload signature;
}