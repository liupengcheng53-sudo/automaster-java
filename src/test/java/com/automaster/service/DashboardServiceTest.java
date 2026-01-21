package com.automaster.service;

import com.automaster.dto.DashboardStats;
import com.automaster.entity.Car;
import com.automaster.entity.Transaction;
import com.automaster.repository.CarRepository;
import com.automaster.repository.CustomerRepository;
import com.automaster.repository.TransactionRepository;
import com.automaster.service.impl.DashboardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DashboardService 单元测试
 * 测试仪表盘统计功能
 */
@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private CarRepository carRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private List<Car> mockCars;
    private List<Transaction> mockTransactions;

    @BeforeEach
    void setUp() {
        // 准备测试数据
        Car car1 = new Car();
        car1.setId("car1");
        car1.setStatus("AVAILABLE");
        car1.setPrice(100000);
        car1.setCostPrice(80000);

        Car car2 = new Car();
        car2.setId("car2");
        car2.setStatus("PENDING");
        car2.setPrice(150000);
        car2.setCostPrice(120000);

        Car car3 = new Car();
        car3.setId("car3");
        car3.setStatus("SOLD");
        car3.setPrice(200000);
        car3.setCostPrice(160000);

        mockCars = Arrays.asList(car1, car2, car3);

        Transaction tx1 = new Transaction();
        tx1.setId("tx1");
        tx1.setCarId("car3");
        tx1.setType("Sale");
        tx1.setPrice(200000);

        mockTransactions = Arrays.asList(tx1);
    }

    @Test
    void testGetDashboardStats() {
        // Given
        when(carRepository.findAll()).thenReturn(mockCars);
        when(transactionRepository.findAll()).thenReturn(mockTransactions);
        when(customerRepository.count()).thenReturn(10L);
        when(carRepository.findById("car3")).thenReturn(Optional.of(mockCars.get(2)));

        // When
        DashboardStats stats = dashboardService.getDashboardStats();

        // Then
        assertNotNull(stats);
        assertEquals(250000, stats.getTotalInventoryValue()); // 100000 + 150000
        assertEquals(3, stats.getTotalInventoryCount());
        assertEquals(200000, stats.getTotalRevenue());
        assertEquals(1, stats.getTotalSalesCount());
        assertEquals(40000, stats.getTotalProfit()); // 200000 - 160000
        assertEquals(20.0, stats.getAvgProfitRate()); // 40000 / 200000 * 100
        assertEquals(10, stats.getTotalCustomersCount());
        assertEquals(1, stats.getAvailableCarsCount());
        assertEquals(1, stats.getSoldCarsCount());
        assertEquals(1, stats.getPendingCarsCount());
        assertEquals(0, stats.getMaintenanceCarsCount());

        // Verify
        verify(carRepository, times(1)).findAll();
        verify(transactionRepository, times(1)).findAll();
        verify(customerRepository, times(1)).count();
    }

    @Test
    void testGetDashboardStatsWithNoData() {
        // Given
        when(carRepository.findAll()).thenReturn(Arrays.asList());
        when(transactionRepository.findAll()).thenReturn(Arrays.asList());
        when(customerRepository.count()).thenReturn(0L);

        // When
        DashboardStats stats = dashboardService.getDashboardStats();

        // Then
        assertNotNull(stats);
        assertEquals(0, stats.getTotalInventoryValue());
        assertEquals(0, stats.getTotalInventoryCount());
        assertEquals(0, stats.getTotalRevenue());
        assertEquals(0, stats.getTotalSalesCount());
        assertEquals(0, stats.getTotalProfit());
        assertEquals(0.0, stats.getAvgProfitRate());
        assertEquals(0, stats.getTotalCustomersCount());
    }
}
