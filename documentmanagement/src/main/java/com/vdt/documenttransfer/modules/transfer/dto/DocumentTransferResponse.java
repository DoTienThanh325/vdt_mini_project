package com.vdt.documenttransfer.modules.transfer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentTransferResponse {
    private String responseContent;
    private String status;
    private String senderUsername;
    private String receiverOrgCode;
    private String receiverUsername;
    private String documentCode;
}
