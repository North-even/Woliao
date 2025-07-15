package com.woliao.backend.repository;

import com.woliao.backend.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    // 单聊历史：查询双方的单聊消息
    @Query("SELECT m FROM ChatMessageEntity m WHERE m.type = 'SINGLE_CHAT' AND ((m.fromUserId = :userId AND m.toUserId = :peerId) OR (m.fromUserId = :peerId AND m.toUserId = :userId)) ORDER BY m.timestamp ASC")
    List<ChatMessageEntity> findSingleChatHistory(@Param("userId") Long userId, @Param("peerId") Long peerId);

    // 群聊历史
    @Query("SELECT m FROM ChatMessageEntity m WHERE m.type = 'GROUP_CHAT' AND m.toUserId = :groupId ORDER BY m.timestamp ASC")
    List<ChatMessageEntity> findGroupChatHistory(@Param("groupId") Long groupId);

    // 查询未读消息
    @Query("SELECT m FROM ChatMessageEntity m WHERE m.toUserId = :userId AND m.isRead = false ORDER BY m.timestamp ASC")
    List<ChatMessageEntity> findUnreadMessages(@Param("userId") Long userId);

    // 批量标记为已读
    @Modifying
    @Query("UPDATE ChatMessageEntity m SET m.isRead = true WHERE m.id IN :ids")
    void markMessagesAsRead(@Param("ids") List<Long> ids);
} 