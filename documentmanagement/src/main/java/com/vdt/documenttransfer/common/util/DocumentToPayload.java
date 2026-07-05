package com.vdt.documenttransfer.common.util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import org.springframework.stereotype.Component;

import com.vdt.documenttransfer.modules.document.entity.Document;
import com.vdt.documenttransfer.modules.documentfile.entity.DocumentFile;
import com.vdt.documenttransfer.modules.transfer.dto.ExternalDocumentFilePayload;
import com.vdt.documenttransfer.modules.transfer.dto.ExternalDocumentPayload;

@Component
public class DocumentToPayload {
    public ExternalDocumentPayload buiPayload(Document document) {

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
                .documentCode(document.getDocumentCode())
                .documentType(document.getDocumentType())
                .summary(document.getSummary())
                .senderOrgCode(document.getSenderOrganization().getOrgCode())
                .files(documentFilePayloads)
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
}
