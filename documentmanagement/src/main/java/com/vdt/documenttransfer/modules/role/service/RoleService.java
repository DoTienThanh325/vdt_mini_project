package com.vdt.documenttransfer.modules.role.service;

import java.util.List;

import com.vdt.documenttransfer.modules.role.dto.RoleResponse;

public interface RoleService {
    List<RoleResponse> getAll();
}
