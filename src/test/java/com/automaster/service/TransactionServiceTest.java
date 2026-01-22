package com.automaster.service;

import com.automaster.entity.Car;
import com.automaster.entity.Customer;
import com.automaster.entity.Transaction;
import com.automaster.repository.CarRepository;
import com.automaster.repository.CustomerRepository;
import com.automaster.repository.TransactionRepository;
import com.automaster.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * TransactionService 单元测试
 * 测试交易订单管理功能
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private Car mockCar;
    private Customer mockCustomer;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        mockCar = new Car();
        mockCar.setId("car1");
        mockCar.setStatus("PENDING");
        mockCar.setPrice(150000);
        mockCar.setCostPrice(120000);

        mockCustomer = new Customer();
        mockCustomer.setId("customer1");
        mockCustomer.setName("张三");

        mockTransaction = new Transaction();
        mockTransaction.setId("tx1");
        mockTransaction.setCarId("car1");
        mockTransaction.setCustomerId("customer1");
        mockTransaction.setPrice(150000);
        mockTransaction.setStatus("PENDING");
        mockTransaction.setDeposit(10000);
    }

    @Test
    void testCompleteTransaction_Success() {
        // Given
        when(transactionRepository.findById("tx1")).thenReturn(Optional.of(mockTransaction));
        when(carRepository.findById("car1")).thenReturn(Optional.of(mockCar));
        when(customerRepository.findById("customer1")).thenReturn(Optional.of(mockCustomer));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(mockTransaction);
        when(carRepository.save(any(Car.class))).thenReturn(mockCar);

        // When
        Transaction completed = transactionService.completeTransaction("tx1", 160000);

        // Then
        assertNotNull(completed);
        assertEquals("COMPLETED", completed.getStatus());
        assertEquals(160000, completed.getFinalPrice());
        assertEquals(160000, completed.getPrice());

        // Verify
        verify(transactionRepository, times(1)).findById("tx1");
        verify(carRepository, times(1)).findById("car1");
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(carRepository, times(1)).save(any(Car.class));
    }

    @Test
    void testCompleteTransaction_TransactionNotFound() {
        // Given
        when(transactionRepository.findById("tx1")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.completeTransaction("tx1", 160000);
        });

        assertEquals("交易记录不存在", exception.getMessage());
        verify(transactionRepository, times(1)).findById("tx1");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testCompleteTransaction_InvalidStatus() {
        // Given
        mockTransaction.setStatus("COMPLETED");
        when(transactionRepository.findById("tx1")).thenReturn(Optional.of(mockTransaction));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.completeTransaction("tx1", 160000);
        });

        assertEquals("只有预定状态的交易才能完成", exception.getMessage());
        verify(transactionRepository, times(1)).findById("tx1");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testSaveTransaction_CarNotFound() {
        // Given
        when(carRepository.findById("car1")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.saveTransaction(mockTransaction);
        });

        assertEquals("关联车辆不存在", exception.getMessage());
        verify(carRepository, times(1)).findById("car1");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testSaveTransaction_CarAlreadySold() {
        // Given
        mockCar.setStatus("SOLD");
        when(carRepository.findById("car1")).thenReturn(Optional.of(mockCar));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transactionService.saveTransaction(mockTransaction);
        });

        assertEquals("该车辆已售出，无法创建交易", exception.getMessage());
        verify(carRepository, times(1)).findById("car1");
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    // ========== 查询功能测试 ==========

    @Test
    void testSearchTransactions_WithStatus() throws Exception {
        // 准备测试数据
        Transaction tx1 = new Transaction();
        tx1.setId("tx-001");
        tx1.setCarId("car1");
        tx1.setCustomerId("customer1");
        tx1.setStatus("COMPLETED");
        tx1.setPrice(200000);
        tx1.setFinalPrice(200000);
        tx1.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd").parse("2024-01-15"));

        Transaction tx2 = new Transaction();
        tx2.setId("tx-002");
        tx2.setCarId("car1");
        tx2.setCustomerId("customer1");
        tx2.setStatus("PENDING");
        tx2.setPrice(10000);
        tx2.setDeposit(10000);
        tx2.setDate(new java.text.SimpleDateFormat("yyyy-MM-dd").parse("2024-02-20"));

        when(transactionRepository.findAll()).thenReturn(java.util.Arrays.asList(tx1, tx2));
        when(carRepository.findById("car1")).thenReturn(Optional.of(mockCar));
        when(customerRepository.findById("customer1")).thenReturn(Optional.of(mockCustomer));

        // 执行查询：只查询已完成的订单
        java.util.List<Transaction> result = transactionService.searchTransactions(
            "COMPLETED", null, null, null, null, null, null
        );

        // 验证结果
        assertEquals(1, result.size());
        assertEquals("tx-001", result.get(0).getId());
        assertEquals("COMPLETED", result.get(0).getStatus());
    }

    @Test
    void testSearchTransactions_WithOrderId() throws Exception {
        // 准备测试数据
        Transaction tx1 = new Transaction();
        tx1.setId("tx-001");
        tx1.setCarId("car1");
        tx1.setCustomerId("customer1");
        tx1.setStatus("COMPLETED");
        tx1.setDate(new java.util.Date());

        Transaction tx2 = new Transaction();
        tx2.setId("tx-002");
        tx2.setCarId("car1");
        tx2.setCustomerId("customer1");
        tx2.setStatus("PENDING");
        tx2.setDate(new java.util.Date());

        when(transactionRepository.findAll()).thenReturn(java.util.Arrays.asList(tx1, tx2));
        when(carRepository.findById("car1")).thenReturn(Optional.of(mockCar));
        when(customerRepository.findById("customer1")).thenReturn(Optional.of(mockCustomer));

        // 执行查询：模糊匹配订单号
        java.util.List<Transaction> result = transactionService.searchTransactions(
            null, "001", null, null, null, null, null
        );

        // 验证结果
        assertEquals(1, result.size());
        assertEquals("tx-001", result.get(0).getId());
    }

    @Test
    void testSearchTransactions_WithCarName() throws Exception {
        // 准备测试数据
        Car car1 = new Car();
        car1.setId("car-1");
        car1.setYear(2020);
        car1.setMake("Toyota");
        car1.setModel("Camry");

        Car car2 = new Car();
        car2.setId("car-2");
        car2.setYear(2021);
        car2.setMake("Honda");
        car2.setModel("Accord");

        Transaction tx1 = new Transaction();
        tx1.setId("tx-001");
        tx1.setCarId("car-1");
        tx1.setCustomerId("customer1");
        tx1.setStatus("COMPLETED");
        tx1.setDate(new java.util.Date());

        Transaction tx2 = new Transaction();
        tx2.setId("tx-002");
        tx2.setCarId("car-2");
        tx2.setCustomerId("customer1");
        tx2.setStatus("PENDING");
        tx2.setDate(new java.util.Date());

        when(transactionRepository.findAll()).thenReturn(java.util.Arrays.asList(tx1, tx2));
        when(carRepository.findById("car-1")).thenReturn(Optional.of(car1));
        when(carRepository.findById("car-2")).thenReturn(Optional.of(car2));
        when(customerRepository.findById("customer1")).thenReturn(Optional.of(mockCustomer));

        // 执行查询：模糊匹配车辆名称
        java.util.List<Transaction> result = transactionService.searchTransactions(
            null, null, "Camry", null, null, null, null
        );

        // 验证结果
        assertEquals(1, result.size());
        assertEquals("car-1", result.get(0).getCarId());
    }

    @Test
    void testSearchTransactions_WithCustomerInfo() throws Exception {
        // 准备测试数据
        Customer cust1 = new Customer();
        cust1.setId("cust-1");
        cust1.setName("张三");
        cust1.setPhone("13800138000");

        Customer cust2 = new Customer();
        cust2.setId("cust-2");
        cust2.setName("李四");
        cust2.setPhone("13900139000");

        Transaction tx1 = new Transaction();
        tx1.setId("tx-001");
        tx1.setCarId("car1");
        tx1.setCustomerId("cust-1");
        tx1.setStatus("COMPLETED");
        tx1.setDate(new java.util.Date());

        Transaction tx2 = new Transaction();
        tx2.setId("tx-002");
        tx2.setCarId("car1");
        tx2.setCustomerId("cust-2");
        tx2.setStatus("PENDING");
        tx2.setDate(new java.util.Date());

        when(transactionRepository.findAll()).thenReturn(java.util.Arrays.asList(tx1, tx2));
        when(carRepository.findById("car1")).thenReturn(Optional.of(mockCar));
        when(customerRepository.findById("cust-1")).thenReturn(Optional.of(cust1));
        when(customerRepository.findById("cust-2")).thenReturn(Optional.of(cust2));

        // 执行查询：模糊匹配客户姓名
        java.util.List<Transaction> result = transactionService.searchTransactions(
            null, null, null, "张三", null, null, null
        );

        // 验证结果
        assertEquals(1, result.size());
        assertEquals("cust-1", result.get(0).getCustomerId());
    }

    @Test
    void testSearchTransactions_WithPrice() throws Exception {
        // 准备测试数据
        Transaction tx1 = new Transaction();
        tx1.setId("tx-001");
        tx1.setCarId("car1");
        tx1.setCustomerId("customer1");
        tx1.setStatus("COMPLETED");
        tx1.setPrice(200000);
        tx1.setFinalPrice(200000);
        tx1.setDate(new java.util.Date());

        Transaction tx2 = new Transaction();
        tx2.setId("tx-002");
        tx2.setCarId("car1");
        tx2.setCustomerId("customer1");
        tx2.setStatus("PENDING");
        tx2.setPrice(10000);
        tx2.setDeposit(10000);
        tx2.setDate(new java.util.Date());

        when(transactionRepository.findAll()).thenReturn(java.util.Arrays.asList(tx1, tx2));
        when(carRepository.findById("car1")).thenReturn(Optional.of(mockCar));
        when(customerRepository.findById("customer1")).thenReturn(Optional.of(mockCustomer));

        // 执行查询：精确匹配价格（预定状态的定金）
        java.util.List<Transaction> result = transactionService.searchTransactions(
            null, null, null, null, 10000, null, null
        );

        // 验证结果
        assertEquals(1, result.size());
        assertEquals("tx-002", result.get(0).getId());
        assertEquals(10000, result.get(0).getDeposit());
    }

    @Test
    void testSearchTransactions_WithDateRange() throws Exception {
        // 准备测试数据
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
        
        Transaction tx1 = new Transaction();
        tx1.setId("tx-001");
        tx1.setCarId("car1");
        tx1.setCustomerId("customer1");
        tx1.setStatus("COMPLETED");
        tx1.setDate(sdf.parse("2024-01-15"));

        Transaction tx2 = new Transaction();
        tx2.setId("tx-002");
        tx2.setCarId("car1");
        tx2.setCustomerId("customer1");
        tx2.setStatus("PENDING");
        tx2.setDate(sdf.parse("2024-02-20"));

        when(transactionRepository.findAll()).thenReturn(java.util.Arrays.asList(tx1, tx2));
        when(carRepository.findById("car1")).thenReturn(Optional.of(mockCar));
        when(customerRepository.findById("customer1")).thenReturn(Optional.of(mockCustomer));

        // 执行查询：日期范围筛选
        java.util.List<Transaction> result = transactionService.searchTransactions(
            null, null, null, null, null, "2024-02-01", "2024-02-28"
        );

        // 验证结果
        assertEquals(1, result.size());
        assertEquals("tx-002", result.get(0).getId());
    }

    @Test
    void testSearchTransactions_NoResults() throws Exception {
        // 准备测试数据
        Transaction tx1 = new Transaction();
        tx1.setId("tx-001");
        tx1.setCarId("car1");
        tx1.setCustomerId("customer1");
        tx1.setStatus("COMPLETED");
        tx1.setDate(new java.util.Date());

        when(transactionRepository.findAll()).thenReturn(java.util.Arrays.asList(tx1));
        when(carRepository.findById("car1")).thenReturn(Optional.of(mockCar));
        when(customerRepository.findById("customer1")).thenReturn(Optional.of(mockCustomer));

        // 执行查询：不存在的订单号
        java.util.List<Transaction> result = transactionService.searchTransactions(
            null, "tx-999", null, null, null, null, null
        );

        // 验证结果
        assertEquals(0, result.size());
    }

    @Test
    void testSearchTransactions_AllConditionsNull() throws Exception {
        // 准备测试数据
        Transaction tx1 = new Transaction();
        tx1.setId("tx-001");
        tx1.setCarId("car1");
        tx1.setCustomerId("customer1");
        tx1.setStatus("COMPLETED");
        tx1.setDate(new java.util.Date());

        Transaction tx2 = new Transaction();
        tx2.setId("tx-002");
        tx2.setCarId("car1");
        tx2.setCustomerId("customer1");
        tx2.setStatus("PENDING");
        tx2.setDate(new java.util.Date());

        when(transactionRepository.findAll()).thenReturn(java.util.Arrays.asList(tx1, tx2));
        when(carRepository.findById("car1")).thenReturn(Optional.of(mockCar));
        when(customerRepository.findById("customer1")).thenReturn(Optional.of(mockCustomer));

        // 执行查询：所有条件为空，应返回所有记录
        java.util.List<Transaction> result = transactionService.searchTransactions(
            null, null, null, null, null, null, null
        );

        // 验证结果
        assertEquals(2, result.size());
    }
}
