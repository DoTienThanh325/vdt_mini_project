package com.vdt.documenttransfer.modules.transfer.service;

import java.util.*;

import com.vdt.documenttransfer.modules.transfer.dto.DocumentTransferResponse;
import com.vdt.documenttransfer.modules.transfer.dto.ExternalDocumentPayload;
import com.vdt.documenttransfer.modules.user.entity.User;

public interface DocumentTransferService {
    DocumentTransferResponse transferDocument(Integer documentId, Integer orgId, User clerk,
            String authorizationHeader);

    String receiveDocument(ExternalDocumentPayload payload, String apiKey);

    DocumentTransferResponse accessReceiveDocument(Integer documentId, User clerk, Integer receiverOrgId);

    DocumentTransferResponse responseReceiveDocument(Integer documentId, User manager, Integer receiverOrgId, Map<String, String> request);
}
