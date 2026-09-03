package com.migo.backend.service.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.migo.backend.dto.request.FriendRequestRequest;
import com.migo.backend.dto.response.FriendResponse;
import com.migo.backend.dto.response.UserResponse;
import com.migo.backend.entity.Friend;
import com.migo.backend.entity.FriendRequest;
import com.migo.backend.entity.RequestStatus;
import com.migo.backend.entity.User;
import com.migo.backend.exception.AppException;
import com.migo.backend.exception.ErrorCode;
import com.migo.backend.repository.FriendRepository;
import com.migo.backend.repository.FriendRequestRepository;
import com.migo.backend.repository.UserRepository;
import com.migo.backend.service.FriendRequestService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendRequestServiceImpl implements FriendRequestService {

    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;

    // 1. Gửi lời mời kết bạn
    @Override 
    public FriendResponse sendFriendRequest(String currentUsername, FriendRequestRequest request) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        User friendUser = userRepository.findById(request.getTo()) 
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (currentUser.getId().equals(friendUser.getId())) {
            throw new AppException(ErrorCode.CANNOT_ADD_FRIEND_SELF);
        }

        // Kiểm tra 1: Đã là bạn bè chưa?
        friendRepository.findFriendshipBetween(currentUser.getId(), friendUser.getId())
                .ifPresent(f -> {
                    throw new AppException(ErrorCode.FRIEND_REQUEST_ALREADY_EXISTS);
                });

        // Kiểm tra 2: Đã có lời mời đang chờ chưa?
        friendRequestRepository.findRequestBetween(currentUser.getId(), friendUser.getId())
                .ifPresent(f -> {
                    if (f.getStatus() == RequestStatus.Pending) {
                        throw new AppException(ErrorCode.FRIEND_REQUEST_ALREADY_EXISTS);
                    }
                });

        // Lưu lời mời kết bạn (Entity)
        FriendRequest savedFriend = FriendRequest.builder()
                .from(currentUser)
                .to(friendUser)
                .message(request.getMessage())
                .status(RequestStatus.Pending)
                .createdAt(Instant.now())
                .build();

        friendRequestRepository.save(savedFriend);

        return FriendResponse.builder()
                .id(savedFriend.getId())
                .senderId(savedFriend.getFrom().getId())
                .receiverId(savedFriend.getTo().getId())
                .status(savedFriend.getStatus() != null ? savedFriend.getStatus().name() : RequestStatus.Pending.name())
                .createdAt(savedFriend.getCreatedAt())
                .build();
    }

    // 2. Chấp nhận lời mời kết bạn
    @Override
    @Transactional
    public FriendResponse acceptFriendRequest(String currentUsername, String requestId) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_NOT_FOUND));

        // Ràng buộc: Chỉ người nhận (receiver) mới được bấm chấp nhận
        if (!request.getTo().getId().equals(currentUser.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // Tạo bản ghi Bạn bè chính thức mới
        Friend savedFriend = Friend.builder()
                .userA(request.getFrom())
                .userB(request.getTo())
                .createdAt(Instant.now())
                .build();

        friendRepository.save(savedFriend);

        request.setStatus(RequestStatus.Accepted);
        request.setUpdatedAt(Instant.now());
        friendRequestRepository.save(request);

        return FriendResponse.builder()
                .id(savedFriend.getId())
                .senderId(savedFriend.getUserA().getId())
                .receiverId(savedFriend.getUserB().getId())
                .status(request.getStatus().name())
                .createdAt(savedFriend.getCreatedAt())
                .build();
    }

    // 3. Từ chối / Hủy lời mời kết bạn
    @Override
    @Transactional
    public FriendResponse rejectFriendRequest(String currentUsername, String requestId) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_NOT_FOUND));

        boolean isSender = request.getFrom().getId().equals(currentUser.getId());
        boolean isReceiver = request.getTo().getId().equals(currentUser.getId());

        if (!isSender && !isReceiver) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        request.setStatus(RequestStatus.Rejected);
        request.setUpdatedAt(Instant.now());
        friendRequestRepository.save(request);

        return FriendResponse.builder()
                .id(request.getId())
                .senderId(request.getFrom().getId())
                .receiverId(request.getTo().getId())
                .status(request.getStatus().name())
                .createdAt(request.getCreatedAt())
                .build();
    }

    // 4. Lấy danh sách lời mời chờ duyệt
    @Override
    public List<UserResponse> getPendingRequests(String currentUsername) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<FriendRequest> requests = friendRequestRepository.findByToId(currentUser.getId());

        return requests.stream()
                .filter(r -> r.getStatus() == RequestStatus.Pending)
                .map(r -> toUserResponse(r.getFrom()))
                .collect(Collectors.toList());
    }

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