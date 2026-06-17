package com.vdt.documenttransfer.modules.signature.service;

import com.vdt.documenttransfer.modules.signature.dto.DocumentSignatureResponse;
import com.vdt.documenttransfer.modules.signature.dto.VerifyDocumentSignatureResponse;
import com.vdt.documenttransfer.modules.user.entity.User;

public interface DocumentSignatureService {
    DocumentSignatureResponse signDocument(Integer documentId, User leader);
    VerifyDocumentSignatureResponse checkSignatureDocument(Integer documentId, User clerk);
}
