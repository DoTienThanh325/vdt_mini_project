package com.vdt.documenttransfer.modules.document.service;


import com.vdt.documenttransfer.modules.document.dto.DocumentResponse;
import com.vdt.documenttransfer.modules.document.dto.NewDocumentRequest;
import com.vdt.documenttransfer.modules.user.entity.User;

public interface DocumentService {
    DocumentResponse createNewDocument(Integer userId, Integer orgId, NewDocumentRequest request);
    DocumentResponse approveNewDocument(Integer documentId, User user);
    DocumentResponse rejectNewDocument(Integer documentId, User user);
}
