package com.migo.backend.service.impl;

import org.springframework.stereotype.Service;

import com.migo.backend.dto.response.UserResponse;
import com.migo.backend.entity.User;
import com.migo.backend.exception.AppException;
import com.migo.backend.exception.ErrorCode;
import com.migo.backend.repository.UserRepository;
import com.migo.backend.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserResponse searchUserByUsername(String currentUsername, String keyword) {
        User user = userRepository.findByUsernameIgnoreCase(keyword.trim())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.getUsername().equalsIgnoreCase(currentUsername)) {
            throw new AppException(ErrorCode.CANNOT_ADD_FRIEND_SELF);
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
