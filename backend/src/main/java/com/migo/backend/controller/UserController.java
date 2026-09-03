package com.migo.backend.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.migo.backend.dto.request.ApiResponse;
import com.migo.backend.dto.response.UserResponse;
import com.migo.backend.entity.User;
import com.migo.backend.repository.UserRepository;
import com.migo.backend.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository; // 👈 1. Inject UserRepository
    private final UserService userService;
    @GetMapping("/me") 
    public ResponseEntity<ApiResponse<Object>> getMyInfo( 
            @AuthenticationPrincipal  UserDetails userDetails) {
         
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        
        if (userDetails == null) {
            apiResponse.setCode(1001);
            apiResponse.setMessage("Access token hết hạn hoặc không đúng");
            return ResponseEntity.status(403).body(apiResponse);
        }

        // 👈 2. Tìm User từ Database để lấy đầy đủ thông tin
        User user = userRepository.findByUsernameIgnoreCase(userDetails.getUsername())
                .orElse(null);

        if (user == null) {
            apiResponse.setCode(1002);
            apiResponse.setMessage("Không tìm thấy người dùng");
            return ResponseEntity.status(404).body(apiResponse);
        }

        // 👈 3. Đưa id và các thông tin cần thiết vào response
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId()); 
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("displayName", user.getDisplayName());
        userInfo.put("avatarUrl", user.getAvatarUrl());
        userInfo.put("authorities", userDetails.getAuthorities());

        apiResponse.setCode(1000);
        apiResponse.setMessage("Lấy thông tin cá nhân thành công");
        apiResponse.setResult(userInfo);

        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/search")
    
    public ResponseEntity<ApiResponse<UserResponse>> searchUser(
            @RequestParam("keyword") String keyword,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        UserResponse result = userService.searchUserByUsername(currentUsername, keyword);

        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .code(1000)
                .result(result)
                .build());
    }
}