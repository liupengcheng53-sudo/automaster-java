package com.automaster.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 仪表盘统计数据 DTO
 *
 * @author AutoMaster Team
 * @since 1.0.0
 */
@Data
@NoArgsConstructor // Lombok 自动生成无参构造
@AllArgsConstructor // 全参构造（按需保留）
public class DashboardStats {
    // 字段初始化：直接给默认值，替代手动构造的赋值逻辑
    /**
     * 库存总货值（在售+预定车辆的总价值）
     */
    private Long totalInventoryValue = 0L;

    /**
     * 库存车辆总数
     */
    private Integer totalInventoryCount = 0;

    /**
     * 累计销售额
     */
    private Long totalRevenue = 0L;

    /**
     * 累计销售订单数
     */
    private Integer totalSalesCount = 0;

    /**
     * 预估总利润
     */
    private Long totalProfit = 0L;

    /**
     * 平均利润率（百分比）
     */
    private Double avgProfitRate = 0.0;

    /**
     * 客户总数
     */
    private Integer totalCustomersCount = 0;

    /**
     * 在售车辆数
     */
    private Integer availableCarsCount = 0;

    /**
     * 已售车辆数
     */
    private Integer soldCarsCount = 0;

    /**
     * 预定车辆数
     */
    private Integer pendingCarsCount = 0;

    /**
     * 整备中车辆数
     */
    private Integer maintenanceCarsCount = 0;
}