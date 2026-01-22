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

    /**
     * 完成预定交易（预定转销售）
     * 
     * @param id 交易ID
     * @param finalPrice 最终成交价
     * @return 更新后的交易记录
     */
    Transaction completeTransaction(String id, Integer finalPrice);

    /**
     * 多条件查询交易订单
     * 
     * @param status 订单状态
     * @param orderId 订单号
     * @param carName 车辆名称
     * @param customerInfo 客户信息
     * @param price 价格
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 筛选后的交易订单列表
     */
    List<Transaction> searchTransactions(String status, String orderId, String carName, 
                                        String customerInfo, Integer price, 
                                        String startDate, String endDate);
}