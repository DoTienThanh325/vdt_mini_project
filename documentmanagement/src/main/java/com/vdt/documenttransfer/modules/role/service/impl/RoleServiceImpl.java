package com.vdt.documenttransfer.modules.role.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.vdt.documenttransfer.modules.role.dto.RoleResponse;
import com.vdt.documenttransfer.modules.role.entity.Role;
import com.vdt.documenttransfer.modules.role.repository.RoleRepository;
import com.vdt.documenttransfer.modules.role.service.RoleService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;

    @Override
    public List<RoleResponse> getAll() {
        List<Role> roles = roleRepository.findAll();
        List<RoleResponse> roleResponses = roles.stream()
                .map(role -> RoleResponse.builder().id(role.getId()).roleCode(role.getRoleCode()).build()).toList();
        return roleResponses;
    }

}
