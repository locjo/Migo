package com.migo.backend.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.migo.backend.dto.request.CreateGroupRequest;
import com.migo.backend.dto.response.ConversationResponse;
import com.migo.backend.dto.response.ParticipantResponse;
import com.migo.backend.entity.Conversation;
import com.migo.backend.entity.ConversationType;
import com.migo.backend.entity.ParticipantInfo;
import com.migo.backend.entity.ParticipantRole;
import com.migo.backend.entity.User;
import com.migo.backend.exception.AppException;
import com.migo.backend.exception.ErrorCode;
import com.migo.backend.repository.ConversationRepository;
import com.migo.backend.repository.FriendRepository;
import com.migo.backend.repository.UserRepository;
import com.migo.backend.service.ConversationService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ConversationResponse createGroupConversation(String currentUsername, CreateGroupRequest request) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<ParticipantInfo> participants = new ArrayList<>();
        Map<String, Integer> unreadCountMap = new HashMap<>();

        // 1. Thêm Người tạo nhóm (Owner)
        participants.add(ParticipantInfo.builder()
                .userId(currentUser)
                .role(ParticipantRole.Owner)
                .joinedAt(Instant.now())
                .build());
        unreadCountMap.put(currentUser.getId(), 0);

        // 2. Thêm danh sách thành viên từ Request (Tối ưu bằng Batch Query)
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            List<String> memberIds = request.getMemberIds().stream()
                    .filter(id -> !id.equals(currentUser.getId()))
                    .distinct()
                    .collect(Collectors.toList());

            if (!memberIds.isEmpty()) {
                List<User> memberUsers = userRepository.findAllById(memberIds);
                if (memberUsers.size() != memberIds.size()) {
                    throw new AppException(ErrorCode.USER_NOT_EXISTED);
                }

                for (User memberUser : memberUsers) {
                    boolean isFriend = friendRepository.existsFriendshipBetween(currentUser.getId(),
                            memberUser.getId());
                    if (!isFriend) {
                        throw new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND);
                    }

                    participants.add(ParticipantInfo.builder()
                            .userId(memberUser)
                            .role(ParticipantRole.Member)
                            .joinedAt(Instant.now())
                            .build());
                    unreadCountMap.put(memberUser.getId(), 0);
                }
            }
        }

        // 3. Khởi tạo Conversation Entity
        Conversation conversation = Conversation.builder()
                .name(request.getName())
                .avatar(request.getAvatar())
                .participants(participants)
                .type(ConversationType.Group)
                .adminId(currentUser.getId())
                .unreadCount(unreadCountMap)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        return toConversationResponse(conversationRepository.save(conversation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationResponse> getUserConversations(String currentUsername) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<Conversation> conversations = conversationRepository.findByUserId(currentUser.getId());
        return conversations.stream()
                .map(this::toConversationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationResponse getConversationById(String currentUsername, String conversationId) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        validateParticipant(conversation, currentUser.getId());
        return toConversationResponse(conversation);
    }

    @Override
    @Transactional
    public ConversationResponse addMemberToGroup(String currentUsername, String conversationId, String targetUserId) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        validateGroupType(conversation);
        validateParticipant(conversation, currentUser.getId());

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (conversation.getParticipants() == null) {
            conversation.setParticipants(new ArrayList<>());
        }

        boolean exists = conversation.getParticipants().stream()
                .anyMatch(p -> p.getUserId() != null && p.getUserId().getId().equals(targetUser.getId()));

        if (exists) {
            throw new AppException(ErrorCode.USER_ALREADY_IN_GROUP);
        }

        // Kiểm tra điều kiện bạn bè trước khi thêm người vào nhóm
        boolean isFriend = friendRepository.existsFriendshipBetween(currentUser.getId(), targetUser.getId());
        if (!isFriend) {
            throw new AppException(ErrorCode.FRIENDSHIP_NOT_FOUND);
        }

        conversation.getParticipants().add(ParticipantInfo.builder()
                .userId(targetUser)
                .role(ParticipantRole.Member)
                .joinedAt(Instant.now())
                .build());

        if (conversation.getUnreadCount() == null) {
            conversation.setUnreadCount(new HashMap<>());
        }
        conversation.getUnreadCount().put(targetUser.getId(), 0);
        conversation.setUpdatedAt(Instant.now());

        return toConversationResponse(conversationRepository.save(conversation));
    }

    @Override
    @Transactional
    public ConversationResponse removeMemberFromGroup(String currentUsername, String conversationId,
            String targetUserId) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        validateGroupType(conversation);

        ParticipantInfo currentParticipant = getParticipantInfo(conversation, currentUser.getId());
        if (currentParticipant.getRole() != ParticipantRole.Owner
                && currentParticipant.getRole() != ParticipantRole.Admin) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        if (conversation.getParticipants() != null) {
            conversation.getParticipants()
                    .removeIf(p -> p.getUserId() != null && p.getUserId().getId().equals(targetUserId));
        }

        if (conversation.getUnreadCount() != null) {
            conversation.getUnreadCount().remove(targetUserId);
        }

        conversation.setUpdatedAt(Instant.now());
        return toConversationResponse(conversationRepository.save(conversation));
    }

    @Override
    @Transactional
    public void leaveGroup(String currentUsername, String conversationId) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        validateGroupType(conversation);
        validateParticipant(conversation, currentUser.getId());

        // 1. Xóa người dùng khỏi nhóm
        if (conversation.getParticipants() != null) {
            conversation.getParticipants()
                    .removeIf(p -> p.getUserId() != null && p.getUserId().getId().equals(currentUser.getId()));
        }

        if (conversation.getUnreadCount() != null) {
            conversation.getUnreadCount().remove(currentUser.getId());
        }

        // 2. Xử lý nhóm không còn ai
        if (conversation.getParticipants() == null || conversation.getParticipants().isEmpty()) {
            conversationRepository.delete(conversation);
            return;
        }

        // 3. Tự động chuyển quyền Admin nếu người rời nhóm là Admin/Owner hiện tại
        if (currentUser.getId().equals(conversation.getAdminId())) {
            ParticipantInfo newAdmin = conversation.getParticipants().get(0);
            newAdmin.setRole(ParticipantRole.Admin);
            conversation.setAdminId(newAdmin.getUserId().getId());
        }

        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);
    }

    // --- HELPER METHODS ---
 
    private void validateParticipant(Conversation conversation, String userId) {
        if (conversation.getParticipants() == null) {
            throw new AppException(ErrorCode.NOT_IN_CONVERSATION);
        }
        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getUserId() != null && p.getUserId().getId().equals(userId));
        if (!isParticipant) {
            throw new AppException(ErrorCode.NOT_IN_CONVERSATION);
        }
    }

    private void validateGroupType(Conversation conversation) {
        if (conversation.getType() != ConversationType.Group) {
            throw new AppException(ErrorCode.INVALID_CONVERSATION_TYPE);
        }
    }

    private ParticipantInfo getParticipantInfo(Conversation conversation, String userId) {
        if (conversation.getParticipants() == null) {
            throw new AppException(ErrorCode.NOT_IN_CONVERSATION);
        }
        return conversation.getParticipants().stream()
                .filter(p -> p.getUserId() != null && p.getUserId().getId().equals(userId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.NOT_IN_CONVERSATION));
    }


    private ConversationResponse toConversationResponse(Conversation conversation) {
        List<ParticipantResponse> participantResponses = new ArrayList<>();

        if (conversation.getParticipants() != null) {
            for (ParticipantInfo p : conversation.getParticipants()) {
                if (p.getUserId() != null) {
                    User u = p.getUserId();
                    participantResponses.add(ParticipantResponse.builder()
                            .userId(u.getId())
                            .displayName(u.getDisplayName() != null ? u.getDisplayName() : u.getUsername())
                            .username(u.getUsername())
                            .avatarUrl(u.getAvatarUrl())
                            .role(p.getRole() != null ? p.getRole().name() : null)
                            .joinedAt(p.getJoinedAt())
                            .build());
                }
            }
        }

        return ConversationResponse.builder()
                .id(conversation.getId())
                .name(conversation.getName())
                .avatar(conversation.getAvatar())
                .type(conversation.getType() != null ? conversation.getType().name() : null)
                .adminId(conversation.getAdminId())
                .participants(participantResponses)
                .unreadCount(conversation.getUnreadCount() != null ? conversation.getUnreadCount() : new HashMap<>())
                .lastMessage(conversation.getLastMessage() != null ? conversation.getLastMessage().getContent() : null)
                .createdAt(conversation.getCreatedAt())
                .updatedAt(conversation.getUpdatedAt())
                .build();
    }
}