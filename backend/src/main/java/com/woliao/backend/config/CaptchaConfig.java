package com.woliao.backend.config;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class CaptchaConfig {
    
    @Bean
    public DefaultKaptcha defaultKaptcha() {
        DefaultKaptcha defaultKaptcha = new DefaultKaptcha();
        Properties properties = new Properties();
        
        // 图片边框
        properties.setProperty("kaptcha.border", "yes");
        properties.setProperty("kaptcha.border.color", "105,179,90");
        
        // 字体颜色
        properties.setProperty("kaptcha.textproducer.font.color", "blue");
        
        // 图片宽度
        properties.setProperty("kaptcha.image.width", "110");
        
        // 图片高度
        properties.setProperty("kaptcha.image.height", "40");
        
        // 字体大小
        properties.setProperty("kaptcha.textproducer.font.size", "30");
        
        // session key
        properties.setProperty("kaptcha.session.key", "code");
        
        // 验证码长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        
        // 字体
        properties.setProperty("kaptcha.textproducer.font.names", "宋体,楷体,微软雅黑");
        
        // 图片效果
        properties.setProperty("kaptcha.obscurificator.impl", "com.google.code.kaptcha.impl.ShadowGimpy");
        
        // 文字间隔
        properties.setProperty("kaptcha.textproducer.char.space", "4");
        
        // 干扰实现类
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");
        
        // 文字渲染器
        properties.setProperty("kaptcha.word.impl", "com.google.code.kaptcha.text.impl.DefaultWordRenderer");
        
        // 背景实现类
        properties.setProperty("kaptcha.background.impl", "com.google.code.kaptcha.impl.DefaultBackground");
        
        // 背景渐变开始颜色
        properties.setProperty("kaptcha.background.clear.from", "lightGray");
        
        // 背景渐变结束颜色
        properties.setProperty("kaptcha.background.clear.to", "white");
        
        Config config = new Config(properties);
        defaultKaptcha.setConfig(config);
        
        return defaultKaptcha;
    }
} 