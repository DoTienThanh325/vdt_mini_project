package com.vdt.documenttransfer.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ErrorResponse {

    private int status;

    private String message;

    private String path;

    private LocalDateTime timestamp;
}