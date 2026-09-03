package com.migo.backend.socket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    public static final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();
    public static final Set<String> onlineUserIds = ConcurrentHashMap.newKeySet();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Ưu tiên đọc từ Principal, nếu không có thì đọc từ SessionAttributes
        Principal principal = headerAccessor.getUser();
        String userId = (principal != null) ? principal.getName() : null;

        if (userId == null && headerAccessor.getSessionAttributes() != null) {
            userId = (String) headerAccessor.getSessionAttributes().get("userId");
        }

        if (sessionId != null && userId != null) {
            sessionUserMap.put(sessionId, userId);
            onlineUserIds.add(userId);

            messagingTemplate.convertAndSend("/topic/online-users", onlineUserIds);
            log.info("🟢 User [{}] ONLINE. Danh sách hiện tại: {}", userId, onlineUserIds);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        if (sessionId != null) {
            String userId = sessionUserMap.remove(sessionId);

            if (userId != null) {
                // Nếu user không còn mở tab nào khác thì xóa khỏi danh sách online
                if (!sessionUserMap.containsValue(userId)) {
                    onlineUserIds.remove(userId);
                }
                messagingTemplate.convertAndSend("/topic/online-users", onlineUserIds);
                log.info("🔴 User [{}] OFFLINE. Danh sách hiện tại: {}", userId, onlineUserIds);
            }
        }
    }

    @EventListener
    public void handleSessionSubscribeListener(SessionSubscribeEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = headerAccessor.getDestination();

        if ("/topic/online-users".equals(destination)) {
            // Đảm bảo subscriber mới nhận ngay danh sách online hiện tại
            messagingTemplate.convertAndSend("/topic/online-users", onlineUserIds);
        }
    }
}