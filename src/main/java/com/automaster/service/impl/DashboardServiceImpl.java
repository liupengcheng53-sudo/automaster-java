package com.automaster.service.impl;

import com.automaster.dto.DashboardStats;
import com.automaster.entity.Car;
import com.automaster.entity.Transaction;
import com.automaster.repository.CarRepository;
import com.automaster.repository.CustomerRepository;
import com.automaster.repository.TransactionRepository;
import com.automaster.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 仪表盘统计业务逻辑实现类
 * 
 * @author AutoMaster Team
 * @since 1.0.0
 */
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

        // 1. 获取所有车辆数据
        List<Car> allCars = carRepository.findAll();
        
        // 2. 计算库存总货值（在售 + 预定）
        long inventoryValue = allCars.stream()
                .filter(car -> "AVAILABLE".equals(car.getStatus()) || "PENDING".equals(car.getStatus()))
                .mapToLong(Car::getPrice)
                .sum();
        stats.setTotalInventoryValue(inventoryValue);
        stats.setTotalInventoryCount(allCars.size());

        // 3. 统计各状态车辆数量
        stats.setAvailableCarsCount((int) allCars.stream()
                .filter(car -> "AVAILABLE".equals(car.getStatus()))
                .count());
        stats.setPendingCarsCount((int) allCars.stream()
                .filter(car -> "PENDING".equals(car.getStatus()))
                .count());
        stats.setMaintenanceCarsCount((int) allCars.stream()
                .filter(car -> "MAINTENANCE".equals(car.getStatus()))
                .count());

        // 4. 获取所有交易记录
        List<Transaction> allTransactions = transactionRepository.findAll();
        
        // 5. 计算销售相关数据（只统计 Sale 类型的交易）
        List<Transaction> salesTransactions = allTransactions.stream()
                .filter(tx -> "Sale".equals(tx.getType()))
                .toList();
        
        long totalRevenue = salesTransactions.stream()
                .mapToLong(Transaction::getPrice)
                .sum();
        stats.setTotalRevenue(totalRevenue);
        stats.setTotalSalesCount(salesTransactions.size());
        stats.setSoldCarsCount(salesTransactions.size());

        // 6. 计算总利润（销售价 - 成本价）
        long totalProfit = salesTransactions.stream()
                .mapToLong(tx -> {
                    // 从交易记录中获取车辆信息
                    Car car = carRepository.findById(tx.getCarId()).orElse(null);
                    if (car != null) {
                        return tx.getPrice() - car.getCostPrice();
                    }
                    return 0L;
                })
                .sum();
        stats.setTotalProfit(totalProfit);

        // 7. 计算平均利润率
        if (totalRevenue > 0) {
            double profitRate = (double) totalProfit / totalRevenue * 100;
            stats.setAvgProfitRate(Math.round(profitRate * 10.0) / 10.0); // 保留1位小数
        } else {
            stats.setAvgProfitRate(0.0);
        }

        // 8. 统计客户总数
        long customerCount = customerRepository.count();
        stats.setTotalCustomersCount((int) customerCount);

        return stats;
    }
}
