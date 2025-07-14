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
} 