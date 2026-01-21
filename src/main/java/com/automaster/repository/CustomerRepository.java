package com.automaster.repository;

import com.automaster.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 客户数据访问层
 * 
 * @author AutoMaster Team
 * @since 1.0.0
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

    /**
     * 根据手机号查询客户（用于唯一性校验）
     * 
     * @param phone 手机号
     * @return 客户对象（Optional）
     */
    Optional<Customer> findByPhone(String phone);

    /**
     * 按关键词搜索客户（姓名或手机号模糊匹配）
     * 
     * @param keyword 搜索关键词
     * @return 客户列表（按录入时间倒序）
     */
    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:keyword% OR c.phone LIKE %:keyword% ORDER BY c.dateAdded DESC")
    List<Customer> searchByKeyword(@Param("keyword") String keyword);

    /**
     * 查询所有客户（按录入时间倒序）
     * 
     * @return 客户列表
     */
    @Query("SELECT c FROM Customer c ORDER BY c.dateAdded DESC")
    List<Customer> findAllOrderByDateAddedDesc();

    /**
     * 查询最新10个未订车的买家客户
     * 条件：客户类型为Buyer，且没有关联任何交易记录（未购车）
     * 
     * @return 最新10个未订车客户列表
     */
    @Query(value = "SELECT c.* FROM customers c " +
            "WHERE c.type = 'Buyer' " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM transactions t " +
            "    WHERE t.customer_id = c.id AND t.type = 'Sale'" +
            ") " +
            "ORDER BY c.date_added DESC " +
            "LIMIT 10", nativeQuery = true)
    List<Customer> findTop10UnpurchasedBuyers();

    /**
     * 按姓名或手机号搜索未订车的买家客户
     * 条件：客户类型为Buyer，且没有关联任何销售交易记录
     * 
     * @param keyword 搜索关键词（姓名或手机号）
     * @return 未订车客户列表（按录入时间倒序）
     */
    @Query(value = "SELECT c.* FROM customers c " +
            "WHERE c.type = 'Buyer' " +
            "AND (c.name LIKE CONCAT('%', :keyword, '%') OR c.phone LIKE CONCAT('%', :keyword, '%')) " +
            "AND NOT EXISTS (" +
            "    SELECT 1 FROM transactions t " +
            "    WHERE t.customer_id = c.id AND t.type = 'Sale'" +
            ") " +
            "ORDER BY c.date_added DESC", nativeQuery = true)
    List<Customer> searchUnpurchasedBuyersByKeyword(@Param("keyword") String keyword);
}