package com.migo.backend.service;

import java.util.List;

import com.migo.backend.dto.response.UserResponse;

public interface FriendService {
    
    void unfriend(String currentUsername, String friendId);
    boolean isFriend(String currentUsername, String targetUserId);
    List<UserResponse> getFriendsList(String currentUsername);
}