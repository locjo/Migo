package com.migo.backend.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupRequest {
    @NotBlank(message = "Tên nhóm không được để trống")
    private String name;
    private String avatar;
    private List<String> memberIds; // Danh sách ID những người bạn được thêm vào nhóm
}