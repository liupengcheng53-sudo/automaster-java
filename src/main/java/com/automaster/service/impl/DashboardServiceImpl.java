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
}