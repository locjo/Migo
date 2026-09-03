package com.migo.backend.dto.response;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor; // Phải là DTO này
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationResponse {
    private String id;
    private String name;            // Tên nhóm hoặc Tên người bạn (nếu là chat 1-1)
    private String avatar;          // Avatar nhóm hoặc Avatar người bạn
    private String adminId;         // ID người tạo nhóm (null nếu là 1-1)
    private List<ParticipantResponse> participants; // Danh sách thành viên\
    private Map<String, Integer> unreadCount; // Map: { "userId": 0 }
    private String type;  // Loại cuộc trò chuyện (Group hoặc Direct)
    private String lastMessage;    // Tin nhắn mới nhất
    private Instant createdAt;
    private Instant updatedAt;
}