package com.vdt.documenttransfer.modules.transfer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vdt.documenttransfer.modules.transfer.entity.DocumentTransfer;

public interface DocumentTransferRepository extends JpaRepository<DocumentTransfer, Integer> {
    Optional<DocumentTransfer> findByDocument_IdAndReceiverOrganization_Id(Integer documentId, Integer orgId);
}
