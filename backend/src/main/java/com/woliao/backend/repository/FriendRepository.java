package com.woliao.backend.repository;

import com.woliao.backend.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    List<Friend> findByUserId(Long userId);
    Optional<Friend> findByUserIdAndFriendId(Long userId, Long friendId);
    void deleteByUserIdAndFriendId(Long userId, Long friendId);
} 