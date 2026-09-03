package com.migo.backend.entity;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.DocumentReference;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class LastMessageInfo {
    private String content;

    private Instant createdAt;

    @DocumentReference
    private User senderId;
}
