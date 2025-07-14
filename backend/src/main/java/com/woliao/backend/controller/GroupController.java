package com.woliao.backend.controller;

import com.woliao.backend.entity.Group;
import com.woliao.backend.entity.GroupMember;
import com.woliao.backend.repository.GroupRepository;
import com.woliao.backend.repository.GroupMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/group")
public class GroupController {
    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;

    // 创建群聊
    @PostMapping("/create")
    public Group createGroup(@RequestParam String name) {
        Group group = new Group();
        group.setName(name);
        group.setCreateTime(LocalDateTime.now());
        return groupRepository.save(group);
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

    // 退出群聊
    @PostMapping("/leave")
    public void leaveGroup(@RequestParam Long groupId, @RequestParam Long userId) {
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        members.stream()
            .filter(m -> m.getUserId().equals(userId))
            .findFirst()
            .ifPresent(groupMemberRepository::delete);
    }

    // 查询群成员
    @GetMapping("/members")
    public List<GroupMember> getGroupMembers(@RequestParam Long groupId) {
        return groupMemberRepository.findByGroupId(groupId);
    }
} 