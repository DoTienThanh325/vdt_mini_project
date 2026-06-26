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

    private String fullName;

    private String status;

    private String roleName;

    private String organizationName;

    private String message;
}
