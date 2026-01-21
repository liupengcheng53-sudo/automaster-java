package com.automaster.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "cars")
@Data
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "make", nullable = false, length = 50)
    private String make;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "cost_price", nullable = false)
    private Integer costPrice;

    // 新增：定金字段
    @Column(name = "deposit")
    private Integer deposit;

    // 新增：关联客户ID
    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "mileage", nullable = false)
    private Integer mileage;

    @Column(name = "color", nullable = false, length = 20)
    private String color;

    @Column(name = "vin", nullable = false, unique = true, length = 50)
    private String vin;

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "varchar(20) default 'AVAILABLE'")
    private String status;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "date_added", nullable = false, updatable = false)
    @CreationTimestamp
    private Date dateAdded;
}