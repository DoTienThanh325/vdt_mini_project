package com.vdt.documenttransfer.modules.auth.service.impl;

import com.vdt.documenttransfer.common.security.CustomUserDetails;
import com.vdt.documenttransfer.common.security.JwtService;
import com.vdt.documenttransfer.modules.auth.dto.LoginRequest;
import com.vdt.documenttransfer.modules.auth.dto.LoginResponse;
import com.vdt.documenttransfer.modules.auth.dto.RegisterRequest;
import com.vdt.documenttransfer.modules.auth.dto.RegisterResponse;
import com.vdt.documenttransfer.modules.auth.service.AuthService;
import com.vdt.documenttransfer.modules.organization.entity.Organization;
import com.vdt.documenttransfer.modules.organization.repository.OrganizationRepository;
import com.vdt.documenttransfer.modules.role.repository.RoleRepository;
import com.vdt.documenttransfer.modules.user.entity.User;
import com.vdt.documenttransfer.modules.role.entity.Role;
import com.vdt.documenttransfer.modules.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

        private final AuthenticationManager authenticationManager;
        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final OrganizationRepository organizationRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;

        @Override
        public LoginResponse login(LoginRequest request) {
                try {
                        authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken(
                                                        request.getUsername(),
                                                        request.getPassword()));
                } catch (BadCredentialsException e) {
                        throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không đúng");
                } catch (DisabledException e) {
                        throw new RuntimeException("Tài khoản đã bị vô hiệu hóa");
                } catch (LockedException e) {
                        throw new RuntimeException("Tài khoản đã bị khóa");
                }

                User user = userRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

                if (!user.getStatus().name().equals("ACTIVE")) {
                        throw new RuntimeException("Tài khoản chưa được kích hoạt");
                }

                CustomUserDetails userDetails = new CustomUserDetails(user);

                String token = jwtService.generateToken(
                                Map.of(
                                                "userId", user.getId(),
                                                "fullName", user.getFullName(),
                                                "role", user.getRole().getRoleCode()),
                                userDetails);

                return LoginResponse.builder()
                                .accessToken(token)
                                .tokenType("Bearer")
                                .username(user.getUsername())
                                .fullName(user.getFullName())
                                .role(user.getRole().getRoleCode())
                                .orgCode(user.getOrganization() != null ? user.getOrganization().getOrgCode() : null)
                                .build();
        }

        @Override
        @Transactional
        public RegisterResponse register(RegisterRequest request) {

                if (userRepository.existsByUsername(request.getUsername())) {
                        throw new RuntimeException("Username đã tồn tại");
                }

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("Email đã tồn tại");
                }

                Role role = roleRepository.findById(request.getRoleId())
                                .orElseThrow(() -> new RuntimeException("Role không tồn tại"));

                Organization organization = null;
                if (request.getOrganizationId() != null) {
                        organization = organizationRepository.findById(request.getOrganizationId())
                                        .orElseThrow(() -> new RuntimeException("Organization không tồn tại"));

                        if (!organization.getStatus().name().equals("ACTIVE")) {
                                throw new RuntimeException(
                                                "Đơn vị này đang tạm dừng hoạt động vui lòng chọn đơn vị khác");
                        }
                }

                LocalDateTime now = LocalDateTime.now();

                User user = User.builder()
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .fullName(request.getFullName())
                                .phone(request.getPhone())
                                .email(request.getEmail())
                                .status(User.Status.ACTIVE)
                                .role(role)
                                .organization(organization)
                                .createdAt(now)
                                .updatedAt(null)
                                .build();

                User savedUser = userRepository.save(user);

                return RegisterResponse.builder()
                                .id(savedUser.getId())
                                .username(savedUser.getUsername())
                                .fullName(savedUser.getFullName())
                                .email(savedUser.getEmail())
                                .role(savedUser.getRole().getRoleCode())
                                .organization(savedUser.getOrganization() != null
                                                ? savedUser.getOrganization().getOrgName()
                                                : null)
                                .status(savedUser.getStatus().name())
                                .message("Đăng ký tài khoản thành công")
                                .build();
        }
}
