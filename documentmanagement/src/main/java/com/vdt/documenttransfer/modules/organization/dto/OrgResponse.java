package com.vdt.documenttransfer.modules.organization.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrgResponse {
    private Integer id;
    private String orgCode;
    private String orgName;
    private String address;
    private String email;
    private String phone;
    private String status;
    private Integer systemId;
    private String systemCode;
    private String systemName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
}
