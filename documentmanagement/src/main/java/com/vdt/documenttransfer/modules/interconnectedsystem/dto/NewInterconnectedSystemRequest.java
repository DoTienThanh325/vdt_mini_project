package com.vdt.documenttransfer.modules.interconnectedsystem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewInterconnectedSystemRequest {
    private String systemCode;
    private String systemName;
    private String endpointUrl;
    private String apiKey;
}
