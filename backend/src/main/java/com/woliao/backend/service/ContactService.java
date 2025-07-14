package com.woliao.backend.service;

import com.woliao.backend.dto.ContactDTO;
import com.woliao.backend.dto.GroupInfoDTO;
import com.woliao.backend.entity.Group;
import com.woliao.backend.entity.GroupMember;
import com.woliao.backend.entity.User;
import com.woliao.backend.repository.GroupMemberRepository;
import com.woliao.backend.repository.GroupRepository;
import com.woliao.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ContactService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 临时：返回所有用户（除自己外）作为“好友”
    public List<ContactDTO> getFriends(Long userId) {
        final String onlineUsersKey = "online_users";
        Set<String> onlineUserIds = stringRedisTemplate.opsForSet().members(onlineUsersKey);
        return userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(userId))
                .map(user -> {
                    ContactDTO dto = new ContactDTO();
                    dto.setId(user.getId());
                    dto.setNickname(user.getNickname());
                    dto.setAvatarUrl(user.getAvatarUrl());
                    dto.setOnline(onlineUserIds != null && onlineUserIds.contains(user.getId().toString()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // 查询所有群聊
    public List<GroupInfoDTO> getGroups(Long userId) {
        // 先查出所有 groupId
        List<GroupMember> memberships = groupMemberRepository.findByUserId(userId);
        Set<Long> groupIds = memberships.stream()
                .map(GroupMember::getGroupId)
                .collect(Collectors.toSet());
        // 再查群信息
        List<Group> groups = groupRepository.findAllById(groupIds);
        return groups.stream().map(group -> {
            GroupInfoDTO dto = new GroupInfoDTO();
            dto.setId(group.getId());
            dto.setGroupName(group.getName());
            dto.setAvatarUrl(null); // 你的 Group 没有 avatar 字段
            return dto;
        }).collect(Collectors.toList());
    }
}