package com.vdt.documenttransfer.modules.interconnectedsystem.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.vdt.documenttransfer.common.logging.AppLogger;
import com.vdt.documenttransfer.modules.interconnectedsystem.dto.InterconnectedSystemResponse;
import com.vdt.documenttransfer.modules.interconnectedsystem.dto.NewInterconnectedSystemRequest;
import com.vdt.documenttransfer.modules.interconnectedsystem.dto.UpdateInterconnectedSystemRequest;
import com.vdt.documenttransfer.modules.interconnectedsystem.entity.InterconnectedSystem;
import com.vdt.documenttransfer.modules.interconnectedsystem.repository.InterconnectedSystemRepository;
import com.vdt.documenttransfer.modules.interconnectedsystem.service.InterconnectedSystemService;
import com.vdt.documenttransfer.modules.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InterconnectedSystemServiceImpl implements InterconnectedSystemService {
    private final InterconnectedSystemRepository interconnectedSystemRepository;
    private final AppLogger appLogger;

    private InterconnectedSystemResponse entityToResponse(InterconnectedSystem is) {
        return InterconnectedSystemResponse
                .builder()
                .id(is.getId())
                .systemCode(is.getSystemCode())
                .systemName(is.getSystemName())
                .endpointUrl(is.getEndpointUrl())
                .apiKey(is.getApiKey())
                .status(is.getStatus().name())
                .createdAt(is.getCreatedAt())
                .updatedAt(is.getUpdatedAt())
                .build();
    }

    @Override
    @Cacheable(value = "interconnected-systems:list", key = "'all'")
    public List<InterconnectedSystemResponse> findAll() {
        List<InterconnectedSystem> interconnectedSystems = interconnectedSystemRepository.findAll();
        return interconnectedSystems.stream().map(is -> entityToResponse(is)).toList();
    }

    @Override
    public InterconnectedSystemResponse createNew(NewInterconnectedSystemRequest request, User user) {
        try {
            if (interconnectedSystemRepository.existsBySystemCode(request.getSystemCode())) {
                throw new RuntimeException("Mã hệ thống đã tồn tại");
            }

            if (interconnectedSystemRepository.existsByEndpointUrl(request.getEndpointUrl())) {
                throw new RuntimeException("Đường dẫn hệ thống đã được sử dụng");
            }

            LocalDateTime now = LocalDateTime.now();

            InterconnectedSystem interconnectedSystem = InterconnectedSystem.builder()
                    .systemCode(request.getSystemCode())
                    .systemName(request.getSystemName())
                    .endpointUrl(request.getEndpointUrl())
                    .apiKey(request.getApiKey())
                    .status(InterconnectedSystem.Status.ACTIVE)
                    .createdAt(now)
                    .updatedAt(null)
                    .organizations(null)
                    .build();

            InterconnectedSystem newInterconnectedSystem = interconnectedSystemRepository.save(interconnectedSystem);

            appLogger.infoAction("REGISTER_INTERCONNECTED_SYSTEM", user != null ? user.getId() : null,
                    "INTERCONNECTED_SYSTEM", newInterconnectedSystem.getSystemCode(),
                    "Register interconnected system successfully");

            return InterconnectedSystemResponse.builder()
                    .id(newInterconnectedSystem.getId())
                    .systemCode(newInterconnectedSystem.getSystemCode())
                    .systemName(newInterconnectedSystem.getSystemName())
                    .endpointUrl(newInterconnectedSystem.getEndpointUrl())
                    .apiKey(newInterconnectedSystem.getApiKey())
                    .status(newInterconnectedSystem.getStatus().name())
                    .createdAt(newInterconnectedSystem.getCreatedAt())
                    .updatedAt(newInterconnectedSystem.getUpdatedAt())
                    .message("Tạo hệ thống liên thông thành công")
                    .build();
        } catch (RuntimeException e) {
            appLogger.errorAction("REGISTER_INTERCONNECTED_SYSTEM", user != null ? user.getId() : null,
                    "INTERCONNECTED_SYSTEM", request != null ? request.getSystemCode() : null,
                    "Register interconnected system failed: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public InterconnectedSystemResponse update(Integer id, UpdateInterconnectedSystemRequest request, User user) {
        try {
            InterconnectedSystem interconnectedSystem = interconnectedSystemRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hệ thống này"));

            if (request.getApiKey() != null) {
                interconnectedSystem.setApiKey(request.getApiKey());
            }

            if (request.getEndpointUrl() != null) {
                interconnectedSystem.setEndpointUrl(request.getEndpointUrl());
            }

            String status = request.getStatus();
            if (status != null) {
                if (status.equals("ACTIVE")) {
                    interconnectedSystem.setStatus(InterconnectedSystem.Status.ACTIVE);
                } else if (status.equals("INACTIVE")) {
                    interconnectedSystem.setStatus(InterconnectedSystem.Status.INACTIVE);
                } else {
                    interconnectedSystem.setStatus(InterconnectedSystem.Status.DELETED);
                }
            }
            LocalDateTime now = LocalDateTime.now();
            interconnectedSystem.setUpdatedAt(now);

            InterconnectedSystem updateInterconnectedSystem = interconnectedSystemRepository.save(interconnectedSystem);

            appLogger.infoAction("APPROVE_INTERCONNECTED_SYSTEM", user != null ? user.getId() : null,
                    "INTERCONNECTED_SYSTEM", updateInterconnectedSystem.getSystemCode(),
                    "Update interconnected system successfully");

            return InterconnectedSystemResponse.builder()
                    .id(updateInterconnectedSystem.getId())
                    .systemCode(updateInterconnectedSystem.getSystemCode())
                    .systemName(updateInterconnectedSystem.getSystemName())
                    .endpointUrl(updateInterconnectedSystem.getEndpointUrl())
                    .apiKey(updateInterconnectedSystem.getApiKey())
                    .status(updateInterconnectedSystem.getStatus().name())
                    .createdAt(updateInterconnectedSystem.getCreatedAt())
                    .updatedAt(updateInterconnectedSystem.getUpdatedAt())
                    .message("Cập nhật hệ thống thành công")
                    .build();
        } catch (RuntimeException e) {
            appLogger.errorAction("APPROVE_INTERCONNECTED_SYSTEM", user != null ? user.getId() : null,
                    "INTERCONNECTED_SYSTEM", String.valueOf(id),
                    "Update interconnected system failed: " + e.getMessage(), e);
            throw e;
        }
    }
}
