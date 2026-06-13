package com.vdt.documenttransfer.modules.auth.controller;

import com.vdt.documenttransfer.modules.auth.dto.LoginRequest;
import com.vdt.documenttransfer.modules.auth.dto.LoginResponse;
import com.vdt.documenttransfer.modules.auth.dto.RegisterRequest;
import com.vdt.documenttransfer.modules.auth.dto.RegisterResponse;
import com.vdt.documenttransfer.modules.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
}
