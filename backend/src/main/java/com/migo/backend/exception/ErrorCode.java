package com.migo.backend.exception;

public enum ErrorCode {
    // --- System & Auth Errors ---
    UNCATEGORIZED_EXCEPTION(9999, "Lỗi hệ thống chưa xác định"),
    INVALID_KEY(1001, "Khoá không hợp lệ"),
    USER_NOT_EXISTED(1002, "Người dùng không tồn tại"),
    UNAUTHENTICATED(1003, "Chưa xác thực người dùng"),
    UNAUTHORIZED_ACTION(1004, "Bạn không có quyền thực hiện thao tác này"),
    INVALID_CREDENTIAL(1005, "Tên đăng nhập hoặc mật khẩu không chính xác"),
    USERNAME_EXISTED(1006, "Tên đăng nhập đã tồn tại"),
    EMAIL_EXISTED(1007, "Email đã tồn tại"),
    

    // --- Friend Feature Errors ---
    CANNOT_ADD_FRIEND_SELF(2001, "Bạn không thể gửi lời mời kết bạn cho chính mình"),
    FRIEND_REQUEST_ALREADY_EXISTS(2002, "Lời mời kết bạn hoặc mối quan hệ giữa hai người đã tồn tại"),
    REQUEST_NOT_FOUND(2003, "Không tìm thấy lời mời kết bạn này"),
    FRIENDSHIP_NOT_FOUND(2004, "Hai người chưa phải là bạn bè của nhau"),
    

    CONVERSATION_NOT_FOUND(3001, "Cuộc trò chuyện không tồn tại"),
    INVALID_CONVERSATION_TYPE(3002, "Loại cuộc trò chuyện không hợp lệ cho hành động này"),
    NOT_IN_CONVERSATION(3003, "Bạn không phải thành viên của cuộc trò chuyện này"),
    USER_ALREADY_IN_GROUP(3004, "Người dùng này đã có trong nhóm chat");
    

    private int code;
    private String message;
    
    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    public int getCode() {
        return code;
    }
    public String getMessage() {
        return message;
    }

    
}
