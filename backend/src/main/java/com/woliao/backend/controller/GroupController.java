package com.woliao.backend.controller;

import com.woliao.backend.entity.Group;
import com.woliao.backend.entity.GroupMember;
import com.woliao.backend.repository.GroupRepository;
import com.woliao.backend.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.woliao.backend.entity.User;
import org.springframework.http.ResponseEntity;
import java.util.Set;

@RestController
@RequestMapping("/api/group")
public class GroupController {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;

    // 创建群聊（支持指定成员，自动加入创建者）
    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@AuthenticationPrincipal User currentUser, @RequestParam String name, @RequestBody(required = false) Set<Long> memberIds) {
        if (currentUser == null) return ResponseEntity.status(401).body("未登录");
        Group group = new Group();
        group.setName(name);
        group.setCreateTime(LocalDateTime.now());
        group = groupRepository.save(group);
        // 创建者自动加入
        GroupMember creator = new GroupMember();
        creator.setGroupId(group.getId());
        creator.setUserId(currentUser.getId());
        creator.setJoinTime(LocalDateTime.now());
        groupMemberRepository.save(creator);
        // 其他成员
        if (memberIds != null) {
            for (Long uid : memberIds) {
                if (!uid.equals(currentUser.getId()) && !groupMemberRepository.existsByGroupIdAndUserId(group.getId(), uid)) {
                    GroupMember member = new GroupMember();
                    member.setGroupId(group.getId());
                    member.setUserId(uid);
                    member.setJoinTime(LocalDateTime.now());
                    groupMemberRepository.save(member);
                }
            }
        }
        return ResponseEntity.ok(group);
    }

    // 加入群聊
    @PostMapping("/join")
    public GroupMember joinGroup(@RequestParam Long groupId, @RequestParam Long userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setJoinTime(LocalDateTime.now());
            return groupMemberRepository.save(member);
        }
        return null;
    }

    // 退出群聊（自动获取当前用户）
    @PostMapping("/leave")
    public ResponseEntity<?> leaveGroup(@AuthenticationPrincipal User currentUser, @RequestParam Long groupId) {
        if (currentUser == null) return ResponseEntity.status(401).body("未登录");
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.stream()
            .filter(m -> m.getUserId().equals(currentUser.getId()))
            .findFirst()
            .ifPresent(groupMemberRepository::delete);
        return ResponseEntity.ok().build();
    }

    // 查询群成员
    @GetMapping("/members")
    public List<GroupMember> getGroupMembers(@RequestParam Long groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }
} 