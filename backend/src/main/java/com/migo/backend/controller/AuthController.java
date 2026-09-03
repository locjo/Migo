package com.migo.backend.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.migo.backend.dto.request.ApiResponse;
import com.migo.backend.dto.request.LoginRequest;
import com.migo.backend.dto.request.RegisterRequest;
import com.migo.backend.dto.response.AuthResponse;
import com.migo.backend.entity.User;
import com.migo.backend.security.JwtTokenProvider;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

        private final JwtTokenProvider jwtTokenProvider;

        private final com.migo.backend.service.AuthService authService; // Inject Service của bạn để gọi

        // 1. ROUTE ĐĂNG KÝ (Giữ nguyên như cũ)
        @PostMapping("/signup")
        public ResponseEntity<ApiResponse<User>> registerUser(@Valid @RequestBody RegisterRequest request) {

                User createdUser = authService.register(request);

                ApiResponse<User> response = ApiResponse.<User>builder()
                                .message("Đăng ký tài khoản thành công")
                                .result(createdUser)
                                .build();

                return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        // 2. ROUTE ĐĂNG NHẬP (Cập nhật trả về Access Token qua Body và Refresh Token
        // qua Cookie)
        @PostMapping("/signin")
        public ResponseEntity<ApiResponse<AuthResponse>> loginUser(@Valid @RequestBody LoginRequest request) {

                // 1. Gọi Service thực hiện đăng nhập (Trả về Map chứa cả AuthResponse và chuỗi
                // RefreshToken)
                Map<String, Object> result = authService.login(request);

                // 2. Rút 2 đối tượng ra từ Map
                AuthResponse authResponse = (AuthResponse) result.get("authResponse");
                String refreshTokenStr = (String) result.get("refreshToken");

                // 3. Tạo HttpOnly Cookie trực tiếp từ chuỗi refreshTokenStr nhận được
                ResponseCookie cookie = jwtTokenProvider.createRefreshTokenCookie(refreshTokenStr);

                // 4. Đóng gói kết quả gửi về cho Frontend
                ApiResponse<AuthResponse> apiResponse = new ApiResponse<>();
                apiResponse.setCode(1000);
                apiResponse.setMessage("Đăng nhập thành công!");
                apiResponse.setResult(authResponse);

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cookie.toString()) // Đính kèm cookie an toàn
                                .body(apiResponse);
        }

        // 3. ROUTE ĐĂNG XUẤT (Xóa token dưới DB và xóa cookie ở trình duyệt)
        @PostMapping("/signout")
        public ResponseEntity<ApiResponse<String>> logoutUser(
                        @CookieValue(name = "migo_refresh_token", required = false) String refreshTokenString) {

                // 1. Đẩy logic xóa DB sang cho Service
                authService.logout(refreshTokenString);

                // 2. Tạo cookie rỗng làm sạch Browser (dùng lại helper method chuẩn của bạn)
                ResponseCookie cleanCookie = jwtTokenProvider.cleanRefreshTokenCookie();

                // 3. Chuẩn hóa ApiResponse đồng bộ với cả dự án
                ApiResponse<String> apiResponse = new ApiResponse<>();
                apiResponse.setCode(1000);
                apiResponse.setMessage("Đăng xuất thành công!");
                apiResponse.setResult("Logged out successfully");

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                                .body(apiResponse);
        }

        @PostMapping("/refresh")
        public ResponseEntity<AuthResponse> refreshToken(
                        // Spring Boot sẽ tự lấy giá trị của cookie "migo_refresh_token" và gán vào biến
                        // refreshTokenStr
                        @CookieValue(name = "migo_refresh_token", required = false) String refreshTokenStr) {

                AuthResponse response = authService.refreshToken(refreshTokenStr);
                return ResponseEntity.ok(response);
        }
}