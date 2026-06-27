package com.vdt.documenttransfer.modules.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class LoginResponse {

    private String accessToken;

    private String tokenType;

    private String username;

    private String fullName;

    private String role;
}
