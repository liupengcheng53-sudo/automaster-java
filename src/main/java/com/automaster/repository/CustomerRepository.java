package com.automaster.repository;

import com.automaster.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, String> {

    // 原有方法保留（如根据ID/关键词搜索）
    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:keyword% OR c.phone LIKE %:keyword% ORDER BY c.dateAdded DESC")
    List<Customer> searchByKeyword(@Param("keyword") String keyword);

    // 新增：查询最新10个未购车客户（订单数=0）
    @Query(value = "SELECT c.* FROM customers c " +
            "LEFT JOIN transactions t ON c.id = t.customer_id " +
            "GROUP BY c.id " +
            "HAVING COUNT(t.id) = 0 " +
            "ORDER BY c.date_added DESC " +
            "LIMIT 10", nativeQuery = true)
    List<Customer> findTop10UnpurchasedLatest();

    // 新增：按姓名搜索未购车客户（订单数=0）
    @Query(value = "SELECT c.* FROM customers c " +
            "LEFT JOIN transactions t ON c.id = t.customer_id " +
            "WHERE c.name LIKE %:name% " +
            "GROUP BY c.id " +
            "HAVING COUNT(t.id) = 0 " +
            "ORDER BY c.date_added DESC", nativeQuery = true)
    List<Customer> searchUnpurchasedByName(@Param("name") String name);
}