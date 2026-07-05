package com.vdt.documenttransfer.modules.signature.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdt.documenttransfer.common.util.AesEncryptionUtils;
import com.vdt.documenttransfer.common.util.DocumentToPayload;
import com.vdt.documenttransfer.modules.document.entity.Document;
import com.vdt.documenttransfer.modules.document.repository.DocumentRepository;
import com.vdt.documenttransfer.modules.documentfile.entity.DocumentFile;
import com.vdt.documenttransfer.modules.documentfile.repository.DocumentFileRepository;
import com.vdt.documenttransfer.modules.notification.service.NotificationService;
import com.vdt.documenttransfer.modules.signature.dto.DocumentSignatureResponse;
import com.vdt.documenttransfer.modules.signature.dto.VerifyDocumentSignatureResponse;
import com.vdt.documenttransfer.modules.signature.entity.DocumentSignature;
import com.vdt.documenttransfer.modules.signature.repository.DocumentSignatureRepository;
import com.vdt.documenttransfer.modules.signature.service.DocumentSignatureService;
import com.vdt.documenttransfer.modules.transfer.dto.ExternalDocumentPayload;
import com.vdt.documenttransfer.modules.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentSignatureServiceImpl implements DocumentSignatureService {

    private final DocumentRepository documentRepository;
    private final DocumentFileRepository documentFileRepository;
    private final DocumentSignatureRepository documentSignatureRepository;
    private final NotificationService notificationService;
    private final DocumentToPayload documentToPayload;
    private final ObjectMapper objectMapper;
    private final AesEncryptionUtils aesEncryptionUtils;

    @Value("${interconnection.encryption-key}")
    private String secretKey;

    @Override
    @Transactional
    public DocumentSignatureResponse signDocument(Integer documentId, User leader) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy văn bản"));

        if (document.getStatus() != Document.Status.APPROVED) {
            throw new RuntimeException("Chỉ văn bản đã được phê duyệt mới được ký");
        }

        if (leader.getOrganization().getId() != document.getSenderOrganization().getId()) {
            throw new RuntimeException("Văn bản không thuộc đơn vị liên thông của bạn");
        }

        List<DocumentFile> files = documentFileRepository.findByDocumentId(documentId);

        if (files == null || files.isEmpty()) {
            throw new RuntimeException("Văn bản chưa có file đính kèm, không thể ký");
        }
        ExternalDocumentPayload externalDocumentPayload = documentToPayload.buiPayload(document);
        String json = "";
        try {
            json = objectMapper.writeValueAsString(externalDocumentPayload);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa văn bản" + e);
        }
        String hashValue = aesEncryptionUtils.encrypt(json, secretKey);

        DocumentSignature signature = DocumentSignature.builder()
                .document(document)
                .signer(leader)
                .status(DocumentSignature.Status.PENDING)
                .hashValue(hashValue)
                .algorithm("AES")
                .signedAt(LocalDateTime.now())
                .build();

        DocumentSignature savedSignature = documentSignatureRepository.save(signature);

        document.setStatus(Document.Status.SIGNED);
        document.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(document);

        notificationService.createNotification(document.getCreatedBy().getId(), "Tài liệu, văn bản đã được ký",
                "Tài liệu, văn bản " + document.getDocumentCode() + " - " + document.getSummary() + " đã được "
                        + leader.getFullName() + " ký thành công");

        return DocumentSignatureResponse.builder()
                .id(savedSignature.getId())
                .documentCode(document.getDocumentCode())
                .hashValue(savedSignature.getHashValue())
                .algorithm(savedSignature.getAlgorithm())
                .status(savedSignature.getStatus().name())
                .documentStatus(savedSignature.getDocument().getStatus().name())
                .signerUsername(savedSignature.getSigner().getUsername())
                .signedAt(savedSignature.getSignedAt())
                .message("Ký văn bản thành công")
                .build();
    }

    @Override
    public VerifyDocumentSignatureResponse checkSignatureDocument(Integer documentId, User clerk) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy văn bản"));

        User leader = document.getSignature().getSigner();

        DocumentSignature signature = documentSignatureRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new RuntimeException("Văn bản chưa được ký"));

        if (clerk.getOrganization().getId() != document.getSenderOrganization().getId()) {
            throw new RuntimeException("Văn bản không thuộc đơn vị liên thông của bạn");
        }

        List<DocumentFile> files = documentFileRepository.findByDocumentId(documentId);

        if (files == null || files.isEmpty()) {
            throw new RuntimeException("Văn bản chưa có file đính kèm, không thể kiểm tra");
        }

        ExternalDocumentPayload externalDocumentPayload = documentToPayload.buiPayload(document);
        String json = "";
        try {
            json = objectMapper.writeValueAsString(externalDocumentPayload);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi mã hóa văn bản" + e);
        }
        String storedJson = aesEncryptionUtils.decrypt(signature.getHashValue(), secretKey);

        boolean valid = json.equals(storedJson);

        if (!valid) {
            signature.setStatus(DocumentSignature.Status.INVALID);
            documentSignatureRepository.save(signature);
            throw new RuntimeException("Chữ ký văn bản không hợp lệ");
        }

        signature.setStatus(DocumentSignature.Status.VALID);
        documentSignatureRepository.save(signature);

        return VerifyDocumentSignatureResponse.builder()
                .algorithm(signature.getAlgorithm())
                .documentCode(document.getDocumentCode())
                .signatureStatus(signature.getStatus().name())
                .documentSummary(document.getSummary())
                .signerUsername(leader.getUsername())
                .message("Kiểm tra chữ ký thành công")
                .build();
    }
}