package com.woliao.backend.controller;

import com.woliao.backend.entity.Message;
import com.woliao.backend.entity.User;
import com.woliao.backend.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping("/history/user/{partnerId}")
    public ResponseEntity<List<Message>> getSingleChatHistory(
        @AuthenticationPrincipal User currentUser,
        @PathVariable Long partnerId
    ) {
        List<Message> history = messageService.getSingleChatHistory(currentUser.getId(), partnerId);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/history/group/{groupId}")
    public ResponseEntity<List<Message>> getGroupChatHistory(
        @AuthenticationPrincipal User currentUser, // 确保用户已登录
        @PathVariable Long groupId
    ) {
        // TODO: 在未来的版本中，这里应该增加一步验证，确保 currentUser 是 groupId 这个群的成员

        List<Message> history = messageService.getGroupChatHistory(groupId);
        return ResponseEntity.ok(history);
    }
} 