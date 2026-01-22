package com.automaster.service.impl;

import com.automaster.entity.Car;
import com.automaster.entity.Customer;
import com.automaster.entity.Transaction;
import com.automaster.repository.CarRepository;
import com.automaster.repository.CustomerRepository;
import com.automaster.repository.TransactionRepository;
import com.automaster.repository.UserRepository;
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
    private final UserRepository userRepository;

    // 构造器注入所有依赖
    @Autowired
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  CarRepository carRepository,
                                  CustomerRepository customerRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.carRepository = carRepository;
        this.customerRepository = customerRepository;
        this.userRepository = userRepository;
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

        String handledByUserId = transaction.getHandledByUserId();
        if (handledByUserId != null && !handledByUserId.trim().isEmpty()) {
            // 传了值：校验该用户是否存在（必须是users表中有效的UUID）
            if (!userRepository.existsById(handledByUserId.trim())) {
                throw new RuntimeException("经手人ID不存在：" + handledByUserId);
            }
        } else {
            // 没传值/空字符串：强制设为NULL（表结构允许NULL）
            transaction.setHandledByUserId(null);
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

    @Override
    @Transactional
    public Transaction completeTransaction(String id, Integer finalPrice) {
        // 1. 查询交易记录
        Optional<Transaction> transactionOptional = transactionRepository.findById(id);
        if (transactionOptional.isEmpty()) {
            throw new RuntimeException("交易记录不存在");
        }
        Transaction transaction = transactionOptional.get();

        // 2. 校验交易状态
        if (!"PENDING".equals(transaction.getStatus())) {
            throw new RuntimeException("只有预定状态的交易才能完成");
        }

        // 3. 更新交易信息
        transaction.setStatus("COMPLETED");
        transaction.setFinalPrice(finalPrice);
        transaction.setPrice(finalPrice); // 同步更新 price 字段
        transaction.setDate(new java.util.Date()); // 更新为实际成交时间

        // 4. 更新车辆状态为 SOLD
        Optional<Car> carOptional = carRepository.findById(transaction.getCarId());
        if (carOptional.isPresent()) {
            Car car = carOptional.get();
            car.setStatus("SOLD");
            car.setCustomerId(null); // 清空预定客户关联
            car.setDeposit(0); // 清空定金
            carRepository.save(car);
            transaction.setCar(car);
        }

        // 5. 保存交易记录
        Transaction saved = transactionRepository.save(transaction);

        // 6. 关联客户信息
        Optional<Customer> customer = customerRepository.findById(transaction.getCustomerId());
        customer.ifPresent(saved::setCustomer);

        return saved;
    }

    @Override
    public List<Transaction> searchTransactions(String status, String orderId, String carName, 
                                        String customerInfo, Integer price, 
                                        String startDate, String endDate) {
        // 1. 查询所有交易记录
        List<Transaction> transactions = transactionRepository.findAll();
        
        // 2. 关联车辆和客户信息
        transactions.forEach(t -> {
            Optional<Car> car = carRepository.findById(t.getCarId());
            car.ifPresent(t::setCar);
            Optional<Customer> customer = customerRepository.findById(t.getCustomerId());
            customer.ifPresent(t::setCustomer);
        });
        
        // 3. 筛选数据
        return transactions.stream()
                .filter(t -> {
                    // 状态筛选
                    if (status != null && !status.isEmpty() && !status.equals(t.getStatus())) {
                        return false;
                    }
                    
                    // 订单号筛选（模糊匹配）
                    if (orderId != null && !orderId.isEmpty() && !t.getId().toLowerCase().contains(orderId.toLowerCase())) {
                        return false;
                    }
                    
                    // 车辆名称筛选（模糊匹配）
                    if (carName != null && !carName.isEmpty()) {
                        if (t.getCar() == null) {
                            return false;
                        }
                        String fullCarName = t.getCar().getYear() + " " + t.getCar().getMake() + " " + t.getCar().getModel();
                        if (!fullCarName.toLowerCase().contains(carName.toLowerCase())) {
                            return false;
                        }
                    }
                    
                    // 客户信息筛选（模糊匹配姓名或电话）
                    if (customerInfo != null && !customerInfo.isEmpty()) {
                        if (t.getCustomer() == null) {
                            return false;
                        }
                        String fullCustomerInfo = t.getCustomer().getName() + " " + t.getCustomer().getPhone();
                        if (!fullCustomerInfo.toLowerCase().contains(customerInfo.toLowerCase())) {
                            return false;
                        }
                    }
                    
                    // 价格筛选（精确匹配定金或成交价）
                    if (price != null) {
                        boolean matchPrice = false;
                        if ("PENDING".equals(t.getStatus())) {
                            // 预定状态：匹配定金或价格
                            matchPrice = (t.getDeposit() != null && t.getDeposit().equals(price)) ||
                                       (t.getPrice() != null && t.getPrice().equals(price));
                        } else {
                            // 已完成状态：匹配成交价或价格
                            matchPrice = (t.getFinalPrice() != null && t.getFinalPrice().equals(price)) ||
                                       (t.getPrice() != null && t.getPrice().equals(price));
                        }
                        if (!matchPrice) {
                            return false;
                        }
                    }
                    
                    // 日期范围筛选（支持 ISO 8601 格式：yyyy-MM-dd）
                    if (startDate != null && !startDate.isEmpty()) {
                        try {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                            java.util.Date txDate = t.getDate();
                            java.util.Date start = sdf.parse(startDate);
                            if (txDate.before(start)) {
                                return false;
                            }
                        } catch (Exception e) {
                            // 日期解析失败，忽略该条件
                        }
                    }
                    
                    if (endDate != null && !endDate.isEmpty()) {
                        try {
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                            java.util.Date txDate = t.getDate();
                            java.util.Date end = sdf.parse(endDate);
                            // 设置结束日期为当天 23:59:59
                            java.util.Calendar cal = java.util.Calendar.getInstance();
                            cal.setTime(end);
                            cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
                            cal.set(java.util.Calendar.MINUTE, 59);
                            cal.set(java.util.Calendar.SECOND, 59);
                            end = cal.getTime();
                            if (txDate.after(end)) {
                                return false;
                            }
                        } catch (Exception e) {
                            // 日期解析失败，忽略该条件
                        }
                    }
                    
                    return true;
                })
                .toList();
    }
}