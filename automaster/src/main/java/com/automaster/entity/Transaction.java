package com.automaster.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

/**
 * 交易订单实体类
 * 对应数据库transactions表
 */
@Entity
@Table(name = "transactions")
@Data
public class Transaction {

    /** 订单ID：UUID自动生成 */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    /** 关联车辆ID（外键） */
    @Column(name = "car_id", nullable = false, length = 36)
    private String carId;

    /** 关联客户ID（外键） */
    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    /** 成交金额（元） */
    @Column(name = "price", nullable = false)
    private Integer price;

    /** 交易日期（前端传入/自动生成） */
    @Column(name = "date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    /** 交易类型（前端默认Sale） */
    @Column(name = "type", nullable = false, length = 20)
    private String type; // 如：Sale（销售）

    /** 经手人ID */
    @Column(name = "handled_by_user_id", length = 36)
    private String handledByUserId;

    // 可选：关联查询车辆/客户信息（用于前端展示）
    @Transient // 非数据库字段，仅用于返回给前端
    private Car car; // 关联车辆详情
    @Transient
    private Customer customer; // 关联客户详情
}