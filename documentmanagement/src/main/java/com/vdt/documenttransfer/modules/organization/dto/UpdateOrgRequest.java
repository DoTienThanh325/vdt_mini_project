package com.vdt.documenttransfer.modules.organization.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateOrgRequest {
    private String orgCode;
    private String address;
    private String email;
    private String phone;
    private Integer systemId;
}
