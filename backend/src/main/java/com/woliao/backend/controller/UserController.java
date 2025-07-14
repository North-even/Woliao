package com.woliao.backend.controller;

import com.woliao.backend.entity.User;
import com.woliao.backend.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 更改昵称
    @PostMapping("/update-nickname")
    public ResponseEntity<?> updateNickname(@RequestBody UpdateNicknameRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phone = auth.getName();
        User user = userRepository.findByPhoneNumber(phone).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("用户不存在");
        user.setNickname(req.getNickname());
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    // 更改密码
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String phone = auth.getName();
        User user = userRepository.findByPhoneNumber(phone).orElse(null);
        if (user == null) return ResponseEntity.status(401).body("用户不存在");
        if (!passwordEncoder.matches(req.getOldPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/online")
    public Set<String> getOnlineUsers() {
        return stringRedisTemplate.opsForSet().members("online_users");
    }

    // DTOs
    public static class UpdateNicknameRequest {
        @NotBlank
        private String nickname;
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
    }
    public static class ChangePasswordRequest {
        @NotBlank
        private String oldPassword;
        @NotBlank
        private String newPassword;
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
} 