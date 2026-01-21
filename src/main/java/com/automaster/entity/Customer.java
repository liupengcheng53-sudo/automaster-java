package com.automaster.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

/**
 * 客户实体类（适配前端字段）
 */
@Entity
@Table(name = "customers")
@Data
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** 客户姓名（前端一致） */
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    /** 联系电话（前端一致） */
    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    /** 客户类型：适配前端Buyer/Seller */
    @Column(name = "type", nullable = false, length = 10)
    private String type; // 存储：Buyer(买方)、Seller(卖方)

    /** 补充联系方式（前端扩展字段） */
    @Column(name = "contact_info", length = 100)
    private String contactInfo;

    /** 备注：对应前端notes */
    @Column(name = "remark", length = 2000)
    private String notes; // 字段名改为notes，和前端一致

    /** 录入时间：对应前端dateAdded */
    @Column(name = "date_added", nullable = false, updatable = false)
    @CreationTimestamp
    private Date dateAdded; // 字段名改为dateAdded，和前端一致
}