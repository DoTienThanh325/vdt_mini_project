package com.vdt.documenttransfer.modules.document.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.vdt.documenttransfer.modules.document.dto.DocumentResponse;
import com.vdt.documenttransfer.modules.document.entity.Document;
import com.vdt.documenttransfer.modules.documentfile.dto.DocumentFileResponse;
import com.vdt.documenttransfer.modules.documentfile.entity.DocumentFile;
import com.vdt.documenttransfer.modules.transfer.dto.DocumentTransferResponse;
import com.vdt.documenttransfer.modules.transfer.entity.DocumentTransfer;

@Component
public class EntityToDTO {
    public DocumentResponse entityToResponseSelectFilesAndTransfers(Document document, String message) {
        List<DocumentFile> documentFiles = document.getFiles() != null ? document.getFiles() : null;
        List<DocumentTransfer> documentTransfers = document.getTransfers() != null ? document.getTransfers()
                : null;
        List<DocumentFileResponse> files = new ArrayList<>();
        List<DocumentTransferResponse> transfers = new ArrayList<>();

        if (documentFiles != null) {
            files = documentFiles.stream().map(file -> DocumentFileResponse.builder().id(file.getId())
                    .originalFileName(file.getOriginalFileName())
                    .storedFileName(file.getStoredFileName())
                    .filePath(file.getFilePath())
                    .fileType(file.getFileType())
                    .fileSize(file.getFileSize()).build()).toList();
        }

        if (documentTransfers != null) {
            transfers = documentTransfers.stream().map(tranfer -> DocumentTransferResponse.builder()
                    .responseContent(tranfer.getResponseContent())
                    .status(tranfer.getStatus().name())
                    .senderUsername(tranfer.getSender().getUsername())
                    .receiverOrgCode(tranfer.getReceiverOrganization().getOrgCode())
                    .receiverUsername(tranfer.getReceiver().getUsername())
                    .build())
                    .toList();
        }

        return DocumentResponse.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .documentCode(document.getDocumentCode())
                .summary(document.getSummary())
                .status(document.getStatus().name())
                .files(files)
                .tranfers(transfers)
                .creatdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .message(message)
                .build();
    }

    public DocumentResponse entityToResponseNotSelectFilesAndTransfers(Document document, String message) {
        return DocumentResponse.builder()
                .id(document.getId())
                .documentType(document.getDocumentType())
                .documentCode(document.getDocumentCode())
                .summary(document.getSummary())
                .status(document.getStatus().name())
                .creatdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .message(message)
                .build();
    }
}
