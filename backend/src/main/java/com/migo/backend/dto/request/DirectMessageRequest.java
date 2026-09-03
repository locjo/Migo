package com.migo.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DirectMessageRequest {
    @NotBlank(message = "recipientId không được để trống")
    private String recipientId;

    private String conversationId; // Optional: Có thể null
    private String content;        // Optional
    private String imgUrl;         // Optional
}
