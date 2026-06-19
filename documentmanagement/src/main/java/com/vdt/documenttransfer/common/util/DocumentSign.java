package com.vdt.documenttransfer.common.util;

import java.io.InputStream;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;


import com.vdt.documenttransfer.modules.document.entity.Document;
import com.vdt.documenttransfer.modules.documentfile.entity.DocumentFile;
import com.vdt.documenttransfer.modules.user.entity.User;

public class DocumentSign {
    public static String generateDocumentHash(Document document, List<DocumentFile> files, User leader) {
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
}
