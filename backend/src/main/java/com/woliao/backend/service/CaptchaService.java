package com.woliao.backend.service;

import com.google.code.kaptcha.Producer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaService {
    
    @Autowired
    private Producer captchaProducer;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRE_TIME = 300; // 5分钟
    
    public CaptchaResponse generateCaptcha() {
        // 生成验证码文本
        String captchaText = captchaProducer.createText();
        
        // 生成验证码图片
        BufferedImage image = captchaProducer.createImage(captchaText);
        
        // 生成唯一标识
        String captchaId = UUID.randomUUID().toString();
        
        // 将验证码存储到Redis
        redisTemplate.opsForValue().set(
            CAPTCHA_PREFIX + captchaId,
            captchaText.toLowerCase(),
            CAPTCHA_EXPIRE_TIME,
            TimeUnit.SECONDS
        );
        
        // 将图片转换为Base64
        String imageBase64 = convertImageToBase64(image);
        
        return new CaptchaResponse(captchaId, imageBase64);
    }
    
    public boolean validateCaptcha(String captchaId, String userInput) {
        if (captchaId == null || userInput == null) {
            return false;
        }
        
        String storedCaptcha = redisTemplate.opsForValue().get(CAPTCHA_PREFIX + captchaId);
        if (storedCaptcha == null) {
            return false;
        }
        
        // 验证成功后删除验证码
        redisTemplate.delete(CAPTCHA_PREFIX + captchaId);
        
        return storedCaptcha.equals(userInput.toLowerCase());
    }
    
    private String convertImageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert image to Base64", e);
        }
    }
    
    public static class CaptchaResponse {
        private String captchaId;
        private String imageBase64;
        
        public CaptchaResponse(String captchaId, String imageBase64) {
            this.captchaId = captchaId;
            this.imageBase64 = imageBase64;
        }
        
        public String getCaptchaId() {
            return captchaId;
        }
        
        public void setCaptchaId(String captchaId) {
            this.captchaId = captchaId;
        }
        
        public String getImageBase64() {
            return imageBase64;
        }
        
        public void setImageBase64(String imageBase64) {
            this.imageBase64 = imageBase64;
        }
    }
} 