package com.migo.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.migo.backend.dto.request.ApiResponse;
import com.migo.backend.dto.request.FriendRequestRequest;
import com.migo.backend.dto.response.FriendResponse;
import com.migo.backend.dto.response.UserResponse;
import com.migo.backend.service.FriendRequestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendRequestService friendService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 1. Gửi lời mời kết bạn
     * POST /api/v1/friends/requests/{friendId}
     */
    @PostMapping("/requests")
    public ResponseEntity<ApiResponse<FriendResponse>> sendFriendRequest(
            Authentication authentication,
            @Valid @RequestBody FriendRequestRequest request) {

        String currentUsername = authentication.getName();
        FriendResponse message = friendService.sendFriendRequest(currentUsername, request);

        // Bắn thông báo Realtime tới người nhận
        String destination = "/topic/users/" + request.getTo() + "/friend-requests";
        messagingTemplate.convertAndSend(destination, message );

        ApiResponse<FriendResponse> response = ApiResponse.<FriendResponse>builder()
                .code(1000)
                .message("Gửi lời mời kết bạn thành công")
                .result(message)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 2. Chấp nhận lời mời kết bạn
     * PUT /api/v1/friends/requests/{requestId}/accept
     */
    @PutMapping("/requests/{requestId}/accept")
    public ResponseEntity<ApiResponse<FriendResponse>> acceptFriendRequest(
            Authentication authentication,
            @PathVariable String requestId) {

        String currentUsername = authentication.getName();
        FriendResponse message = friendService.acceptFriendRequest(currentUsername, requestId);

        ApiResponse<FriendResponse> response = ApiResponse.<FriendResponse>builder()
                .code(1000)
                .message("Đã chấp nhận lời mời kết bạn.")
                .result(message)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 3. Từ chối / Hủy lời mời kết bạn
     * DELETE /api/v1/friends/requests/{requestId}
     */
    @PutMapping("/requests/{requestId}/reject")
    public ResponseEntity<ApiResponse<FriendResponse>> rejectFriendRequest(
            Authentication authentication,
            @PathVariable String requestId) {

        String currentUsername = authentication.getName();
        FriendResponse message = friendService.rejectFriendRequest(currentUsername, requestId);

        ApiResponse<FriendResponse> response = ApiResponse.<FriendResponse>builder()
                .code(1000)
                .message("Đã từ chối lời mời kết bạn.")
                .result(message)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 6. Lấy danh sách lời mời kết bạn đang chờ duyệt (Pending Requests)
     * GET /api/v1/friends/requests/pending
     */
    @GetMapping("/requests/pending")
    public ResponseEntity<List<UserResponse>> getPendingRequests(Authentication authentication) {
        String currentUsername = authentication.getName();
        List<UserResponse> pendingRequests = friendService.getPendingRequests(currentUsername);

        return ResponseEntity.ok(pendingRequests);
    }
}