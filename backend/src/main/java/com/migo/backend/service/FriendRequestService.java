package com.migo.backend.service;

import java.util.List; // DTO trả về thông tin cơ bản của User

import com.migo.backend.dto.request.FriendRequestRequest;
import com.migo.backend.dto.response.FriendResponse;
import com.migo.backend.dto.response.UserResponse;

public interface FriendRequestService {
  FriendResponse sendFriendRequest(String currentUsername, FriendRequestRequest request);
    FriendResponse acceptFriendRequest(String currentUsername, String requestId);
    FriendResponse rejectFriendRequest(String currentUsername, String requestId);
    List<UserResponse> getPendingRequests(String currentUsername);
}