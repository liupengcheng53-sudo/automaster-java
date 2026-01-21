package com.automaster.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Swagger(OpenAPI)配置类
 * @Profile({"dev", "test"}) 仅在开发/测试环境生效，生产环境自动禁用
 */
@Configuration
@Profile({"dev", "test"}) // 核心：只在dev/test环境加载该配置，prod环境不加载
public class OpenApiConfig {

    /**
     * 配置Swagger文档基本信息
     */
    @Bean
    public OpenAPI autoMasterOpenAPI() {
        return new OpenAPI()
                // 文档标题+描述+版本
                .info(new Info()
                        .title("AutoMaster 二手车管理系统 API文档")
                        .description("二手车销售额后管系统 - 车辆/客户相关接口")
                        .version("v1.0.0")
                        // 可选：配置联系人信息
                        .contact(new Contact()
                                .name("LPC开发团队")
                                .email("liupengcheng53@gmail.com")));
    }
}