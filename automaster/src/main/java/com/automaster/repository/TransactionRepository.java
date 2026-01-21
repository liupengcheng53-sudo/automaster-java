package com.automaster.repository;

import com.automaster.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 交易订单数据访问层
 * 基础CRUD由JpaRepository自动实现
 */
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    // 如需扩展查询（如按车辆/客户/日期），可添加方法：
    // List<Transaction> findByCarId(String carId);
    // List<Transaction> findByCustomerId(String customerId);
    // List<Transaction> findByDateBetween(Date start, Date end);
}