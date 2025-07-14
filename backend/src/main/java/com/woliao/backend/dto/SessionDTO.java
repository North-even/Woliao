package com.woliao.backend.dto;

import java.time.LocalDateTime;

public class SessionDTO {
    private Long partnerId;         // 对话伙伴的ID (用户ID或群组ID)
    private String partnerName;       // 伙伴的名称
    private String partnerAvatar;     // 伙伴的头像
    private String lastMessage;       // 最后一条消息的内容
    private LocalDateTime timestamp;  // 最后一条消息的时间
    private int unreadCount;        // 未读消息数
    private boolean isPinned;         // 是否置顶 (暂时硬编码为false)
    private String chatType;          // "SINGLE" 或 "GROUP"

    public Long getPartnerId() { return partnerId; }
    public void setPartnerId(Long partnerId) { this.partnerId = partnerId; }
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String partnerName) { this.partnerName = partnerName; }
    public String getPartnerAvatar() { return partnerAvatar; }
    public void setPartnerAvatar(String partnerAvatar) { this.partnerAvatar = partnerAvatar; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    public boolean isPinned() { return isPinned; }
    public void setPinned(boolean pinned) { isPinned = pinned; }
    public String getChatType() { return chatType; }
    public void setChatType(String chatType) { this.chatType = chatType; }
} 