package com.migo.backend.entity;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "messages")
@CompoundIndexes({
        @CompoundIndex(name = "conv_created_idx", def = "{'conversationId': 1, 'createdAt': -1}")
})
public class Message {
    @Id
    private String id;

    @DocumentReference
    @Field("senderId")
    private User senderId;

    @Indexed
    @DocumentReference
    @Field("conversationId")
    private Conversation conversationId;

    @Field("content")
    private String content;

    @Field("imgUrl")
    private String imgUrl;

    @Builder.Default
    @Field("readBy")
    private Set<String> readBy = new HashSet<>(

    );
    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;
}
