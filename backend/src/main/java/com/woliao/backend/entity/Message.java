package com.woliao.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    public enum ReceiverTypeEnum {
        SINGLE, GROUP
    }

    public enum ContentTypeEnum {
        TEXT, IMAGE, FILE, SYSTEM
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private Long senderId;

    @Column(name = "receiver_id", nullable = false)
    private Long receiverId;

    @Enumerated(EnumType.STRING)
    @Column(name = "receiver_type", nullable = false)
    private ReceiverTypeEnum receiverType;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false)
    private ContentTypeEnum contentType;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public Long getReceiverId() { return receiverId; }
    public void setReceiverId(Long receiverId) { this.receiverId = receiverId; }
    public ReceiverTypeEnum getReceiverType() { return receiverType; }
    public void setReceiverType(ReceiverTypeEnum receiverType) { this.receiverType = receiverType; }
    public ContentTypeEnum getContentType() { return contentType; }
    public void setContentType(ContentTypeEnum contentType) { this.contentType = contentType; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 