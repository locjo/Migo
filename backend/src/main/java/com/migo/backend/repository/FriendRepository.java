package com.migo.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.migo.backend.entity.Friend;

@Repository
public interface FriendRepository extends MongoRepository<Friend, String> {

    // Tìm mối quan hệ bạn bè 2 chiều userA và userB
    @Query("{ $or: [ { 'userA': ObjectId(?0), 'userB': ObjectId(?1) }, { 'userA': ObjectId(?1), 'userB': ObjectId(?0) } ] }")
    Optional<Friend> findFriendshipBetween(String userId1, String userId2);

    // Lấy tất cả bạn bè của 1 User
    @Query("{ $or: [ { 'userA': ObjectId(?0) }, { 'userB': ObjectId(?0) } ] }")
    List<Friend> findAllFriends(String userId);

    @Query(value = "{ $or: [ { 'userA': ObjectId(?0), 'userB': ObjectId(?1) }, { 'userA': ObjectId(?1), 'userB': ObjectId(?0) } ] }", exists = true)
    boolean existsFriendshipBetween(String userId1, String userId2);

}