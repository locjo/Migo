package com.migo.backend.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.migo.backend.dto.request.ApiResponse;
import com.migo.backend.dto.request.DirectMessageRequest;
import com.migo.backend.dto.request.GroupMessageRequest;
import com.migo.backend.dto.request.ReadReceiptDto;
import com.migo.backend.dto.response.MessageResponse;
import com.migo.backend.service.MessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j // 👈 Thêm annotation này để sử dụng đối tượng log
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * POST /api/messages/direct
     * Gửi tin nhắn trực tiếp đến một user khác.
     */
    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<MessageResponse>> sendDirectMessage(
            Authentication authentication,
            @Valid @RequestBody DirectMessageRequest request) {

        String currentUsername = authentication.getName();
        MessageResponse message = messageService.sendDirectMessage(currentUsername, request);

        ApiResponse<MessageResponse> response = ApiResponse.<MessageResponse>builder()
                .code(1000)
                .message("Gửi tin nhắn trực tiếp thành công")
                .result(message)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/messages/{conversationId}/messages
     * Lấy danh sách tất cả tin nhắn trong cuộc trò chuyện
     */
    @GetMapping("/{conversationId}/messages")
    public ResponseEntity<List<MessageResponse>> getConversationMessages(
            Authentication authentication,
            @PathVariable("conversationId") String conversationId) {

        String currentUsername = authentication.getName();
        List<MessageResponse> responses = messageService.getConversationMessages(currentUsername, conversationId);

        return ResponseEntity.ok(responses);
    }

    /**
     * POST /api/messages/group
     * Gửi tin nhắn nhóm.
     */
    @PostMapping("/group")
    public ResponseEntity<ApiResponse<MessageResponse>> sendGroupMessage(
            Authentication authentication,
            @Valid @RequestBody GroupMessageRequest request) {

        String currentUsername = authentication.getName();
        MessageResponse message = messageService.sendGroupMessage(currentUsername, request);

        ApiResponse<MessageResponse> response = ApiResponse.<MessageResponse>builder()
                .code(1000)
                .message("Gửi tin nhắn nhóm thành công")
                .result(message)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * STOMP Message Handler
     * Client gửi tới: /app/chat/read
     * Broadcast tới: /topic/conversation/{conversationId}/read
     */
    @MessageMapping("/chat/read")
    public void handleMarkAsRead(@Payload ReadReceiptDto payload, Principal principal) {
        if (principal == null || payload.getConversationId() == null) {
            return;
        }

        String userId = principal.getName();
        String conversationId = payload.getConversationId();

        // 1. Cập nhật MongoDB
        messageService.markMessagesAsRead(conversationId, userId);

        // 2. Broadcast sự kiện
        String destination = "/topic/conversation/" + conversationId + "/read";
        Object response = java.util.Map.of( 
                "conversationId", conversationId,
                "readerId", userId);

        messagingTemplate.convertAndSend(destination, response); 
        log.info("👁️ User [{}] đã xem tin nhắn trong room [{}]", userId, conversationId);
    }
}