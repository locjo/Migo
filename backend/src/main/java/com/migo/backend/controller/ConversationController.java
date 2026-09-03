package com.migo.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.migo.backend.dto.request.ApiResponse;
import com.migo.backend.dto.request.CreateGroupRequest;
import com.migo.backend.dto.response.ConversationResponse;
import com.migo.backend.service.ConversationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    // 1. Tạo nhóm chat mới
    @PostMapping("/groups")
    public ResponseEntity<ApiResponse<ConversationResponse>> createGroup(
            Authentication authentication,
            @Valid @RequestBody CreateGroupRequest request) {

        String currentUsername = authentication.getName();
        ConversationResponse response = conversationService.createGroupConversation(currentUsername, request);

        return ResponseEntity.ok(ApiResponse.<ConversationResponse>builder()
                .code(1000)
                .message("Tạo nhóm chat thành công")
                .result(response)
                .build());
    }

    // 2. Lấy danh sách tất cả hội thoại của người dùng
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<ConversationResponse>>> getUserConversations(Authentication authentication) {
        String currentUsername = authentication.getName();
        List<ConversationResponse> response = conversationService.getUserConversations(currentUsername);

        return ResponseEntity.ok(ApiResponse.<List<ConversationResponse>>builder()
                .code(1000)
                .message("Lấy danh sách cuộc trò chuyện thành công")
                .result(response)
                .build());
    }

    // 3. Lấy thông tin chi tiết một hội thoại
    @GetMapping("/{conversationId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> getConversationById(
            Authentication authentication,
            @PathVariable String conversationId) {

        String currentUsername = authentication.getName();
        ConversationResponse response = conversationService.getConversationById(currentUsername, conversationId);

        return ResponseEntity.ok(ApiResponse.<ConversationResponse>builder()
                .code(1000)
                .message("Lấy chi tiết cuộc trò chuyện thành công")
                .result(response)
                .build());
    }
    
    // 4. Thêm thành viên vào nhóm
    @PostMapping("/groups/{conversationId}/members/{targetUserId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> addMember(
            Authentication authentication,
            @PathVariable String conversationId,
            @PathVariable String targetUserId) {

        String currentUsername = authentication.getName();
        ConversationResponse response = conversationService.addMemberToGroup(currentUsername, conversationId, targetUserId);

        return ResponseEntity.ok(ApiResponse.<ConversationResponse>builder()
                .code(1000)
                .message("Thêm thành viên vào nhóm thành công")
                .result(response)
                .build());
    }

    // 5. Kick thành viên khỏi nhóm (Chỉ Admin)
    @DeleteMapping("/groups/{conversationId}/members/{targetUserId}")
    public ResponseEntity<ApiResponse<ConversationResponse>> removeMember(
            Authentication authentication,
            @PathVariable String conversationId,
            @PathVariable String targetUserId) {

        String currentUsername = authentication.getName();
        ConversationResponse response = conversationService.removeMemberFromGroup(currentUsername, conversationId, targetUserId);

        return ResponseEntity.ok(ApiResponse.<ConversationResponse>builder()
                .code(1000)
                .message("Đã xóa thành viên khỏi nhóm")
                .result(response)
                .build());
    }

    // 6. Rời khỏi nhóm chat
    @PostMapping("/groups/{conversationId}/leave")
    public ResponseEntity<ApiResponse<String>> leaveGroup(
            Authentication authentication,
            @PathVariable String conversationId) {

        String currentUsername = authentication.getName();
        conversationService.leaveGroup(currentUsername, conversationId);

        return ResponseEntity.ok(ApiResponse.<String>builder()
                .code(1000)
                .message("Đã rời khỏi nhóm chat thành công")
                .result("OK")
                .build());
    }
}