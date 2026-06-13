package com.vdt.documenttransfer.modules.user.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserResponse implements Serializable {
    private Integer id;

    private String username;

    private String status;

    private Integer roleId;

    private String roleCode;

    private String roleName;

    private Integer organizationId;

    private String organizationCode;

    private String organizationName;

    private String message;
}
