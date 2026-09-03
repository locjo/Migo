package com.migo.backend.entity;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "friend_requests")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@CompoundIndex(name = "from_to_unique_idx", def = "{'from': 1, 'to': 1}", unique = true)
public class FriendRequest {
    @Id
    private String id;

    @DocumentReference
    private User from;

    @DocumentReference
    private User to;

    private String message;

    @Builder.Default
    private RequestStatus status = RequestStatus.Pending;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
