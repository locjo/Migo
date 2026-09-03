package com.migo.backend.repository;

import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import com.migo.backend.entity.Message;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    @Query(value = "{ 'conversationId': ObjectId(?0) }", sort = "{ 'createdAt': 1 }")
    List<Message> findByConversationIdCustom(String conversationId);

    @Query(value = "{ 'conversationId.$id': ?0 }", sort = "{ 'createdAt': -1 }")
    Optional<Message> findLatestMessageByConvoId(String convoId);

    @Query("{ 'conversationId.$id': ?0, 'readBy': { $ne: ?1 } }")
    @Update("{ '$addToSet': { 'readBy': ?1 } }")
    void updateReadByForConversation(ObjectId conversationId, String userId);
}