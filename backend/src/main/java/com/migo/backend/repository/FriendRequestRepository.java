package com.migo.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.migo.backend.entity.FriendRequest;

@Repository
public interface FriendRequestRepository extends MongoRepository<FriendRequest, String> {

    // Kiểm tra xem đã có lời mời kết bạn 2 chiều giữa 2 người này chưa
    @Query("{ $or: [ { 'from': ObjectId(?0), 'to': ObjectId(?1) }, { 'from': ObjectId(?1), 'to': ObjectId(?0) } ] }")
    Optional<FriendRequest> findRequestBetween(String userId1, String userId2);

    // Lấy danh sách lời mời gửi tới "to" (dựa vào ID của User trong @DBRef)
    @Query("{ 'to': ObjectId(?0) }")
    List<FriendRequest> findByToId(String toId);

    
}