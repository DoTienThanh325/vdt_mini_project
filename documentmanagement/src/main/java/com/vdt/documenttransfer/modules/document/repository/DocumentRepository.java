package com.vdt.documenttransfer.modules.document.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.vdt.documenttransfer.modules.document.entity.Document;

public interface DocumentRepository extends JpaRepository<Document, Integer> {
    @Query(value = """
            SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(document_code, '-', -1) AS UNSIGNED)), 0)
            FROM documents
            WHERE document_code LIKE CONCAT('DOC-', :code, '-', :year, '-%')
            """, nativeQuery = true)
    Integer findMaxDocumentCodeSequenceByYear(@Param("year") int year, @Param("code") String code);

    Optional<Document> findByDocumentCode(String code);
}
