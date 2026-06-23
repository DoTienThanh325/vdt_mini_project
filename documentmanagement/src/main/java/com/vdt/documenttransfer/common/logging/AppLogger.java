package com.vdt.documenttransfer.common.logging;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AppLogger {

    public void info(String action, String message) {
        try {
            MDC.put("action", action);
            log.info(message);
        } finally {
            MDC.clear();
        }
    }

    public void infoAction(String action, Integer userId, String objectType, String objectValue, String message) {
        try {
            putIfNotNull("action", action);
            putIfNotNull("user_id", userId);
            putIfNotNull("object_type", objectType);
            putIfNotNull("object_value", objectValue);
            MDC.put("status", "SUCCESS");
            log.info(message);
        } finally {
            MDC.clear();
        }
    }

    public void errorAction(String action, Integer userId, String objectType, String objectValue, String message,
            Exception e) {
        try {
            putIfNotNull("action", action);
            putIfNotNull("user_id", userId);
            putIfNotNull("object_type", objectType);
            putIfNotNull("object_value", objectValue);
            MDC.put("status", "FAILED");
            log.error(message, e);
        } finally {
            MDC.clear();
        }
    }

    public void infoDocument(String action, Integer userId, String documentCode, String message) {
        try {
            MDC.put("action", action);
            MDC.put("user_id", String.valueOf(userId));
            MDC.put("document_code", documentCode);
            log.info(message);
        } finally {
            MDC.clear();
        }
    }

    public void errorDocument(String action, Integer userId, String documentCode, String message, Exception e) {
        try {
            MDC.put("action", action);
            MDC.put("user_id", String.valueOf(userId));
            MDC.put("document_code", documentCode);
            log.error(message, e);
        } finally {
            MDC.clear();
        }
    }

    private void putIfNotNull(String key, Object value) {
        if (value != null) {
            MDC.put(key, Objects.toString(value));
        }
    }
}
