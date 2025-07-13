package com.woliao.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sessions")
@CrossOrigin(origins = "*")
public class SessionController {
    
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getSessions() {
        List<Map<String, Object>> sessions = Arrays.asList(
            Map.of(
                "id", 1L,
                "name", "张三",
                "lastMessage", "你好，最近怎么样？",
                "timestamp", LocalDateTime.now().minusMinutes(5),
                "isPinned", true,
                "unreadCount", 3
            ),
            Map.of(
                "id", 2L,
                "name", "李四",
                "lastMessage", "明天一起吃饭吧",
                "timestamp", LocalDateTime.now().minusHours(2),
                "isPinned", true,
                "unreadCount", 0
            ),
            Map.of(
                "id", 3L,
                "name", "王五",
                "lastMessage", "项目进展如何？",
                "timestamp", LocalDateTime.now().minusDays(1),
                "isPinned", false,
                "unreadCount", 1
            ),
            Map.of(
                "id", 4L,
                "name", "赵六",
                "lastMessage", "周末有空吗？",
                "timestamp", LocalDateTime.now().minusDays(2),
                "isPinned", false,
                "unreadCount", 0
            ),
            Map.of(
                "id", 5L,
                "name", "钱七",
                "lastMessage", "谢谢你的帮助",
                "timestamp", LocalDateTime.now().minusDays(3),
                "isPinned", false,
                "unreadCount", 0
            )
        );
        
        return ResponseEntity.ok(sessions);
    }
} 