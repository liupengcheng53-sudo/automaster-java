package com.automaster.service;

import com.automaster.dto.DashboardStats;

/**
 * 仪表盘统计业务逻辑接口
 * 
 * @author AutoMaster Team
 * @since 1.0.0
 */
public interface DashboardService {
    
    /**
     * 获取仪表盘统计数据
     * 包含库存货值、销售额、利润、客户数等核心指标
     * 
     * @return 仪表盘统计数据
     */
    DashboardStats getDashboardStats();
}
