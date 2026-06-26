package com.vdt.documenttransfer.modules.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDetailResponse {
    private String username;
    private String fullName;
    private String phone;
    private String email;
    private String status;
    private String roleCode;
    private String roleName;
    private String orgCode;
    private String orgName;
}
