package com.vdt.documenttransfer.modules.role.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RoleResponse {
    private Integer id;
    private String roleCode;
}
