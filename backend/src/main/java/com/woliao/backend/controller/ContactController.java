package com.woliao.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
@CrossOrigin(origins = "*")
public class ContactController {
    
    @GetMapping
    public ResponseEntity<Map<String, List<Map<String, Object>>>> getContacts() {
        List<Map<String, Object>> groups = Arrays.asList(
            Map.of("id", 1L, "name", "技术交流群"),
            Map.of("id", 2L, "name", "项目讨论组"),
            Map.of("id", 3L, "name", "学习小组"),
            Map.of("id", 4L, "name", "朋友聚会群")
        );
        
        List<Map<String, Object>> individuals = Arrays.asList(
            Map.of("id", 1L, "name", "张三", "avatar", "https://via.placeholder.com/40"),
            Map.of("id", 2L, "name", "李四", "avatar", "https://via.placeholder.com/40"),
            Map.of("id", 3L, "name", "王五", "avatar", "https://via.placeholder.com/40"),
            Map.of("id", 4L, "name", "赵六", "avatar", "https://via.placeholder.com/40"),
            Map.of("id", 5L, "name", "钱七", "avatar", "https://via.placeholder.com/40"),
            Map.of("id", 6L, "name", "孙八", "avatar", "https://via.placeholder.com/40"),
            Map.of("id", 7L, "name", "周九", "avatar", "https://via.placeholder.com/40"),
            Map.of("id", 8L, "name", "吴十", "avatar", "https://via.placeholder.com/40")
        );
        
        Map<String, List<Map<String, Object>>> response = Map.of(
            "groups", groups,
            "individuals", individuals
        );
        
        return ResponseEntity.ok(response);
    }
} 