package com.vdt.documenttransfer.modules.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegisterResponse {

    private Integer id;

    private String username;

    private String fullName;

    private String email;

    private String role;

    private String organization;

    private String status;

    private String message;
}