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
public class LastMessageResponse {
    private String id;
    private String content;
    private String senderId;
    private Instant createdAt;
}