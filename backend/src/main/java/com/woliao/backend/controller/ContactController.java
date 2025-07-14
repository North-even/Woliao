package com.woliao.backend.controller;

import com.woliao.backend.dto.ContactDTO;
import com.woliao.backend.dto.GroupInfoDTO;
import com.woliao.backend.entity.User;
import com.woliao.backend.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
} 