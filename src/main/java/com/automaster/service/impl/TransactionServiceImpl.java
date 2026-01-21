package com.automaster.service.impl;

import com.automaster.entity.Car;
import com.automaster.entity.Customer;
import com.automaster.entity.Transaction;
import com.automaster.repository.CarRepository;
import com.automaster.repository.CustomerRepository;
import com.automaster.repository.TransactionRepository;
import com.automaster.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 交易订单业务逻辑层实现
 * 核心：创建交易后自动将车辆状态改为SOLD
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final CarRepository carRepository;
    private final CustomerRepository customerRepository;

    // 构造器注入所有依赖
    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  CarRepository carRepository,
                                  CustomerRepository customerRepository) {
        this.transactionRepository = transactionRepository;
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Transaction> getAllTransactions() {
        // 查询所有交易，并关联车辆/客户信息（用于前端展示）
        List<Transaction> transactions = transactionRepository.findAll();
        transactions.forEach(t -> {
            // 关联车辆信息
            Optional<Car> car = carRepository.findById(t.getCarId());
            car.ifPresent(t::setCar);
            // 关联客户信息
            Optional<Customer> customer = customerRepository.findById(t.getCustomerId());
            customer.ifPresent(t::setCustomer);
        });
        return transactions;
    }

    @Override
    @Transactional // 事务控制：确保创建交易+改车辆状态原子性
    public Transaction saveTransaction(Transaction transaction) {
        // 1. 校验车辆是否存在且未售出
        Optional<Car> carOptional = carRepository.findById(transaction.getCarId());
        if (carOptional.isEmpty()) {
            throw new RuntimeException("关联车辆不存在");
        }
        Car car = carOptional.get();
        if ("SOLD".equals(car.getStatus())) {
            throw new RuntimeException("该车辆已售出，无法创建交易");
        }

        // 2. 校验客户是否存在
        if (!customerRepository.existsById(transaction.getCustomerId())) {
            throw new RuntimeException("关联客户不存在");
        }

        // 3. 自动设置交易日期（前端未传时）
        if (transaction.getDate() == null) {
            transaction.setDate(new java.util.Date());
        }

        // 4. 保存交易订单
        Transaction saved = transactionRepository.save(transaction);

        // 5. 更新车辆状态为SOLD
        car.setStatus("SOLD");
        carRepository.save(car);

        // 6. 关联车辆/客户信息返回给前端
        saved.setCar(car);
        Optional<Customer> customer = customerRepository.findById(transaction.getCustomerId());
        customer.ifPresent(saved::setCustomer);

        return saved;
    }

    @Override
    public boolean deleteTransaction(String id) {
        if (!transactionRepository.existsById(id)) {
            return false;
        }
        transactionRepository.deleteById(id);
        return true;
    }

    @Override
    public Optional<Transaction> getTransactionById(String id) {
        return transactionRepository.findById(id);
    }
}