package com.vdt.documenttransfer.modules.transfer.service;

import java.util.*;

import com.vdt.documenttransfer.common.response.PageResponse;
import com.vdt.documenttransfer.modules.document.dto.DocumentResponse;
import com.vdt.documenttransfer.modules.transfer.dto.DocumentTransferResponse;
import com.vdt.documenttransfer.modules.user.entity.User;

public interface DocumentTransferService {
    DocumentTransferResponse transferDocument(Integer documentId, Integer orgId, User clerk,
            String authorizationHeader);

    String receiveDocument(Map<String, String> payload, String apiKey);

    DocumentTransferResponse accessReceiveDocument(Integer documentId, User clerk, Integer receiverOrgId);

    DocumentTransferResponse responseReceiveDocument(Integer documentId, User manager, Integer receiverOrgId, Map<String, String> request);

    PageResponse<DocumentResponse> findAllByReceiverOrgId(Integer orgId, int page, int size);

    PageResponse<DocumentResponse> findAllByStatusAndReceiverOrgId(String status, Integer orgId, int page, int size);
}
