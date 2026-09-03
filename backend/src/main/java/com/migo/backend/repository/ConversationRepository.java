package com.migo.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.migo.backend.entity.Conversation;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    // Lấy tất cả các hội thoại của user sắp xếp theo thời gian tin nhắn mới nhất (tận dụng Compound Index của bạn)
    @Query(value = "{ 'participants.userId.id': ?0 }", sort = "{ 'lastMessage.createdAt': -1 }")
    List<Conversation> findByUserId(String userId);

    // Tìm cuộc trò chuyện 1-1 giữa 2 người dùng
    @Query("{ 'type': 'DIRECT', 'participants.userId': { $all: [?0, ?1] } }")
    Optional<Conversation> findDirectConversation(String userId1, String userId2);

    // Tìm tất cả conversation mà user có trong danh sách participants
    // Chỉ lấy trường id (_id: 1) giống trong ảnh
    @Query(value = "{ 'participants.userId': ?0 }", fields = "{ '_id': 1 }")
    List<Conversation> findConversationIdsByUserId(String userId);
}