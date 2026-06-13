package com.vdt.documenttransfer.modules.auth.service;

import com.vdt.documenttransfer.modules.auth.dto.LoginRequest;
import com.vdt.documenttransfer.modules.auth.dto.LoginResponse;
import com.vdt.documenttransfer.modules.auth.dto.RegisterRequest;
import com.vdt.documenttransfer.modules.auth.dto.RegisterResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
    RegisterResponse register(RegisterRequest request);
}