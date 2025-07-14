package com.woliao.backend.handler; // 请确保这个包名和您项目的实际路径一致

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woliao.backend.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication; // 导入Authentication类
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.security.Principal; // 导入Principal类
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, Long> lastHeartbeat = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 从WebSocketSession中安全地提取User对象
     */
    private User getUserFromSession(WebSocketSession session) {
        Principal principal = session.getPrincipal();
        if (principal instanceof Authentication) {
            Object userPrincipal = ((Authentication) principal).getPrincipal();
            if (userPrincipal instanceof User) {
                return (User) userPrincipal;
            }
        }
        return null;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 【核心修改】使用新的辅助方法来正确获取User对象
        User user = getUserFromSession(session);

        if (user == null) {
            logger.warn("WebSocket connection attempt without a valid User principal. Closing session.");
            session.close(CloseStatus.POLICY_VIOLATION.withReason("User principal not found"));
            return;
        }

        Long userId = user.getId();
        sessions.put(userId, session);
        lastHeartbeat.put(userId, System.currentTimeMillis());
        logger.info("WebSocket connection established for user: {}. Total sessions: {}", userId, sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        User user = getUserFromSession(session);
        if (user == null) return; // 如果无法获取用户，则忽略消息
        Long userId = user.getId();

        try {
            Map<String, String> payload = objectMapper.readValue(message.getPayload(), Map.class);
            String type = payload.get("type");

            if ("ping".equals(type)) {
                lastHeartbeat.put(userId, System.currentTimeMillis());
                session.sendMessage(new TextMessage("{\"type\": \"pong\"}"));
            } else {
                logger.info("Received message from user {}: {}", userId, message.getPayload());
                // 在这里处理未来的聊天消息逻辑
            }
        } catch (IOException e) {
            logger.error("Error processing message from user {}: {}", userId, message.getPayload(), e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        User user = getUserFromSession(session);
        if (user == null) {
            logger.warn("A session was closed but user principal could not be identified. Status: {}", status);
            return;
        }

        Long userId = user.getId();
        sessions.remove(userId);
        lastHeartbeat.remove(userId);
        logger.info("WebSocket connection closed for user: {}. Reason: {}. Total sessions: {}", userId, status, sessions.size());
    }

    // 公开给 HeartbeatChecker 使用的方法
    public Map<Long, WebSocketSession> getSessions() {
        return sessions;
    }

    public Map<Long, Long> getLastHeartbeatMap() {
        return lastHeartbeat;
    }
}