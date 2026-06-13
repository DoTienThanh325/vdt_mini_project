package com.vdt.documenttransfer.modules.interconnectedsystem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateInterconnectedSystemRequest {
    private String endpointUrl;
    private String apiKey;
    private String status;
}
