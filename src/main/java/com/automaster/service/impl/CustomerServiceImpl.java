package com.automaster.service.impl;

import com.automaster.entity.Customer;
import com.automaster.repository.CustomerRepository;
import com.automaster.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 客户业务逻辑实现类
 * 
 * @author AutoMaster Team
 * @since 1.0.0
 */
@Service
@Transactional
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllOrderByDateAddedDesc();
    }

    @Override
    public Optional<Customer> getCustomerById(String id) {
        return customerRepository.findById(id);
    }

    @Override
    public List<Customer> searchCustomers(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getAllCustomers();
        }
        return customerRepository.searchByKeyword(keyword.trim());
    }

    @Override
    public Customer saveCustomer(Customer customer) {
        // 参数校验
        validateCustomer(customer);
        
        // 检查手机号是否已存在
        Optional<Customer> existingCustomer = customerRepository.findByPhone(customer.getPhone());
        if (existingCustomer.isPresent()) {
            throw new IllegalArgumentException("手机号已存在，请检查后重新录入");
        }
        
        // 设置默认状态
        if (!StringUtils.hasText(customer.getStatus())) {
            customer.setStatus("ACTIVE");
        }
        
        return customerRepository.save(customer);
    }

    @Override
    public Optional<Customer> updateCustomer(String id, Customer customer) {
        Optional<Customer> existingCustomer = customerRepository.findById(id);
        if (existingCustomer.isEmpty()) {
            return Optional.empty();
        }
        
        // 参数校验
        validateCustomer(customer);
        
        // 检查手机号是否被其他客户占用
        Optional<Customer> customerWithSamePhone = customerRepository.findByPhone(customer.getPhone());
        if (customerWithSamePhone.isPresent() && !customerWithSamePhone.get().getId().equals(id)) {
            throw new IllegalArgumentException("手机号已被其他客户使用");
        }
        
        // 更新字段
        Customer toUpdate = existingCustomer.get();
        toUpdate.setName(customer.getName());
        toUpdate.setPhone(customer.getPhone());
        toUpdate.setType(customer.getType());
        toUpdate.setContactInfo(customer.getContactInfo());
        toUpdate.setNotes(customer.getNotes());
        
        return Optional.of(customerRepository.save(toUpdate));
    }

    @Override
    public boolean deleteCustomer(String id) {
        if (!customerRepository.existsById(id)) {
            return false;
        }
        customerRepository.deleteById(id);
        return true;
    }

    @Override
    public List<Customer> getTop10UnpurchasedBuyers() {
        return customerRepository.findTop10UnpurchasedBuyers();
    }

    @Override
    public List<Customer> searchUnpurchasedCustomers(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getTop10UnpurchasedBuyers(); // 无关键词时返回最新10个
        }
        return customerRepository.searchUnpurchasedBuyersByKeyword(keyword.trim());
    }

    /**
     * 客户参数校验
     * 
     * @param customer 客户对象
     * @throws IllegalArgumentException 校验失败时抛出
     */
    private void validateCustomer(Customer customer) {
        if (!StringUtils.hasText(customer.getName())) {
            throw new IllegalArgumentException("客户姓名不能为空");
        }
        if (!StringUtils.hasText(customer.getPhone())) {
            throw new IllegalArgumentException("客户手机号不能为空");
        }
        if (!StringUtils.hasText(customer.getType())) {
            throw new IllegalArgumentException("客户类型不能为空");
        }
        
        // 校验客户类型
        String type = customer.getType().trim();
        if (!"Buyer".equals(type) && !"Seller".equals(type)) {
            throw new IllegalArgumentException("客户类型只能是 Buyer 或 Seller");
        }
        
        // 校验手机号格式（简单校验：11位数字）
        String phone = customer.getPhone().trim();
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new IllegalArgumentException("手机号格式不正确，请输入11位有效手机号");
        }
    }
}
