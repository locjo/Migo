package com.migo.backend.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.migo.backend.dto.response.UserResponse;
import com.migo.backend.entity.Friend;
import com.migo.backend.entity.User;
import com.migo.backend.exception.AppException;
import com.migo.backend.exception.ErrorCode;
import com.migo.backend.repository.FriendRepository;
import com.migo.backend.repository.UserRepository;
import com.migo.backend.service.FriendService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service  
public class FriendServiceImpl implements FriendService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository; 


    // 1. Kiểm tra trạng thái bạn bè giữa 2 tài khoản
    @Override
    public boolean isFriend(String currentUsername, String targetUserId) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Không thể tự kiểm tra bạn bè với chính mình
        if (currentUser.getId().equals(targetUserId)) {
            return false;
        }

        // Kiểm tra xem ID của targetUser có tồn tại hay không
        userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Gọi repository kiểm tra quan hệ bạn bè 2 chiều
        return friendRepository.existsFriendshipBetween(currentUser.getId(), targetUserId);
    }

    // 4. Hủy kết bạn (Unfriend)
    @Override
    @Transactional
    public void unfriend(String currentUsername, String friendId) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Friend friendship = friendRepository.findFriendshipBetween(currentUser.getId(), friendId)
                .orElseThrow(() -> new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND));

        friendRepository.delete(friendship);
    }

    // 5. Lấy danh sách bạn bè
    @Override
    public List<UserResponse> getFriendsList(String currentUsername) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<Friend> friends = friendRepository.findAllFriends(currentUser.getId());

        return friends.stream()
                .map(f -> { 
                    // 1. Xác định ai là người bạn (không lấy chính mình)
                    boolean isUserA = f.getUserA() != null && f.getUserA().getId().equals(currentUser.getId());
                    User friendUser = isUserA ? f.getUserB() : f.getUserA();

                    // 2. Map đối tượng User của người bạn sang UserResponse
                    return toUserResponse(friendUser);
                })
                .collect(Collectors.toList());
    }

    // Private helper method mapping từ User Entity sang UserResponse DTO
    private UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .roles(Collections.singletonList(user.getRole() != null ? user.getRole() : "USER"))
                .build();
    }
}
 