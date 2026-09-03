package com.migo.backend.service.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.migo.backend.dto.request.LoginRequest;
import com.migo.backend.dto.request.RegisterRequest;
import com.migo.backend.dto.response.AuthResponse;
import com.migo.backend.entity.RefreshToken;
import com.migo.backend.entity.User;
import com.migo.backend.exception.AppException;
import com.migo.backend.exception.ErrorCode;
import com.migo.backend.repository.RefreshTokenRepository;
import com.migo.backend.repository.UserRepository;
import com.migo.backend.security.JwtTokenProvider;
import com.migo.backend.service.AuthService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername().trim().toLowerCase())) {
            throw new AppException(ErrorCode.USERNAME_EXISTED); 
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }

        User newUser = User.builder()
                .username(request.getUsername().trim().toLowerCase())
                .email(request.getEmail().trim().toLowerCase())
                .displayName(request.getDisplayName())
                .hashedPassword(passwordEncoder.encode(request.getPassword()))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .role("USER")
                .build();

        return userRepository.save(newUser);
    } 

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> login(LoginRequest request) {
        String username = request.getUsername().trim().toLowerCase();
        String password = request.getPassword();

        // 1. Tìm kiếm User
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_CREDENTIAL));

        // 2. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(password, user.getHashedPassword())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIAL);
        }

        // 3. Sinh Access Token
        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getId());

        // 4. Sinh & Lưu Refresh Token
        String refreshTokenStr = UUID.randomUUID().toString();
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(refreshTokenStr); 
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(7 * 24 * 60 * 60));

        refreshTokenRepository.deleteByUser(user);
        refreshTokenRepository.save(refreshToken);

        // 5. Chuẩn hóa AuthResponse bằng Builder (có trường id)
        AuthResponse authResponse = AuthResponse.builder()
                .id(user.getId())
                .accessToken(accessToken)
                .username(user.getUsername())
                .displayName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername())
                .roles(Collections.singletonList(user.getRole() != null ? user.getRole() : "USER"))
                .tokenType("Bearer")
                .build();

        // 6. Trả dữ liệu cho Controller
        Map<String, Object> response = new HashMap<>();
        response.put("authResponse", authResponse);
        response.put("refreshToken", refreshTokenStr);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void logout(String refreshTokenStr) {
        if (refreshTokenStr != null && !refreshTokenStr.isBlank()) {
            refreshTokenRepository.findByToken(refreshTokenStr)
                    .ifPresent(refreshTokenRepository::delete);
        }
    }

    @Override
    public AuthResponse refreshToken(String refreshTokenStr) {
        if (refreshTokenStr == null || refreshTokenStr.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED));

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtTokenProvider.generateToken(user.getUsername(), user.getId());

        return AuthResponse.builder()
                .id(user.getId())
                .accessToken(newAccessToken)
                .username(user.getUsername())
                .displayName(user.getDisplayName() != null ? user.getDisplayName() : user.getUsername())
                .roles(Collections.singletonList(user.getRole() != null ? user.getRole() : "USER"))
                .tokenType("Bearer")
                .build();
    }
}   