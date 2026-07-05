package com.vdt.documenttransfer.modules.transfer.service.components;

import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.vdt.documenttransfer.modules.interconnectedsystem.entity.InterconnectedSystem;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExternalDocumentClient {

    private final RestTemplate restTemplate;

    public void sendDocument(
            InterconnectedSystem system,
            String encryptedData,
            String authorizationHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-KEY", system.getApiKey());

        if (authorizationHeader != null && !authorizationHeader.isBlank()) {
            headers.set("Authorization", authorizationHeader);
        }

        Map<String, String> body = Map.of(
                "encryptedData", encryptedData);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                system.getEndpointUrl(),
                request,
                String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(
                    "Hệ thống nhận trả lỗi: " + response.getStatusCode());
        }
    }
}