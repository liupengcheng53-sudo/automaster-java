package com.automaster.repository;

import com.automaster.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 * 
 * @author AutoMaster Team
 * @since 1.0.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户对象（Optional）
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据角色查询用户列表
     * 
     * @param role 用户角色（Admin/Sales）
     * @return 用户列表
     */
    List<User> findByRole(String role);

    /**
     * 根据状态查询用户列表
     * 
     * @param status 账号状态（ACTIVE/DISABLED）
     * @return 用户列表
     */
    List<User> findByStatus(String status);

    /**
     * 根据姓名模糊查询用户
     * 
     * @param name 姓名关键词
     * @return 用户列表
     */
    List<User> findByNameContaining(String name);

    /**
     * 查询所有用户（按创建时间倒序）
     * 
     * @return 用户列表
     */
    @Query("SELECT u FROM User u ORDER BY u.createdAt DESC")
    List<User> findAllOrderByCreatedAtDesc();

    /**
     * 根据角色和状态查询用户（按创建时间倒序）
     * 
     * @param role 用户角色
     * @param status 账号状态
     * @return 用户列表
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.status = :status ORDER BY u.createdAt DESC")
    List<User> findByRoleAndStatusOrderByCreatedAtDesc(
            @Param("role") String role, 
            @Param("status") String status
    );
}
