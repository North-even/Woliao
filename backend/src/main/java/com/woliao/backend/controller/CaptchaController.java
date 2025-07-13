package com.woliao.backend.controller;

import com.woliao.backend.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/captcha")
@CrossOrigin(origins = "*")
public class CaptchaController {
    
    @Autowired
    private CaptchaService captchaService;
    
    @GetMapping("/generate")
    public ResponseEntity<CaptchaService.CaptchaResponse> generateCaptcha() {
        CaptchaService.CaptchaResponse response = captchaService.generateCaptcha();
        return ResponseEntity.ok(response);
    }
} 