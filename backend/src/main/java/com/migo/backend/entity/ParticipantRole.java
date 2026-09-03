package com.migo.backend.entity;

public enum ParticipantRole {
    Owner,   // Trưởng nhóm (Có quyền xóa nhóm, giải tán nhóm)
    Admin,   // Phó nhóm (Có quyền duyệt thành viên, đổi tên nhóm)
    Member   // Thành viên bình thường
}