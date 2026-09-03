package com.migo.backend.service;



import java.util.Map;

import com.migo.backend.dto.request.LoginRequest;
import com.migo.backend.dto.request.RegisterRequest;
import com.migo.backend.dto.response.AuthResponse;
import com.migo.backend.entity.User;

public interface AuthService {
    
    // Hàm đăng ký tài khoản (Khớp với triển khai ở AuthServiceImpl của bạn)
    User register(RegisterRequest request);
    
    public Map<String, Object> login(LoginRequest request);

    // tao refreshToken
    AuthResponse refreshToken(String refreshTokenStr);

    // Hàm đăng xuất
    void logout(String refreshTokenStr);
}