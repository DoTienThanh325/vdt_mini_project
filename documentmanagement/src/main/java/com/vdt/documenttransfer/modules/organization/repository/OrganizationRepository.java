package com.vdt.documenttransfer.modules.organization.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vdt.documenttransfer.modules.organization.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Integer> {
    Optional<Organization> findByOrgCode(String orgCode);

    boolean existsByOrgCode(String orgCode);

    boolean existsByEmail(String orgEmail);

    boolean existsByPhone(String orgPhone);
}
