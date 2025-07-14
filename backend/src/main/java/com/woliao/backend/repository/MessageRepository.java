package com.woliao.backend.repository;

import com.woliao.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // 可根据需要添加自定义查询方法
    @Query("SELECT m FROM Message m WHERE m.receiverType = 'SINGLE' AND ((m.senderId = :userId1 AND m.receiverId = :userId2) OR (m.senderId = :userId2 AND m.receiverId = :userId1)) ORDER BY m.createdAt ASC")
    List<Message> findChatHistory(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    // 查询特定群聊的所有消息，并按时间升序排列
    List<Message> findByReceiverIdAndReceiverTypeOrderByCreatedAtAsc(Long receiverId, Message.ReceiverTypeEnum receiverType);
} 