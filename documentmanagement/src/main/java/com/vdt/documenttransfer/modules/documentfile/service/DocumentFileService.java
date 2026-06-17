package com.vdt.documenttransfer.modules.documentfile.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.vdt.documenttransfer.modules.documentfile.dto.DocumentFileResponse;

public interface DocumentFileService {
    List<DocumentFileResponse> uploadFiles(Integer documentId, MultipartFile files[]);
}
