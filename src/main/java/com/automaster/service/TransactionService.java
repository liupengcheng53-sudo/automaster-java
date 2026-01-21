package com.automaster.service;

import com.automaster.entity.Transaction;

import java.util.List;
import java.util.Optional;

/**
 * 交易订单业务逻辑层接口
 */
public interface TransactionService {
    /**
     * 查询所有交易订单（关联车辆/客户信息）
     */
    List<Transaction> getAllTransactions();

    /**
     * 新增交易订单（自动更新车辆状态为SOLD）
     */
    Transaction saveTransaction(Transaction transaction);

    /**
     * 删除交易订单
     */
    boolean deleteTransaction(String id);

    /**
     * 按ID查询交易订单
     */
    Optional<Transaction> getTransactionById(String id);
}