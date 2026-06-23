package com.vdt.documenttransfer.modules.transfer.service.impl;

import com.vdt.documenttransfer.modules.transfer.service.components.ExternalDocumentClient;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vdt.documenttransfer.common.logging.AppLogger;
import com.vdt.documenttransfer.common.util.DocumentSign;
import com.vdt.documenttransfer.modules.document.entity.Document;
import com.vdt.documenttransfer.modules.document.repository.DocumentRepository;
import com.vdt.documenttransfer.modules.documentfile.entity.DocumentFile;
import com.vdt.documenttransfer.modules.interconnectedsystem.entity.InterconnectedSystem;
import com.vdt.documenttransfer.modules.interconnectedsystem.repository.InterconnectedSystemRepository;
import com.vdt.documenttransfer.modules.notification.service.NotificationService;
import com.vdt.documenttransfer.modules.organization.entity.Organization;
import com.vdt.documenttransfer.modules.organization.repository.OrganizationRepository;
import com.vdt.documenttransfer.modules.signature.entity.DocumentSignature;
import com.vdt.documenttransfer.modules.transfer.dto.DocumentTransferResponse;
import com.vdt.documenttransfer.modules.transfer.dto.ExternalDocumentFilePayload;
import com.vdt.documenttransfer.modules.transfer.dto.ExternalDocumentPayload;
import com.vdt.documenttransfer.modules.transfer.dto.ExternalDocumentSignaturePayload;
import com.vdt.documenttransfer.modules.transfer.entity.DocumentTransfer;
import com.vdt.documenttransfer.modules.transfer.repository.DocumentTransferRepository;
import com.vdt.documenttransfer.modules.transfer.service.DocumentTransferService;
import com.vdt.documenttransfer.modules.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentTransferServiceImpl implements DocumentTransferService {
    private final ExternalDocumentClient externalDocumentClient;
    private final DocumentRepository documentRepository;
    private final OrganizationRepository organizationRepository;
    private final DocumentTransferRepository documentTransferRepository;
    private final InterconnectedSystemRepository interconnectedSystemRepository;
    private final NotificationService notificationService;
    private final AppLogger appLogger;

    @Override
    @Transactional
    public DocumentTransferResponse transferDocument(Integer documentId, Integer orgId, User clerk,
            String authorizationHeader) {
        String documentCode = String.valueOf(documentId);
        DocumentTransfer savedTransfer = null;
        try {
            Document document = documentRepository.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy tài liệu"));
            documentCode = document.getDocumentCode();

            Organization receiverOrg = organizationRepository.findById(orgId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị liên thông"));

            InterconnectedSystem receiverSystem = receiverOrg.getSystem();

            if (!clerk.getOrganization().getId().equals(document.getSenderOrganization().getId())) {
                throw new RuntimeException("Tài liệu không thuộc đơn vị của bạn");
            }

            if (document.getSignature() == null) {
                throw new RuntimeException("Tài liệu chưa được ký");
            }

            if (!document.getSignature().getStatus().name().equals("VALID")) {
                throw new RuntimeException("Vui lòng xác minh chữ ký trước khi gửi");
            }

            if (!receiverSystem.getStatus().name().equals("ACTIVE")) {
                throw new RuntimeException("Hệ thống nhận không hoạt động");
            }

            DocumentSignature signature = document.getSignature();

            DocumentTransfer transfer = DocumentTransfer.builder()
                    .document(document)
                    .sender(clerk)
                    .status(DocumentTransfer.Status.PENDING)
                    .sentAt(LocalDateTime.now())
                    .receiverOrganization(receiverOrg)
                    .build();
            savedTransfer = documentTransferRepository.save(transfer);

            try {
                ExternalDocumentPayload payload = buildPayload(document, signature, receiverOrg, savedTransfer);
                externalDocumentClient.sendDocument(receiverSystem, payload, authorizationHeader);

                savedTransfer.setStatus(DocumentTransfer.Status.SENT);
                documentTransferRepository.save(savedTransfer);
                documentRepository.save(document);

            } catch (Exception e) {
                savedTransfer.setStatus(DocumentTransfer.Status.FAILED);
                documentTransferRepository.save(savedTransfer);
                throw new RuntimeException("Gửi tài liệu thất bại " + e.getMessage(), e);
            }

            notificationService.createNotification(document.getCreatedBy().getId(), "Tài liệu, văn bản đã được gửi đi",
                    "Tài liệu, văn bản " + document.getDocumentCode() + " - " + document.getSummary()
                            + " đã được gửi đến " + receiverOrg.getOrgName());

            appLogger.infoDocument("SEND_DOCUMENT", clerk.getId(), document.getDocumentCode(),
                    "Send document successfully");

            return DocumentTransferResponse.builder()
                    .status(savedTransfer.getStatus().name())
                    .senderUsername(savedTransfer.getSender().getUsername())
                    .receiverOrgCode(receiverOrg.getOrgCode())
                    .documentCode(savedTransfer.getDocument().getDocumentCode())
                    .build();
        } catch (RuntimeException e) {
            appLogger.errorDocument("SEND_DOCUMENT", clerk != null ? clerk.getId() : null, documentCode,
                    "Send document failed: " + e.getMessage(), e);
            throw e;
        }
    }

    private ExternalDocumentPayload buildPayload(Document document, DocumentSignature signature,
            Organization receiverOrg, DocumentTransfer transfer) {
        ExternalDocumentSignaturePayload signaturePayload = ExternalDocumentSignaturePayload.builder()
                .hashValue(signature.getHashValue())
                .algorithm(signature.getAlgorithm())
                .signerId(signature.getSigner().getId())
                .signerName(signature.getSigner().getUsername())
                .build();

        List<ExternalDocumentFilePayload> documentFilePayloads = new ArrayList<>();

        for (DocumentFile file : document.getFiles()) {
            ExternalDocumentFilePayload filePayload = ExternalDocumentFilePayload.builder()
                    .originalFileName(file.getOriginalFileName())
                    .storedFileName(file.getStoredFileName())
                    .fileType(file.getFileType())
                    .fileSize(file.getFileSize())
                    .base64Content(encodeFileToBase64(file.getFilePath()))
                    .build();

            documentFilePayloads.add(filePayload);
        }

        return ExternalDocumentPayload.builder()
                .documentTransferId(transfer.getId())
                .documentCode(document.getDocumentCode())
                .documentType(document.getDocumentType())
                .summary(document.getSummary())
                .senderOrgCode(document.getSenderOrganization().getOrgCode())
                .receiverOrgCode(receiverOrg.getOrgCode())
                .files(documentFilePayloads)
                .signature(signaturePayload)
                .build();
    }

    private String encodeFileToBase64(String filePath) {
        try {
            byte[] fileBytes = Files.readAllBytes(Path.of(filePath));
            return Base64.getEncoder().encodeToString(fileBytes);
        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file: " + filePath, e);
        }
    }

    private String saveReceivedFile(ExternalDocumentFilePayload filePayload, String documentCode) {
        try {
            String uploadDir = "uploads/received-documents/" + documentCode;

            Files.createDirectories(Path.of(uploadDir));

            byte[] fileBytes = Base64.getDecoder().decode(filePayload.getBase64Content());

            String storedFileName = filePayload.getStoredFileName();

            Path outputPath = Path.of(uploadDir, storedFileName);

            Files.write(outputPath, fileBytes);

            return outputPath.toString();

        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file nhận được: " + filePayload.getOriginalFileName(), e);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("File base64 không hợp lệ: " + filePayload.getOriginalFileName(), e);
        }
    }

    @Override
    public String receiveDocument(ExternalDocumentPayload payload, String apiKey) {
        String documentCode = payload != null ? payload.getDocumentCode() : null;
        try {
            if (!interconnectedSystemRepository.existsByApiKey(apiKey)) {
                throw new RuntimeException("Api key không hợp lệ");
            }

            Document document = Document.builder()
                    .documentCode(payload.getDocumentCode())
                    .documentType(payload.getDocumentType())
                    .summary(payload.getSummary())
                    .build();

            User leader = User.builder()
                    .id(payload.getSignature().getSignerId())
                    .username(payload.getSignature().getSignerName())
                    .build();

            List<DocumentFile> documentFiles = new ArrayList<>();

            if (payload.getFiles() != null) {
                for (ExternalDocumentFilePayload filePayload : payload.getFiles()) {
                    String filePath = saveReceivedFile(filePayload, documentCode);
                    DocumentFile file = DocumentFile.builder()
                            .storedFileName(filePayload.getStoredFileName())
                            .originalFileName(filePayload.getOriginalFileName())
                            .fileType(filePayload.getFileType())
                            .fileSize(filePayload.getFileSize())
                            .filePath(filePath)
                            .build();
                    documentFiles.add(file);
                }
            }

            String hashValueChecking = DocumentSign.generateDocumentHash(document, documentFiles, leader);

            if (!hashValueChecking.equals(payload.getSignature().getHashValue())) {
                throw new RuntimeException("Chữ ký không hợp lệ");
            }

            appLogger.infoDocument("RECEIVE_DOCUMENT", leader.getId(), payload.getDocumentCode(),
                    "Receive document successfully");

            return "Gửi thành công";
        } catch (RuntimeException e) {
            appLogger.errorDocument("RECEIVE_DOCUMENT", null, documentCode,
                    "Receive document failed: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public DocumentTransferResponse accessReceiveDocument(Integer documentId, User clerk, Integer receiverOrgId) {
        String documentCode = String.valueOf(documentId);
        try {
            DocumentTransfer transfer = documentTransferRepository
                    .findByDocument_IdAndReceiverOrganization_Id(documentId, receiverOrgId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin gửi"));
            documentCode = transfer.getDocument().getDocumentCode();

            if (!clerk.getOrganization().getId().equals(receiverOrgId)) {
                throw new RuntimeException("Bạn không phải là người nhận của đơn vị này");
            }

            transfer.setReceiver(clerk);
            transfer.setReceivedAt(LocalDateTime.now());
            transfer.setStatus(DocumentTransfer.Status.RECEIVED);

            documentTransferRepository.save(transfer);

            appLogger.infoDocument("CONFIRM_RECEIVE_DOCUMENT", clerk.getId(), documentCode,
                    "Confirm receive document successfully");

            return DocumentTransferResponse.builder()
                    .status(transfer.getStatus().name())
                    .senderUsername(transfer.getSender().getUsername())
                    .receiverOrgCode(transfer.getReceiverOrganization().getOrgCode())
                    .receiverUsername(clerk.getUsername())
                    .documentCode(transfer.getDocument().getDocumentCode())
                    .build();
        } catch (RuntimeException e) {
            appLogger.errorDocument("CONFIRM_RECEIVE_DOCUMENT", clerk != null ? clerk.getId() : null, documentCode,
                    "Confirm receive document failed: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public DocumentTransferResponse responseReceiveDocument(Integer documentId, User manager, Integer receiverOrgId,
            Map<String, String> request) {
        String documentCode = String.valueOf(documentId);
        try {
            DocumentTransfer transfer = documentTransferRepository
                    .findByDocument_IdAndReceiverOrganization_Id(documentId, receiverOrgId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin gửi"));
            documentCode = transfer.getDocument().getDocumentCode();

            if (!transfer.getStatus().name().equals("RECEIVED")) {
                throw new RuntimeException("Tài liệu này chưa được xác nhận");
            }

            if (!manager.getOrganization().getId().equals(receiverOrgId)) {
                throw new RuntimeException("Bạn không phải là người nhận của đơn vị này");
            }

            String responseContent = request.get("responseContent");

            transfer.setResponseContent(responseContent);
            transfer.setStatus(DocumentTransfer.Status.RESPONDED);
            transfer.setRespondedAt(LocalDateTime.now());
            documentTransferRepository.save(transfer);

            appLogger.infoDocument("RESPOND_DOCUMENT", manager.getId(), documentCode,
                    "Respond document successfully");

            return DocumentTransferResponse.builder()
                    .responseContent(responseContent)
                    .status(transfer.getStatus().name())
                    .senderUsername(transfer.getSender().getUsername())
                    .receiverOrgCode(transfer.getReceiverOrganization().getOrgCode())
                    .receiverUsername(manager.getUsername())
                    .documentCode(transfer.getDocument().getDocumentCode())
                    .build();
        } catch (RuntimeException e) {
            appLogger.errorDocument("RESPOND_DOCUMENT", manager != null ? manager.getId() : null, documentCode,
                    "Respond document failed: " + e.getMessage(), e);
            throw e;
        }
    }
}
