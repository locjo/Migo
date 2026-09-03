package com.migo.backend.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id; // ID của User (MongoDB _id)
    private String username; // Tên đăng nhập
    private String displayName; // Tên hiển thị
    private String avatarUrl; // Ảnh đại diện
    private List<String> roles; // Danh sách quyền (nếu cần)
}
