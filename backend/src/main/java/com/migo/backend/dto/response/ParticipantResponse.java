package com.migo.backend.dto.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantResponse {

    private String userId;
    
    private String role;

    private String displayName;

    private String username;

    private String avatarUrl;
    
    private Instant joinedAt;
}