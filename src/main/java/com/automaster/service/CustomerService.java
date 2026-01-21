package com.automaster.service;

import com.automaster.entity.Customer;
import com.automaster.repository.CustomerRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Autowired
    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    // 原有方法保留
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public List<Customer> searchCustomers(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        return customerRepository.searchByKeyword(keyword.trim());
    }

    public Customer saveCustomer(Customer customer) {
        // 基础校验
        if (!StringUtils.hasText(customer.getName())) {
            throw new IllegalArgumentException("客户姓名不能为空");
        }
        if (!StringUtils.hasText(customer.getPhone())) {
            throw new IllegalArgumentException("客户手机号不能为空");
        }
        if (!StringUtils.hasText(customer.getType())) {
            throw new IllegalArgumentException("客户类型不能为空（Buyer/Seller）");
        }
        return customerRepository.save(customer);
    }

    public Optional<Customer> updateCustomer(String id, Customer customer) {
        if (!customerRepository.existsById(id)) {
            return Optional.empty();
        }
        customer.setId(id);
        return Optional.of(customerRepository.save(customer));
    }

    public boolean deleteCustomer(String id) {
        if (!customerRepository.existsById(id)) {
            return false;
        }
        customerRepository.deleteById(id);
        return true;
    }

    public Optional<Customer> getCustomerById(String id) {
        return customerRepository.findById(id);
    }

    // 新增：查询最新10个未购车客户
    public List<Customer> getTop10UnpurchasedLatest() {
        return customerRepository.findTop10UnpurchasedLatest();
    }

    // 新增：按姓名搜索未购车客户
    public List<Customer> searchUnpurchasedCustomers(String name) {
        if (!StringUtils.hasText(name)) {
            return getTop10UnpurchasedLatest(); // 无关键词时返回最新10个
        }
        return customerRepository.searchUnpurchasedByName(name.trim());
    }
}