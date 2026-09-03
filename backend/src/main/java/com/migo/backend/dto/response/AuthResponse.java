package com.migo.backend.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AuthResponse {
    private String id;
    private String accessToken;       // Token JWT dùng để đính kèm vào Header các request sau
    private String tokenType = "Bearer"; // Loại token mặc định
    private String username;          // Username để frontend hiển thị lời chào
    private String displayName;       // Tên hiển thị của người dùng (nếu có)
    private List<String> roles;       // Danh sách vai trò (ví dụ: ["ROLE_USER"]) để frontend ẩn/hiện nút Admin

}