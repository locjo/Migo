package com.migo.backend.service;

import java.util.List;

import com.migo.backend.dto.request.CreateGroupRequest;
import com.migo.backend.dto.response.ConversationResponse;

public interface ConversationService {
    ConversationResponse createGroupConversation(String currentUsername, CreateGroupRequest request);
    List<ConversationResponse> getUserConversations(String currentUsername);
    ConversationResponse getConversationById(String currentUsername, String conversationId);
    ConversationResponse addMemberToGroup(String currentUsername, String conversationId, String targetUserId);
    ConversationResponse removeMemberFromGroup(String currentUsername, String conversationId, String targetUserId);
    void leaveGroup(String currentUsername, String conversationId);
}