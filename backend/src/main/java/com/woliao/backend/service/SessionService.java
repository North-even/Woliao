package com.woliao.backend.service;

import com.woliao.backend.dto.SessionDTO;
import com.woliao.backend.entity.User;
import com.woliao.backend.repository.UserRepository;
import com.woliao.backend.repository.GroupRepository;
import com.woliao.backend.repository.GroupMemberRepository;
import com.woliao.backend.entity.Group;
import com.woliao.backend.entity.GroupMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SessionService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private GroupMemberRepository groupMemberRepository;

    public List<SessionDTO> getSessionsForUser(Long userId) {
        // 单聊
        String sql = "WITH LatestMessages AS (" +
                     "    SELECT *, ROW_NUMBER() OVER(PARTITION BY partner_id ORDER BY created_at DESC) as rn " +
                     "    FROM (" +
                     "        SELECT id, sender_id, receiver_id, content, created_at, " +
                     "               IF(sender_id = ?, receiver_id, sender_id) as partner_id " +
                     "        FROM messages " +
                     "        WHERE (sender_id = ? OR receiver_id = ?) AND receiver_type = 'SINGLE'" +
                     "    ) AS user_messages" +
                     ") " +
                     "SELECT lm.partner_id, lm.content, lm.created_at " +
                     "FROM LatestMessages lm " +
                     "WHERE lm.rn = 1 " +
                     "ORDER BY lm.created_at DESC";

        List<SessionDTO> sessions = jdbcTemplate.query(sql, new Object[]{userId, userId, userId}, (rs, rowNum) -> {
            SessionDTO dto = new SessionDTO();
            dto.setPartnerId(rs.getLong("partner_id"));
            dto.setLastMessage(rs.getString("content"));
            dto.setTimestamp(rs.getTimestamp("created_at").toLocalDateTime());
            dto.setChatType("SINGLE");
            dto.setUnreadCount(0); 
            dto.setPinned(false);
            return dto;
        });

        // 群聊
        List<GroupMember> myGroups = groupMemberRepository.findByUserId(userId);
        List<Long> groupIds = myGroups.stream().map(GroupMember::getGroupId).toList();
        if (!groupIds.isEmpty()) {
            String groupSql = "WITH LatestGroupMessages AS (" +
                    " SELECT *, ROW_NUMBER() OVER(PARTITION BY receiver_id ORDER BY created_at DESC) as rn " +
                    " FROM messages WHERE receiver_type = 'GROUP' AND receiver_id IN (" +
                    groupIds.stream().map(id -> "? ").collect(Collectors.joining(", ")) + ")" +
                    ") " +
                    "SELECT receiver_id, content, created_at FROM LatestGroupMessages WHERE rn = 1 ORDER BY created_at DESC";
            Object[] groupIdParams = groupIds.toArray();
            List<SessionDTO> groupSessions = jdbcTemplate.query(groupSql, groupIdParams, (rs, rowNum) -> {
                SessionDTO dto = new SessionDTO();
                dto.setPartnerId(rs.getLong("receiver_id"));
                dto.setLastMessage(rs.getString("content"));
                dto.setTimestamp(rs.getTimestamp("created_at").toLocalDateTime());
                dto.setChatType("GROUP");
                dto.setUnreadCount(0);
                dto.setPinned(false);
                return dto;
            });
            // 批量查群聊信息
            List<Group> groups = groupRepository.findAllById(groupIds);
            Map<Long, Group> groupMap = groups.stream().collect(Collectors.toMap(Group::getId, Function.identity()));
            groupSessions.forEach(session -> {
                Group group = groupMap.get(session.getPartnerId());
                if (group != null) {
                    session.setPartnerName(group.getName());
                    session.setPartnerAvatar(null); // 可扩展为群头像
                }
            });
            sessions.addAll(groupSessions);
        }

        // 批量查询用户信息以填充名称和头像
        List<Long> partnerIds = sessions.stream().filter(s -> "SINGLE".equals(s.getChatType())).map(SessionDTO::getPartnerId).collect(Collectors.toList());
        if (!partnerIds.isEmpty()) {
            List<User> partners = userRepository.findAllById(partnerIds);
            Map<Long, User> partnerMap = partners.stream().collect(Collectors.toMap(User::getId, Function.identity()));
            sessions.forEach(session -> {
                if ("SINGLE".equals(session.getChatType())) {
                    User partner = partnerMap.get(session.getPartnerId());
                    if (partner != null) {
                        session.setPartnerName(partner.getNickname());
                        session.setPartnerAvatar(partner.getAvatarUrl());
                    }
                }
            });
        }
        return sessions;
    }
} 