package com.automaster.service.impl;

import com.automaster.dto.DashboardStats;
import com.automaster.entity.Car;
import com.automaster.entity.Transaction;
import com.automaster.repository.CarRepository;
import com.automaster.repository.CustomerRepository;
import com.automaster.repository.TransactionRepository;
import com.automaster.service.DashboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final CarRepository carRepository;
    private final TransactionRepository transactionRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public DashboardServiceImpl(
            CarRepository carRepository,
            TransactionRepository transactionRepository,
            CustomerRepository customerRepository) {
        this.carRepository = carRepository;
        this.transactionRepository = transactionRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        try {
            // 1. 获取所有车辆数据（添加空值校验）
            List<Car> allCars = carRepository.findAll();
            if (allCars == null) {
                allCars = List.of(); // 空列表，避免NPE
            }

            // 2. 计算库存总货值（在售 + 预定）
            long inventoryValue = allCars.stream()
                    .filter(car -> car != null
                            && ("AVAILABLE".equals(car.getStatus()) || "PENDING".equals(car.getStatus())))
                    .mapToLong(car -> car.getPrice() != null ? car.getPrice() : 0L) // 空值容错
                    .sum();
            stats.setTotalInventoryValue(inventoryValue);
            stats.setTotalInventoryCount(allCars.size());

            // 3. 统计各状态车辆数量（添加car != null校验）
            stats.setAvailableCarsCount((int) allCars.stream()
                    .filter(car -> car != null && "AVAILABLE".equals(car.getStatus()))
                    .count());
            stats.setPendingCarsCount((int) allCars.stream()
                    .filter(car -> car != null && "PENDING".equals(car.getStatus()))
                    .count());
            stats.setMaintenanceCarsCount((int) allCars.stream()
                    .filter(car -> car != null && "MAINTENANCE".equals(car.getStatus()))
                    .count());

            // 4. 获取所有交易记录（添加空值校验）
            List<Transaction> allTransactions = transactionRepository.findAll();
            if (allTransactions == null) {
                allTransactions = List.of();
            }

            // 5. 计算销售相关数据（只统计 Sale 类型的交易）
            List<Transaction> salesTransactions = allTransactions.stream()
                    .filter(tx -> tx != null && "Sale".equals(tx.getType()))
                    .toList();

            long totalRevenue = salesTransactions.stream()
                    .mapToLong(tx -> tx.getPrice() != null ? tx.getPrice() : 0L) // 空值容错
                    .sum();
            stats.setTotalRevenue(totalRevenue);
            stats.setTotalSalesCount(salesTransactions.size());
            stats.setSoldCarsCount(salesTransactions.size());

            // 6. 计算总利润（核心修复：解决空指针）
            long totalProfit = salesTransactions.stream()
                    .mapToLong(tx -> {
                        if (tx == null || tx.getCarId() == null) {
                            return 0L;
                        }
                        // 避免findById返回null，用orElseThrow不如orElse(null)更容错
                        Car car = carRepository.findById(tx.getCarId()).orElse(null);
                        if (car == null || tx.getPrice() == null || car.getCostPrice() == null) {
                            log.warn("交易{}的车辆信息不完整，利润计算为0", tx.getId());
                            return 0L;
                        }
                        return tx.getPrice() - car.getCostPrice();
                    })
                    .sum();
            stats.setTotalProfit(totalProfit);

            // 7. 计算平均利润率（避免除以0）
            if (totalRevenue > 0) {
                double profitRate = (double) totalProfit / totalRevenue * 100;
                stats.setAvgProfitRate(Math.round(profitRate * 10.0) / 10.0); // 保留1位小数
            } else {
                stats.setAvgProfitRate(0.0);
            }

            // 8. 统计客户总数（添加空值容错）
            long customerCount = customerRepository.count();
            stats.setTotalCustomersCount((int) customerCount);

        } catch (Exception e) {
            log.error("计算仪表盘统计数据失败：", e);
            throw e; // 抛给Controller处理
        }

        return stats;
    }

    @Override
    public java.util.List<java.util.Map<String, Object>> getSalesTrend() {
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        
        try {
            // 获取当前日期
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            
            // 计算近6个月的销售额
            for (int i = 5; i >= 0; i--) {
                // 设置为 i 个月前
                java.util.Calendar monthCal = (java.util.Calendar) calendar.clone();
                monthCal.add(java.util.Calendar.MONTH, -i);
                
                // 获取月份名称（例如：1月、2月）
                int month = monthCal.get(java.util.Calendar.MONTH) + 1; // 0-based
                String monthName = month + "月";
                
                // 计算该月的开始和结束日期
                java.util.Calendar startOfMonth = (java.util.Calendar) monthCal.clone();
                startOfMonth.set(java.util.Calendar.DAY_OF_MONTH, 1);
                startOfMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
                startOfMonth.set(java.util.Calendar.MINUTE, 0);
                startOfMonth.set(java.util.Calendar.SECOND, 0);
                startOfMonth.set(java.util.Calendar.MILLISECOND, 0);
                
                java.util.Calendar endOfMonth = (java.util.Calendar) monthCal.clone();
                endOfMonth.set(java.util.Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
                endOfMonth.set(java.util.Calendar.HOUR_OF_DAY, 23);
                endOfMonth.set(java.util.Calendar.MINUTE, 59);
                endOfMonth.set(java.util.Calendar.SECOND, 59);
                endOfMonth.set(java.util.Calendar.MILLISECOND, 999);
                
                java.util.Date startDate = startOfMonth.getTime();
                java.util.Date endDate = endOfMonth.getTime();
                
                // 查询该月的所有销售交易
                List<Transaction> monthTransactions = transactionRepository.findAll().stream()
                    .filter(tx -> tx != null 
                        && "Sale".equals(tx.getType()) 
                        && tx.getDate() != null
                        && !tx.getDate().before(startDate) 
                        && !tx.getDate().after(endDate))
                    .toList();
                
                // 计算该月销售额
                long monthRevenue = monthTransactions.stream()
                    .mapToLong(tx -> tx.getPrice() != null ? tx.getPrice() : 0L)
                    .sum();
                
                // 构造结果数据
                java.util.Map<String, Object> monthData = new java.util.HashMap<>();
                monthData.put("name", monthName);
                monthData.put("value", monthRevenue);
                result.add(monthData);
            }
            
        } catch (Exception e) {
            log.error("计算销售趋势数据失败：", e);
            throw e;
        }
        
        return result;
    }
}