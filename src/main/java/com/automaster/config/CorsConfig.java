package com.automaster.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的前端地址（覆盖你需要的端口）
                .allowedOrigins("http://localhost:5173", "http://localhost:3000")
                // 新增：允许所有请求头（解决预检请求失败）
                .allowedHeaders("*")
                // 新增：允许携带Cookie/Token等凭证（前端请求带token时必须）
                .allowCredentials(true)
                // 保留：允许的请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                // 新增：预检请求缓存时间（减少OPTIONS请求，提升性能）
                .maxAge(3600);
    }
}