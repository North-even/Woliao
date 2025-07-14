package com.woliao.backend.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.woliao.backend.dto.ChatMessage; // 导入我们刚创建的DTO
import com.woliao.backend.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.core.Authentication;
import com.woliao.backend.repository.ChatMessageRepository;
import com.woliao.backend.entity.ChatMessageEntity;
import java.time.LocalDateTime;
import com.woliao.backend.service.MessageService; // 导入新服务

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<Long, Long> lastHeartbeat = new ConcurrentHashMap<>();

    // 使用@Autowired自动注入ObjectMapper，这是Spring推荐的做法
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // VVVV 在这里添加新的注入 VVVV
    @Autowired
    private MessageService messageService;
    // ^^^^ 在这里添加新的注入 ^^^^

    // 【新增】在这里处理未来的聊天消息逻辑，不再是注释了
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        User sender = getUserFromSession(session);
        if (sender == null) return;
        Long senderId = sender.getId();

        try {
            // 使用我们创建的ChatMessage DTO来解析消息
            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            String type = chatMessage.getType();

            if ("ping".equals(type)) {
                lastHeartbeat.put(senderId, System.currentTimeMillis());
                session.sendMessage(new TextMessage("{\"type\": \"pong\"}"));
                return; // 处理完心跳后直接返回
            }

            logger.info("Received {} from user {}: {}", type, senderId, message.getPayload());

            // 【核心逻辑】根据消息类型进行分发处理
            switch (type) {
                case "USER_CHAT": // 从 SINGLE_CHAT 修改而来
                    handleSingleChatMessage(senderId, chatMessage);
                    break;
                case "GROUP_CHAT":
                    handleGroupChatMessage(senderId, chatMessage);
                    break;
                default:
                    logger.warn("Unknown message type received: {}", type);
                    break;
            }

        } catch (IOException e) {
            logger.error("Error processing message from user {}: {}", senderId, message.getPayload(), e);
        }
    }

    private void handleSingleChatMessage(Long senderId, ChatMessage chatMessage) throws IOException {
        Long recipientId = chatMessage.getToUserId();
        WebSocketSession recipientSession = sessions.get(recipientId);
        String messageToSend = objectMapper.writeValueAsString(chatMessage);

        if (recipientSession != null && recipientSession.isOpen()) {
            recipientSession.sendMessage(new TextMessage(messageToSend));
            logger.info("Message from {} to {} forwarded.", senderId, recipientId);
        } else {
            logger.info("User {} is offline.", recipientId);
        }

        // VVVV 在这里添加回显逻辑 VVVV
        // 将消息也发回给发送方，以实现UI同步
        WebSocketSession senderSession = sessions.get(senderId);
        if (senderSession != null && senderSession.isOpen()) {
            senderSession.sendMessage(new TextMessage(messageToSend));
        }
        // ^^^^ 在这里添加回显逻辑 ^^^^

        // 保存消息到数据库
        messageService.saveMessage(senderId, recipientId, "SINGLE", chatMessage.getContent());
    }

    private void handleGroupChatMessage(Long senderId, ChatMessage chatMessage) throws IOException {
        Long groupId = chatMessage.getToUserId();
        String messageToSend = objectMapper.writeValueAsString(chatMessage);
        sessions.forEach((userId, session) -> {
            if (!userId.equals(senderId)) {
                try {
                    if (session.isOpen()) {
                        session.sendMessage(new TextMessage(messageToSend));
                    }
                } catch (IOException e) {
                    logger.error("Failed to send group message to user {}", userId, e);
                }
            }
        });
        logger.info("Group message from user {} to group {} broadcasted.", senderId, groupId);
        // 消息持久化
        ChatMessageEntity entity = new ChatMessageEntity();
        entity.setType("GROUP_CHAT");
        entity.setFromUserId(senderId);
        entity.setToUserId(groupId);
        entity.setContent(chatMessage.getContent());
        entity.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(entity);
        // 保存群聊消息到数据库
        messageService.saveMessage(senderId, groupId, "GROUP", chatMessage.getContent());
    }


    // =================================================================
    // 以下是生命周期管理和辅助方法，与我们之前修复的版本保持一致
    // =================================================================

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

    public Map<Long, WebSocketSession> getSessions() {
        return sessions;
    }

    public Map<Long, Long> getLastHeartbeatMap() {
        return lastHeartbeat;
    }
}