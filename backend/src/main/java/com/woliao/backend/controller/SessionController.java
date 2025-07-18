package com.woliao.backend.controller;

import com.woliao.backend.dto.SessionDTO;
import com.woliao.backend.entity.User;
import com.woliao.backend.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @GetMapping
    public ResponseEntity<List<SessionDTO>> getSessions(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        List<SessionDTO> sessions = sessionService.getSessionsForUser(currentUser.getId());
        return ResponseEntity.ok(sessions);
    }

    // 新增：标记会话为已读
    @PostMapping("/{chatType}/{partnerId}/read")
    public ResponseEntity<Void> markAsRead(
        @AuthenticationPrincipal User currentUser,
        @PathVariable String chatType,
        @PathVariable Long partnerId
    ) {
        sessionService.markAsRead(currentUser.getId(), partnerId, chatType.toUpperCase());
        return ResponseEntity.ok().build();
    }
} 