package com.woliao.backend.service;

import com.woliao.backend.dto.ContactDTO;
import com.woliao.backend.dto.GroupInfoDTO;
import com.woliao.backend.entity.Group;
import com.woliao.backend.entity.GroupMember;
import com.woliao.backend.entity.Friend;
import com.woliao.backend.entity.User;
import com.woliao.backend.repository.GroupMemberRepository;
import com.woliao.backend.repository.GroupRepository;
import com.woliao.backend.repository.UserRepository;
import com.woliao.backend.repository.FriendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private FriendRepository friendRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 获取真正的好友列表
    public List<ContactDTO> getFriends(Long userId) {
        final String onlineUsersKey = "online_users";
        Set<String> onlineUserIds = stringRedisTemplate.opsForSet().members(onlineUsersKey);
        List<Friend> friends = friendRepository.findByUserId(userId);
        return friends.stream().map(friend -> {
            User user = userRepository.findById(friend.getFriendId()).orElse(null);
            if (user == null) return null;
            ContactDTO dto = new ContactDTO();
            dto.setId(user.getId());
            dto.setNickname(user.getNickname());
            dto.setAvatarUrl(user.getAvatarUrl());
            dto.setOnline(onlineUserIds != null && onlineUserIds.contains(user.getId().toString()));
            return dto;
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }

    // 添加好友（双向）
    @Transactional
    public boolean addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) return false;
        if (friendRepository.findByUserIdAndFriendId(userId, friendId).isPresent()) return false;
        if (!userRepository.existsById(friendId)) return false;
        friendRepository.save(new Friend(userId, friendId));
        friendRepository.save(new Friend(friendId, userId));
        return true;
    }

    // 删除好友（双向）
    @Transactional
    public boolean removeFriend(Long userId, Long friendId) {
        if (!friendRepository.findByUserIdAndFriendId(userId, friendId).isPresent()) return false;
        friendRepository.deleteByUserIdAndFriendId(userId, friendId);
        friendRepository.deleteByUserIdAndFriendId(friendId, userId);
        return true;
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

    public Long getUserIdByPhone(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
            .map(User::getId).orElse(null);
    }
}