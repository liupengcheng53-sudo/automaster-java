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
}
