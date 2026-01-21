package com.automaster.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 对所有接口生效
                // 核心修改：用allowedOriginPatterns替代allowedOrigins（推荐）
                .allowedOriginPatterns("http://localhost:5173", "http://localhost:3000")
                // 允许所有请求头（包含前端的Token、Content-Type等）
                .allowedHeaders("*")
                // 允许携带凭证（Cookie/Token），和前端保持一致
                .allowCredentials(true)
                // 允许所有HTTP方法（覆盖GET/POST/PUT/DELETE等）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 预检请求缓存1小时，减少OPTIONS请求次数
                .maxAge(3600);
    }
}