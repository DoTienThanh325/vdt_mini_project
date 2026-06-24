package com.vdt.documenttransfer.modules.document.service;


import com.vdt.documenttransfer.common.response.PageResponse;
import com.vdt.documenttransfer.modules.document.dto.DocumentResponse;
import com.vdt.documenttransfer.modules.document.dto.NewDocumentRequest;
import com.vdt.documenttransfer.modules.user.entity.User;

public interface DocumentService {
    DocumentResponse createNewDocument(Integer userId, Integer orgId, NewDocumentRequest request);
    DocumentResponse approveNewDocument(Integer documentId, User user);
    DocumentResponse rejectNewDocument(Integer documentId, User user);
    PageResponse<DocumentResponse> findByStatusAndSenderOrg(String status, int orgId, int page, int size);
    DocumentResponse findById(Integer documentId);
    PageResponse<DocumentResponse> findAllByOrg(int page, int size, Integer senderOrgId);
    PageResponse<DocumentResponse> findAllByStaff(int staffId, int page, int size);
}
