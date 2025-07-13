package com.woliao.backend.controller;

import com.woliao.backend.entity.User;
import com.woliao.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 这个接口可以一键修复所有测试用户的密码
    @GetMapping("/fix-all-passwords")
    @Transactional
    public String fixAllUserPasswords() {
        // 从README.md中我们知道所有测试用户的密码都是'123456'
        String commonPassword = "123456";
        List<String> testUserPhoneNumbers = Arrays.asList(
                "13800138000",
                "13800138001",
                "13800138002",
                "13800138003",
                "13800138004"
        );

        StringBuilder result = new StringBuilder("开始修复所有测试用户密码...<br/>");
        String newHashedPassword = passwordEncoder.encode(commonPassword);

        for (String phoneNumber : testUserPhoneNumbers) {
            Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                user.setPassword(newHashedPassword);
                userRepository.save(user);
                result.append("用户 '").append(phoneNumber).append("' 的密码已成功修正。<br/>");
            } else {
                result.append("警告：未在数据库中找到用户 '").append(phoneNumber).append("'。<br/>");
            }
        }

        result.append("所有测试用户密码修复完成！现在所有测试用户的密码都已正确设置为 '123456'。");
        return result.toString();
    }
}