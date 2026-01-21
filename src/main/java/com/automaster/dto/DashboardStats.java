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
@AllArgsConstructor
public class DashboardStats {
    
    /**
     * 默认构造函数，初始化所有字段为 0
     */
    public DashboardStats() {
        this.totalInventoryValue = 0L;
        this.totalInventoryCount = 0;
        this.totalRevenue = 0L;
        this.totalSalesCount = 0;
        this.totalProfit = 0L;
        this.avgProfitRate = 0.0;
        this.totalCustomersCount = 0;
        this.availableCarsCount = 0;
        this.soldCarsCount = 0;
        this.pendingCarsCount = 0;
        this.maintenanceCarsCount = 0;
    }
    
    /**
     * 库存总货值（在售+预定车辆的总价值）
     */
    private Long totalInventoryValue;
    
    /**
     * 库存车辆总数
     */
    private Integer totalInventoryCount;
    
    /**
     * 累计销售额
     */
    private Long totalRevenue;
    
    /**
     * 累计销售订单数
     */
    private Integer totalSalesCount;
    
    /**
     * 预估总利润
     */
    private Long totalProfit;
    
    /**
     * 平均利润率（百分比）
     */
    private Double avgProfitRate;
    
    /**
     * 客户总数
     */
    private Integer totalCustomersCount;
    
    /**
     * 在售车辆数
     */
    private Integer availableCarsCount;
    
    /**
     * 已售车辆数
     */
    private Integer soldCarsCount;
    
    /**
     * 预定车辆数
     */
    private Integer pendingCarsCount;
    
    /**
     * 整备中车辆数
     */
    private Integer maintenanceCarsCount;
}
