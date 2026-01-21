package com.automaster.service.impl;

import com.automaster.entity.Customer;
import com.automaster.repository.CustomerRepository;
import com.automaster.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 客户业务逻辑层实现
 */
@Service
public class CustomerServiceImpl extends CustomerService {
    @Autowired
    private CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        super(customerRepository);
    }


    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    public List<Customer> searchCustomers(String keyword) {
        // 按姓名/电话模糊查询（忽略大小写）
        return customerRepository.findAll().stream()
                .filter(c ->
                        c.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                                c.getPhone().toLowerCase().contains(keyword.toLowerCase())
                )
                .toList();
        // 进阶：可在Repository添加JPQL查询优化性能
        // List<Customer> findByNameContainingIgnoreCaseOrPhoneContainingIgnoreCase(String name, String phone);
    }

    @Override
    public Customer saveCustomer(Customer customer) {
        // 可添加业务校验：如手机号格式、重复手机号提示等
        return customerRepository.save(customer);
    }

    @Override
    public Optional<Customer> updateCustomer(String id, Customer customer) {
        if (!customerRepository.existsById(id)) {
            return Optional.empty();
        }
        customer.setId(id);
        // 录入时间不可修改
        Customer oldCustomer = customerRepository.findById(id).orElse(null);
        if (oldCustomer != null) {
            customer.setDateAdded(oldCustomer.getDateAdded());
        }
        return Optional.of(customerRepository.save(customer));
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
    public Optional<Customer> getCustomerById(String id) {
        return customerRepository.findById(id);
    }


}