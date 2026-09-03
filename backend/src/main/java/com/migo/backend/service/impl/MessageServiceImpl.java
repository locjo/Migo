package com.migo.backend.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.migo.backend.dto.request.DirectMessageRequest;
import com.migo.backend.dto.request.GroupMessageRequest;
import com.migo.backend.dto.response.MessageResponse;
import com.migo.backend.entity.Conversation;
import com.migo.backend.entity.ConversationType;
import com.migo.backend.entity.LastMessageInfo;
import com.migo.backend.entity.Message;
import com.migo.backend.entity.ParticipantInfo;
import com.migo.backend.entity.ParticipantRole;
import com.migo.backend.entity.User;
import com.migo.backend.exception.AppException;
import com.migo.backend.exception.ErrorCode;
import com.migo.backend.repository.ConversationRepository;
import com.migo.backend.repository.MessageRepository;
import com.migo.backend.repository.UserRepository;
import com.migo.backend.service.MessageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    
    // 1. Thay thế SocketIOServer bằng SimpMessagingTemplate
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public MessageResponse sendDirectMessage(String currentUsername, DirectMessageRequest request) {
        // 1. Tìm thông tin người gửi (Sender)
        User sender = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 2. Tìm thông tin người nhận (Recipient)
        User recipient = userRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 3. Xử lý Conversation
        Conversation conversation; 
        if (request.getConversationId() != null && !request.getConversationId().isBlank()) {
            conversation = conversationRepository.findById(request.getConversationId())
                    .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));
        } else {
            conversation = conversationRepository.findDirectConversation(sender.getId(), recipient.getId())
                    .orElseGet(() -> {
                        ParticipantInfo senderParticipant = ParticipantInfo.builder()
                                .userId(sender)
                                .role(ParticipantRole.Member)
                                .joinedAt(Instant.now())
                                .build();

                        ParticipantInfo recipientParticipant = ParticipantInfo.builder()
                                .userId(recipient)
                                .role(ParticipantRole.Member)
                                .joinedAt(Instant.now())
                                .build();

                        Conversation newConversation = Conversation.builder()
                                .type(ConversationType.Direct)
                                .participants(List.of(senderParticipant, recipientParticipant))
                                .createdAt(Instant.now())
                                .updatedAt(Instant.now())
                                .build();

                        return conversationRepository.save(newConversation);
                    });
        }

        // 4. Tạo và lưu Message mới
        Message message = Message.builder()
                .conversationId(conversation)
                .senderId(sender)
                .content(request.getContent())
                .imgUrl(request.getImgUrl())
                .createdAt(Instant.now())
                .build();

        Message savedMessage = messageRepository.save(message);

        // 5. Cập nhật lastMessage và thời gian updatedAt của Conversation
        LastMessageInfo lastMessageInfo = LastMessageInfo.builder()
                .content(savedMessage.getContent())
                .senderId(sender)
                .createdAt(savedMessage.getCreatedAt())
                .build();

        conversation.setLastMessage(lastMessageInfo);
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        // 6. Map sang Response DTO (sử dụng hàm dùng chung toMessageResponse)
        MessageResponse response = toMessageResponse(savedMessage);

        // 7. BẮN REALTIME TỚI TOPIC CỦA CONVERSATION
        messagingTemplate.convertAndSend("/topic/conversation/" + conversation.getId(), response);
        log.info("Phát tin nhắn realtime tới topic: /topic/conversation/{}", conversation.getId());

        return response;
    }

    @Override
    @Transactional
    public MessageResponse sendGroupMessage(String currentUsername, GroupMessageRequest request) {
        // 1. Tìm thông tin người gửi
        User sender = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // 2. Kiểm tra cuộc trò chuyện nhóm có tồn tại không
        Conversation conversation = conversationRepository.findById(request.getConversationId())
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        // 3. Đảm bảo đây là chat nhóm và người gửi thuộc danh sách participants
        if (conversation.getType() != ConversationType.Group) {
            throw new AppException(ErrorCode.INVALID_CONVERSATION_TYPE);
        }

        boolean isParticipant = conversation.getParticipants().stream()
                .anyMatch(p -> p.getUserId() != null && p.getUserId().getId().equals(sender.getId()));

        if (!isParticipant) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // 4. Tạo và lưu tin nhắn nhóm
        Message message = Message.builder()
                .conversationId(conversation)
                .senderId(sender)
                .content(request.getContent())
                .imgUrl(request.getImgUrl())
                .createdAt(Instant.now())
                .build();

        Message savedMessage = messageRepository.save(message);

        LastMessageInfo lastMessageInfo = LastMessageInfo.builder()
                .content(savedMessage.getContent())
                .senderId(sender)
                .createdAt(savedMessage.getCreatedAt())
                .build();

        conversation.setLastMessage(lastMessageInfo);
        conversation.setUpdatedAt(Instant.now());
        conversationRepository.save(conversation);

        // 5. Map sang Response DTO (Đã sửa lỗi gán nhầm ID trước đó)
        MessageResponse response = toMessageResponse(savedMessage);

        // 6. BẮN REALTIME TỚI TOPIC CỦA GROUP CONVERSATION
        messagingTemplate.convertAndSend("/topic/conversation/" + conversation.getId(), response);
        log.info("Phát tin nhắn realtime group tới topic: /topic/conversation/{}", conversation.getId());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MessageResponse> getConversationMessages(String currentUsername, String conversationId) {
        User currentUser = userRepository.findByUsernameIgnoreCase(currentUsername)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new AppException(ErrorCode.CONVERSATION_NOT_FOUND));

        boolean isParticipant = conversation.getParticipants() != null &&
                conversation.getParticipants().stream()
                        .anyMatch(p -> p.getUserId() != null
                                && p.getUserId().getId().equals(currentUser.getId()));

        if (!isParticipant) {
            throw new AppException(ErrorCode.NOT_IN_CONVERSATION);
        }

        List<Message> messages = messageRepository.findByConversationIdCustom(conversationId);

        return messages.stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void markMessagesAsRead(String conversationId, String userId) {
        try {
            messageRepository.updateReadByForConversation(new ObjectId(conversationId), userId);
            log.info("✅ Đã cập nhật readBy cho conversationId: {}, userId: {}", conversationId, userId);
        } catch (Exception e) {
            log.error("❌ Lỗi khi cập nhật readBy: {}", e.getMessage());
        }
    }

    private MessageResponse toMessageResponse(Message message) {
        String senderId = null;
        if (message.getSenderId() != null) {
            senderId = message.getSenderId().getId();
        }

        String convId = null;
        if (message.getConversationId() != null) {
            convId = message.getConversationId().getId();
        }

        return MessageResponse.builder()
                .id(message.getId())
                .conversationId(convId)
                .senderId(senderId)
                .content(message.getContent())
                .imgUrl(message.getImgUrl())
                .createdAt(message.getCreatedAt())
                .build();
    }
}