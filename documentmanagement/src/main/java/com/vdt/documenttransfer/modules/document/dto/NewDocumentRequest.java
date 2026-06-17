package com.vdt.documenttransfer.modules.document.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewDocumentRequest {
    private String documentType;
    private String summary;
}
