package com.migo.backend.service;

import com.migo.backend.dto.response.UserResponse;

public interface UserService {
    UserResponse searchUserByUsername(String currentUsername, String keyword);
}
