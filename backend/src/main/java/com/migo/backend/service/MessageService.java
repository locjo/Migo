package com.migo.backend.service;

import java.util.List;

import com.migo.backend.dto.request.DirectMessageRequest;
import com.migo.backend.dto.request.GroupMessageRequest;
import com.migo.backend.dto.response.MessageResponse;

public interface MessageService {
    MessageResponse sendDirectMessage(String currentUsername, DirectMessageRequest request);

    MessageResponse sendGroupMessage(String currentUsername, GroupMessageRequest request);
    
    List<MessageResponse> getConversationMessages(String currentUsername, String conversationId);

    void markMessagesAsRead(String conversationId, String userId);
}