package com.vdt.documenttransfer.modules.documentfile.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vdt.documenttransfer.modules.documentfile.entity.DocumentFile;

public interface DocumentFileRepository extends JpaRepository<DocumentFile, Integer> {
    Long countByDocumentId(Integer documentId);

    List<DocumentFile> findByDocumentId(Integer documentId);

    void deleteByDocument_Id(Integer documentId);
}
