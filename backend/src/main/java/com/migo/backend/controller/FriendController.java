package com.migo.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.migo.backend.dto.request.ApiResponse;
import com.migo.backend.dto.response.UserResponse;
import com.migo.backend.service.FriendService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    /**
     * Lấy danh sách bạn bè của người dùng hiện tại
     * GET /api/v1/friends
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getFriendsList(Authentication authentication) {
        String currentUsername = authentication.getName();
        List<UserResponse> friends = friendService.getFriendsList(currentUsername);

        return ResponseEntity.ok(ApiResponse.<List<UserResponse>>builder()
                .code(1000)
                .message("Lấy danh sách bạn bè thành công")
                .result(friends)
                .build());
    }

    /**
     * Kiểm tra hai người đã là bạn bè chưa
     * GET /api/v1/friends/status/{targetUserId}
     */
    @GetMapping("/status/{targetUserId}")
    public ResponseEntity<ApiResponse<Boolean>> checkFriendship(
            Authentication authentication,
            @PathVariable String targetUserId) {

        String currentUsername = authentication.getName();
        boolean isFriend = friendService.isFriend(currentUsername, targetUserId);

        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .code(1000)
                .message("Kiểm tra trạng thái bạn bè thành công")
                .result(isFriend)
                .build());
    }

    /**
     * Hủy kết bạn (Unfriend)
     * DELETE /api/v1/friends/{friendId}
     */
    @DeleteMapping("/{friendId}")
    public ResponseEntity<ApiResponse<String>> unfriend(
            Authentication authentication,
            @PathVariable String friendId) {

        String currentUsername = authentication.getName();
        friendService.unfriend(currentUsername, friendId);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000)
                .message("Hủy kết bạn thành công")
                .result("OK")
                .build());
    }
}