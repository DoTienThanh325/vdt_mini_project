package com.vdt.documenttransfer.modules.role.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.vdt.documenttransfer.modules.role.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleCode(String roleCode);

    boolean existsByRoleCode(String roleCode);
}
