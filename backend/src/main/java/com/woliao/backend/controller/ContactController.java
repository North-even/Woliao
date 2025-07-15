package com.woliao.backend.controller;

import com.woliao.backend.dto.ContactDTO;
import com.woliao.backend.dto.GroupInfoDTO;
import com.woliao.backend.entity.User;
import com.woliao.backend.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllContacts(@AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        List<ContactDTO> friends = contactService.getFriends(currentUser.getId());
        List<GroupInfoDTO> groups = contactService.getGroups(currentUser.getId());

        return ResponseEntity.ok(Map.of("friends", friends, "groups", groups));
    }

    @PostMapping("/add-friend")
    public ResponseEntity<?> addFriend(@AuthenticationPrincipal User currentUser, @RequestParam String phoneNumber) {
        if (currentUser == null) return ResponseEntity.status(401).body("未登录");
        // 通过手机号查找好友ID
        Long friendId = contactService.getUserIdByPhone(phoneNumber);
        if (friendId == null) return ResponseEntity.badRequest().body("用户不存在");
        boolean ok = contactService.addFriend(currentUser.getId(), friendId);
        if (ok) return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().body("添加失败，可能已是好友");
    }

    @PostMapping("/remove-friend")
    public ResponseEntity<?> removeFriend(@AuthenticationPrincipal User currentUser, @RequestParam Long friendId) {
        if (currentUser == null) return ResponseEntity.status(401).body("未登录");
        boolean ok = contactService.removeFriend(currentUser.getId(), friendId);
        if (ok) return ResponseEntity.ok().build();
        return ResponseEntity.badRequest().body("删除失败，可能不是好友");
    }
} 