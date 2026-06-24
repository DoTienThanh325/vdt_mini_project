package com.vdt.documenttransfer.modules.user.service;

import com.vdt.documenttransfer.common.response.PageResponse;
import com.vdt.documenttransfer.modules.user.dto.UserAssignmentUpdateRequest;
import com.vdt.documenttransfer.modules.user.dto.UserResponse;

public interface UserService {
    UserResponse updateUserAssignment(Integer userId, UserAssignmentUpdateRequest request);

    UserResponse findByUsername(String username);

    UserResponse updateUserStatus(Integer userId);

    PageResponse<UserResponse> findAll(int page, int size);

    PageResponse<UserResponse> findByStatus(int page, int size, String status);
}
