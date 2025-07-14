package com.woliao.backend.dto;

// 这个类用于接收和解析来自WebSocket客户端的聊天消息
public class ChatMessage {
    private String type;       // 消息类型, e.g., "SINGLE_CHAT" or "GROUP_CHAT"
    private Long toUserId;     // 接收方ID (单聊时是用户ID，群聊时是群组ID)
    private String content;    // 消息内容

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
} 