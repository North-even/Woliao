package com.woliao.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class LoginRequest {
    
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    @NotBlank(message = "验证码不能为空")
    private String verificationCode;
    
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;
    
    public LoginRequest() {}
    
    public LoginRequest(String phoneNumber, String password, String verificationCode, String captchaId) {
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.verificationCode = verificationCode;
        this.captchaId = captchaId;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getVerificationCode() {
        return verificationCode;
    }
    
    public void setVerificationCode(String verificationCode) {
        this.verificationCode = verificationCode;
    }
    
    public String getCaptchaId() {
        return captchaId;
    }
    
    public void setCaptchaId(String captchaId) {
        this.captchaId = captchaId;
    }
} 