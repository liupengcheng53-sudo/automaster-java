package com.automaster.service;

import com.automaster.entity.Customer;

import java.util.List;
import java.util.Optional;

/**
 * 客户业务逻辑接口
 * 
 * @author AutoMaster Team
 * @since 1.0.0
 */
public interface CustomerService {

    /**
     * 查询所有客户（按录入时间倒序）
     * 
     * @return 客户列表
     */
    List<Customer> getAllCustomers();

    /**
     * 根据ID查询客户
     * 
     * @param id 客户ID
     * @return 客户对象（Optional）
     */
    Optional<Customer> getCustomerById(String id);

    /**
     * 按关键词搜索客户（姓名或手机号模糊匹配）
     * 
     * @param keyword 搜索关键词
     * @return 客户列表
     */
    List<Customer> searchCustomers(String keyword);

    /**
     * 新增客户
     * 
     * @param customer 客户对象
     * @return 保存后的客户对象
     * @throws IllegalArgumentException 参数校验失败时抛出
     */
    Customer saveCustomer(Customer customer);

    /**
     * 更新客户信息
     * 
     * @param id 客户ID
     * @param customer 新的客户信息
     * @return 更新后的客户对象（Optional）
     */
    Optional<Customer> updateCustomer(String id, Customer customer);

    /**
     * 删除客户
     * 
     * @param id 客户ID
     * @return 是否删除成功
     */
    boolean deleteCustomer(String id);

    /**
     * 查询最新10个未订车的买家客户
     * 条件：客户类型为Buyer，且没有关联任何销售交易记录
     * 
     * @return 最新10个未订车客户列表
     */
    List<Customer> getTop10UnpurchasedBuyers();

    /**
     * 按关键词搜索未订车的买家客户
     * 如果关键词为空，则返回最新10个未订车客户
     * 
     * @param keyword 搜索关键词（姓名或手机号，可为空）
     * @return 未订车客户列表
     */
    List<Customer> searchUnpurchasedCustomers(String keyword);
}
