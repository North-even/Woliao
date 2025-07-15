package com.woliao.backend.service;

import com.woliao.backend.entity.Message;
import com.woliao.backend.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import com.woliao.backend.entity.ChatMessageEntity;
import com.woliao.backend.repository.ChatMessageRepository;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Transactional
    public Message saveMessage(Long senderId, Long receiverId, String receiverType, String content) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        // 将字符串转换为枚举类型
        message.setReceiverType(Message.ReceiverTypeEnum.valueOf(receiverType.toUpperCase()));
        message.setContentType(Message.ContentTypeEnum.TEXT); // 目前我们先硬编码为TEXT类型
        message.setContent(content);
        message.setCreatedAt(LocalDateTime.now()); // 设置创建时间

        return messageRepository.save(message);
    }

    public List<Message> getSingleChatHistory(Long userId1, Long userId2) {
        return messageRepository.findChatHistory(userId1, userId2);
    }

    public List<Message> getGroupChatHistory(Long groupId) {
        return messageRepository.findByReceiverIdAndReceiverTypeOrderByCreatedAtAsc(groupId, Message.ReceiverTypeEnum.GROUP);
    }

    // 查询未读消息
    public List<ChatMessageEntity> getUnreadMessages(Long userId) {
        return chatMessageRepository.findUnreadMessages(userId);
    }

    // 批量标记为已读
    @Transactional
    public void markMessagesAsRead(List<Long> ids) {
        if (ids != null && !ids.isEmpty()) {
            chatMessageRepository.markMessagesAsRead(ids);
        }
    }
} 