package com.automaster.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

/**
 * 用户实体类
 * 用途：系统用户管理，包含管理员和销售人员
 * 
 * @author AutoMaster Team
 * @since 1.0.0
 */
@Entity
@Table(name = "users")
@Data
public class User {

    /**
     * 用户ID（UUID）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    /**
     * 登录用户名（唯一）
     */
    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    /**
     * 密码（BCrypt加密）
     */
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    /**
     * 用户角色：Admin-管理员，Sales-销售
     */
    @Column(name = "role", length = 20, nullable = false, columnDefinition = "varchar(20) default 'Sales'")
    private String role;

    /**
     * 真实姓名
     */
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    /**
     * 邮箱地址
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 联系电话
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 账号状态：ACTIVE-正常，DISABLED-禁用
     */
    @Column(name = "status", length = 20, nullable = false, columnDefinition = "varchar(20) default 'ACTIVE'")
    private String status;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Date createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private Date updatedAt;
}
