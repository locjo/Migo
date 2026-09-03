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
public class GroupMessageRequest {
    @NotBlank(message = "conversationId không được để trống")
    private String conversationId;

    private String content; // Optional
    private String imgUrl;
}
