package com.migo.backend.entity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "conversations")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@CompoundIndexes({
    @CompoundIndex(name = "user_last_message_idx", def = "{'participants.userId': 1, 'lastMessage.createdAt': -1}")
})
public class Conversation {
    @Id
    private String id;

    private String name;       

    private String avatar;     

    private String adminId;

    private List<ParticipantInfo> participants;

    private ConversationType type;

    private LastMessageInfo lastMessage;

    private Map<String, Integer> unreadCount;
    
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
