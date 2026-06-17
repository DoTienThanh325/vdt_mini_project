package com.vdt.documenttransfer.modules.signature.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vdt.documenttransfer.modules.signature.entity.DocumentSignature;

public interface DocumentSignatureRepository extends JpaRepository<DocumentSignature, Integer> {
    Optional<DocumentSignature> findByDocumentId(Integer documentId);
}
