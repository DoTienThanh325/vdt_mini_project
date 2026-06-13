package com.vdt.documenttransfer.modules.interconnectedsystem.repository;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;

import com.vdt.documenttransfer.modules.interconnectedsystem.entity.InterconnectedSystem;

public interface InterconnectedSystemRepository extends JpaRepository<InterconnectedSystem, Integer> {
    List<InterconnectedSystem> findAll();

    Optional<InterconnectedSystem> findById(Integer id);

    boolean existsByEndpointUrl(String enpointUrl);

    boolean existsBySystemCode(String systemCode);
}
