package com.vdt.documenttransfer.modules.documentfile.service.impl;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.vdt.documenttransfer.modules.document.entity.Document;
import com.vdt.documenttransfer.modules.document.repository.DocumentRepository;
import com.vdt.documenttransfer.modules.documentfile.dto.DocumentFileResponse;
import com.vdt.documenttransfer.modules.documentfile.entity.DocumentFile;
import com.vdt.documenttransfer.modules.documentfile.repository.DocumentFileRepository;
import com.vdt.documenttransfer.modules.documentfile.service.DocumentFileService;
import com.vdt.documenttransfer.modules.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DocumentFileServiceImpl implements DocumentFileService {

    private final DocumentRepository documentRepository;
    private final DocumentFileRepository documentFileRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public List<DocumentFileResponse> uploadFiles(Integer documentId, MultipartFile[] files, User user) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document không tồn tại"));

        if (user.getId() != document.getCreatedBy().getId()) {
            throw new RuntimeException("Bạn không có quyền upload file của tài liệu, văn bản này");
        }

        if (files == null || files.length == 0) {
            throw new RuntimeException("Vui lòng chọn ít nhất một file");
        }

        List<DocumentFileResponse> responses = new ArrayList<>();

        try {
            String folderName = sanitizeFolderName(document.getDocumentCode());

            Path documentUploadPath = Paths.get(uploadDir, folderName)
                    .toAbsolutePath()
                    .normalize();

            if (!Files.exists(documentUploadPath)) {
                Files.createDirectories(documentUploadPath);
            }

            long currentFileCount = documentFileRepository.countByDocumentId(documentId);
            int fileIndex = (int) currentFileCount + 1;

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }

                validateFile(file);

                String originalFileName = file.getOriginalFilename();

                String extension = getFileExtension(originalFileName);

                String storedFileName = folderName
                        + "-"
                        + String.format("%04d", fileIndex)
                        + extension;

                fileIndex++;

                Path targetPath = documentUploadPath.resolve(storedFileName);

                Files.copy(
                        file.getInputStream(),
                        targetPath,
                        StandardCopyOption.REPLACE_EXISTING);

                String filePath = Paths.get(uploadDir, folderName, storedFileName)
                        .toString()
                        .replace("\\", "/");

                DocumentFile documentFile = DocumentFile.builder()
                        .document(document)
                        .originalFileName(originalFileName)
                        .storedFileName(storedFileName)
                        .filePath(filePath)
                        .fileType(file.getContentType())
                        .fileSize(file.getSize())
                        .uploadedAt(LocalDateTime.now())
                        .build();

                DocumentFile savedFile = documentFileRepository.save(documentFile);

                responses.add(DocumentFileResponse.builder()
                        .id(savedFile.getId())
                        .originalFileName(savedFile.getOriginalFileName())
                        .storedFileName(savedFile.getStoredFileName())
                        .filePath(savedFile.getFilePath())
                        .fileType(savedFile.getFileType())
                        .fileSize(savedFile.getFileSize())
                        .documentCode(document.getDocumentCode())
                        .documentSummary(document.getSummary())
                        .build());
            }

            return responses;

        } catch (IOException e) {
            throw new RuntimeException("Upload file thất bại: " + e.getMessage());
        }
    }

    private String sanitizeFolderName(String folderName) {
        if (folderName == null || folderName.isBlank()) {
            return "unknown-document";
        }

        return folderName
                .trim()
                .replaceAll("[^a-zA-Z0-9-_]", "-");
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }

        return fileName.substring(fileName.lastIndexOf("."));
    }

    private void validateFile(MultipartFile file) {
        long maxSize = 10 * 1024 * 1024;

        if (file.getSize() > maxSize) {
            throw new RuntimeException("File vượt quá dung lượng cho phép: " + file.getOriginalFilename());
        }

        String contentType = file.getContentType();

        if (contentType == null) {
            throw new RuntimeException("Không xác định được loại file");
        }

        boolean isAllowed = contentType.equals("application/pdf")
                || contentType.equals("application/msword")
                || contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                || contentType.equals("image/png")
                || contentType.equals("image/jpeg");

        if (!isAllowed) {
            throw new RuntimeException("File không đúng định dạng cho phép: " + file.getOriginalFilename());
        }
    }
}