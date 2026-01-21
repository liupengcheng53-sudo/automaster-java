package com.automaster.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * 启动类
 * 1. @ComponentScan：扫描所有业务包（controller/config等）
 * 2. @EnableJpaRepositories：扫描JPA仓库接口（repository包）
 * 3. @EntityScan：扫描JPA实体类（entity包）—— 新增核心注解
 */
@SpringBootApplication
// 扫描com.automaster下所有子包（controller/config等）
@ComponentScan(basePackages = "com.automaster")
// 扫描JPA仓库接口所在的包
@EnableJpaRepositories(basePackages = "com.automaster.repository")
// 核心新增：扫描JPA实体类所在的包，解决“Not a managed type”错误
@EntityScan(basePackages = "com.automaster.entity")
public class AutoMasterApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoMasterApplication.class, args);
    }

}