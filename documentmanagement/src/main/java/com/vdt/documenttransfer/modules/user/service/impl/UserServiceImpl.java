package com.vdt.documenttransfer.modules.user.service.impl;

import java.time.LocalDateTime;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.vdt.documenttransfer.modules.organization.entity.Organization;
import com.vdt.documenttransfer.modules.organization.repository.OrganizationRepository;
import com.vdt.documenttransfer.modules.role.entity.Role;
import com.vdt.documenttransfer.modules.role.repository.RoleRepository;
import com.vdt.documenttransfer.modules.user.dto.UserAssignmentUpdateRequest;
import com.vdt.documenttransfer.modules.user.dto.UserResponse;
import com.vdt.documenttransfer.modules.user.entity.User;
import com.vdt.documenttransfer.modules.user.repository.UserRepository;
import com.vdt.documenttransfer.modules.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final OrganizationRepository organizationRepository;

        @Override
        @CachePut(value = "users", key = "#result.username")
        public UserResponse updateUserAssignment(Integer userId, UserAssignmentUpdateRequest request) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

                Integer roleId = request.getRoleId();
                Integer organizationId = request.getOrganizationId();
                if (roleId != null) {
                        Role role = roleRepository.findById(request.getRoleId())
                                        .orElseThrow(() -> new RuntimeException("Role không tồn tại"));
                        user.setRole(role);
                }

                if (organizationId != null) {
                        Organization organization = organizationRepository.findById(request.getOrganizationId())
                                        .orElseThrow(() -> new RuntimeException("Organization không tồn tại"));
                        user.setOrganization(organization);
                } else {
                        user.setOrganization(null);
                }

                user.setUpdatedAt(LocalDateTime.now());

                User savedUser = userRepository.save(user);

                return entityToResponse(savedUser, "Cập nhật thông tin user thành công");
        }

        @Override
        @Cacheable(value = "users", key = "#username")
        public UserResponse findByUsername(String username) {
                System.out.println("QUERY DB findByUsername: " + username);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

                return entityToResponse(user, "Tìm kiếm user thành công");
        }

        @Override
        public UserResponse updateUserStatus(Integer userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

                String status = user.getStatus().name();
                String message = "";
                if (status.equals("LOCKED")) {
                        user.setStatus(User.Status.ACTIVE);
                        message = "Mở khóa tài khoản thành công";
                } else {
                        user.setStatus(User.Status.LOCKED);
                        message = "Khóa tài khoản thành công";
                }

                User savedUser = userRepository.save(user);

                return entityToResponse(savedUser, message);
        }

        private UserResponse entityToResponse(User user, String message) {
                return UserResponse.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .status(user.getStatus().name())
                                .roleId(user.getRole().getId())
                                .roleCode(user.getRole().getRoleCode())
                                .roleName(user.getRole().getRoleName())

                                .organizationId(user.getOrganization() != null
                                                ? user.getOrganization().getId()
                                                : null)
                                .organizationCode(user.getOrganization() != null
                                                ? user.getOrganization().getOrgCode()
                                                : null)
                                .organizationName(user.getOrganization() != null
                                                ? user.getOrganization().getOrgName()
                                                : null)

                                .message(message)
                                .build();
        }
}
