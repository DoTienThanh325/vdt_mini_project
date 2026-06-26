package com.vdt.documenttransfer.modules.user.utils;

import org.springframework.stereotype.Component;

import com.vdt.documenttransfer.modules.user.dto.UserDetailResponse;
import com.vdt.documenttransfer.modules.user.dto.UserResponse;
import com.vdt.documenttransfer.modules.user.entity.User;

@Component
public class EntityToResponse {
    public UserResponse entityToUserResponse(User user, String message) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .status(user.getStatus().name())
                .roleName(user.getRole().getRoleName())
                .organizationName(user.getOrganization() != null
                        ? user.getOrganization().getOrgName()
                        : null)
                .message(message)
                .build();
    }

    public UserDetailResponse entityToUserDetailResponse(User user, String message) {
        return UserDetailResponse.builder()
            .username(user.getUsername())
            .fullName(user.getFullName())
            .phone(user.getPhone())
            .email(user.getEmail())
            .status(user.getStatus().name())
            .roleCode(user.getRole().getRoleCode())
            .roleName(user.getRole().getRoleName())
            .orgCode(user.getOrganization() != null
                        ? user.getOrganization().getOrgCode(): null)
            .orgName(user.getOrganization() != null
                        ? user.getOrganization().getOrgName(): null)
            .build();
    }
}
