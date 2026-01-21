package com.automaster.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 静态资源配置：映射本地上传的图片为可访问的URL
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    // 从配置文件读取上传路径
    @Value("${file.upload.path}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 前端访问 http://localhost:8080/uploads/xxx.png 时，映射到本地的 uploadPath 目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}