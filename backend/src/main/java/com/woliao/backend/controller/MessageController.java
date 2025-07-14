package com.woliao.backend.controller;

import com.woliao.backend.entity.ChatMessageEntity;
import com.woliao.backend.repository.ChatMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {
    @Autowired
    private ChatMessageRepository chatMessageRepository;

    // 获取单聊历史消息
    @GetMapping("/single")
    public List<ChatMessageEntity> getSingleChatHistory(@RequestParam Long userId, @RequestParam Long peerId) {
        // 查询双方的单聊消息
        return chatMessageRepository.findSingleChatHistory(userId, peerId);
    }

    // 获取群聊历史消息
    @GetMapping("/group")
    public List<ChatMessageEntity> getGroupChatHistory(@RequestParam Long groupId) {
        return chatMessageRepository.findGroupChatHistory(groupId);
    }

    // 置顶/取消置顶消息（简单实现：仅返回成功，实际可扩展消息表加置顶字段）
    @PostMapping("/pin")
    public String pinMessage(@RequestParam Long messageId, @RequestParam boolean pin) {
        // TODO: 实际应更新消息表的置顶字段
        return "ok";
    }
} 