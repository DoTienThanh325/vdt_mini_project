package com.vdt.documenttransfer.modules.user.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import com.vdt.documenttransfer.common.response.PageResponse;
import com.vdt.documenttransfer.modules.notification.service.NotificationService;
import com.vdt.documenttransfer.modules.organization.entity.Organization;
import com.vdt.documenttransfer.modules.organization.repository.OrganizationRepository;
import com.vdt.documenttransfer.modules.role.entity.Role;
import com.vdt.documenttransfer.modules.role.repository.RoleRepository;
import com.vdt.documenttransfer.modules.user.dto.UserAssignmentUpdateRequest;
import com.vdt.documenttransfer.modules.user.dto.UserDetailResponse;
import com.vdt.documenttransfer.modules.user.dto.UserResponse;
import com.vdt.documenttransfer.modules.user.entity.User;
import com.vdt.documenttransfer.modules.user.repository.UserRepository;
import com.vdt.documenttransfer.modules.user.service.UserService;
import com.vdt.documenttransfer.modules.user.utils.EntityToResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final OrganizationRepository organizationRepository;
        private final NotificationService notificationService;
        private final EntityToResponse entityToResponse;

        @Override
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

                notificationService.createNotification(userId, "Thông tin bị thay đổi",
                                "Thông tin của bạn đã được Admin chỉnh sửa vui lòng xem lại chi tiết thông tin tài khoản");

                return entityToResponse.entityToUserResponse(savedUser, "Cập nhật thông tin user thành công");
        }

        public UserDetailResponse findById(Integer userId) {
                User user = userRepository.findById(
                                userId)
                                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

                return entityToResponse.entityToUserDetailResponse(user, "Tìm kiếm user thành công");
        }

        @Override
        public UserResponse updateUserStatus(Integer userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
                String notificationMessage = "";

                String status = user.getStatus().name();
                String message = "";
                if (status.equals("LOCKED")) {
                        user.setStatus(User.Status.ACTIVE);
                        message = "Mở khóa tài khoản thành công";
                        notificationMessage = "Tài khoản của bạn đã được kích hoạt trở lại. Từ bây giờ bạn có thể tham gia vào việc trao đổi văn bản, tài liệu";
                } else {
                        user.setStatus(User.Status.LOCKED);
                        message = "Khóa tài khoản thành công";
                        notificationMessage = "Tài khoản của bạn đã bị khóa. Từ bây giờ bạn không thể tham gia vào việc trao đổi văn bản, tài liệu";
                }

                User savedUser = userRepository.save(user);
                notificationService.createNotification(userId, "Trạng thái tài khoản thay đổi", notificationMessage);

                return entityToResponse.entityToUserResponse(savedUser, message);
        }

        @Override
        public PageResponse<UserResponse> findAll(int page, int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

                Page<User> userPage = userRepository.findAll(pageable);

                List<UserResponse> content = userPage.getContent().stream()
                                .map(user -> entityToResponse.entityToUserResponse(user, null))
                                .toList();

                return PageResponse.<UserResponse>builder()
                                .content(content)
                                .page(userPage.getNumber())
                                .size(userPage.getSize())
                                .totalElements(userPage.getTotalElements())
                                .totalPages(userPage.getTotalPages())
                                .first(userPage.isFirst())
                                .last(userPage.isLast())
                                .build();
        }

        @Override
        public PageResponse<UserResponse> findByStatus(int page, int size, String status) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
                User.Status userStatus = User.Status.valueOf(status);
                Page<User> userPage = userRepository.findByStatus(userStatus, pageable);

                List<UserResponse> content = userPage.getContent().stream()
                                .map(user -> entityToResponse.entityToUserResponse(user, null))
                                .toList();

                return PageResponse.<UserResponse>builder()
                                .content(content)
                                .page(userPage.getNumber())
                                .size(userPage.getSize())
                                .totalElements(userPage.getTotalElements())
                                .totalPages(userPage.getTotalPages())
                                .first(userPage.isFirst())
                                .last(userPage.isLast())
                                .build();
        }
}
