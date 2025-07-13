package com.woliao.backend.service;

import com.woliao.backend.dto.LoginRequest;
import com.woliao.backend.dto.LoginResponse;
import com.woliao.backend.dto.RefreshTokenRequest;
import com.woliao.backend.dto.RefreshTokenResponse;
import com.woliao.backend.entity.User;
import com.woliao.backend.repository.UserRepository;
import com.woliao.backend.security.JwtTokenProvider;
import com.woliao.backend.service.CaptchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    @Autowired
    private CaptchaService captchaService;
    
    @Value("${jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;
    
    public LoginResponse login(LoginRequest loginRequest) {
        // 验证图形验证码
        if (!captchaService.validateCaptcha(loginRequest.getCaptchaId(), loginRequest.getVerificationCode())) {
            throw new RuntimeException("验证码错误");
        }
        
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                loginRequest.getPhoneNumber(),
                loginRequest.getPassword()
            )
        );
        
        // 生成令牌
        String accessToken = jwtTokenProvider.generateAccessToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        
        // 获取用户信息
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByPhoneNumber(userDetails.getUsername()).orElseThrow();
        
        // 将刷新令牌存储到Redis
        redisTemplate.opsForValue().set(
            "refresh_token:" + refreshToken,
            user.getId().toString(),
            refreshTokenExpiration,
            TimeUnit.MILLISECONDS
        );
        
        return new LoginResponse(accessToken, refreshToken, refreshTokenExpiration);
    }
    
    public RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        
        // 从Redis中获取用户ID
        String userIdStr = redisTemplate.opsForValue().get("refresh_token:" + refreshToken);
        if (userIdStr == null) {
            throw new RuntimeException("刷新令牌无效或已过期");
        }
        
        Long userId = Long.parseLong(userIdStr);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 生成新的访问令牌
        String newAccessToken = jwtTokenProvider.generateAccessToken(
            new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );
        
        return new RefreshTokenResponse(newAccessToken, refreshTokenExpiration);
    }
} 