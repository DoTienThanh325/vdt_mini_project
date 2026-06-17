package com.vdt.documenttransfer.modules.signature.service.impl;

import com.vdt.documenttransfer.modules.document.entity.Document;
import com.vdt.documenttransfer.modules.document.repository.DocumentRepository;
import com.vdt.documenttransfer.modules.documentfile.entity.DocumentFile;
import com.vdt.documenttransfer.modules.documentfile.repository.DocumentFileRepository;
import com.vdt.documenttransfer.modules.signature.dto.DocumentSignatureResponse;
import com.vdt.documenttransfer.modules.signature.dto.VerifyDocumentSignatureResponse;
import com.vdt.documenttransfer.modules.signature.entity.DocumentSignature;
import com.vdt.documenttransfer.modules.signature.repository.DocumentSignatureRepository;
import com.vdt.documenttransfer.modules.signature.service.DocumentSignatureService;
import com.vdt.documenttransfer.modules.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentSignatureServiceImpl implements DocumentSignatureService {

    private final DocumentRepository documentRepository;
    private final DocumentFileRepository documentFileRepository;
    private final DocumentSignatureRepository documentSignatureRepository;

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

        String hashValue = generateDocumentHash(document, files, leader);

        DocumentSignature signature = DocumentSignature.builder()
                .document(document)
                .signer(leader)
                .status(DocumentSignature.Status.PENDING)
                .hashValue(hashValue)
                .algorithm("SHA-256")
                .signedAt(LocalDateTime.now())
                .build();

        DocumentSignature savedSignature = documentSignatureRepository.save(signature);

        document.setStatus(Document.Status.SIGNED);
        document.setUpdatedAt(LocalDateTime.now());
        documentRepository.save(document);

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

    private String generateDocumentHash(Document document, List<DocumentFile> files, User leader) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            digest.update(document.getDocumentCode().getBytes());
            digest.update(document.getDocumentType().getBytes());

            if (document.getSummary() != null) {
                digest.update(document.getSummary().getBytes());
            }

            for (DocumentFile file : files) {
                digest.update(file.getStoredFileName().getBytes());
                digest.update(String.valueOf(file.getFileSize()).getBytes());

                Path path = Path.of(file.getFilePath());

                if (!Files.exists(path)) {
                    throw new RuntimeException("Không tìm thấy file vật lý: " + file.getFilePath());
                }

                try (InputStream inputStream = Files.newInputStream(path)) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        digest.update(buffer, 0, bytesRead);
                    }
                }
            }

            digest.update(String.valueOf(leader.getId()).getBytes());

            byte[] hashBytes = digest.digest();

            return HexFormat.of().formatHex(hashBytes);

        } catch (Exception e) {
            throw new RuntimeException("Tạo chữ ký văn bản thất bại: " + e.getMessage());
        }
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

        String currentHash = generateDocumentHash(document, files, leader);

        boolean valid = currentHash.equals(signature.getHashValue());

        if (!valid) {
            signature.setStatus(DocumentSignature.Status.INVALID);
            documentSignatureRepository.save(signature);
            throw new RuntimeException("Chữ ký văn bản không hợp lệ");
        }

        signature.setStatus(DocumentSignature.Status.VALID);
        documentSignatureRepository.save(signature);

        return VerifyDocumentSignatureResponse.builder()
                    .hashValue(currentHash)
                    .algorithm(signature.getAlgorithm())
                    .documentCode(document.getDocumentCode())
                    .signatureStatus(signature.getStatus().name())
                    .documentSummary(document.getSummary())
                    .signerUsername(leader.getUsername())
                    .message("Kiểm tra chữ ký thành công")
                    .build();
    }
}