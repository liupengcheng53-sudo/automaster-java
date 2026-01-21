package com.automaster.service;

import com.automaster.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * 用户业务逻辑接口
 * 
 * @author AutoMaster Team
 * @since 1.0.0
 */
public interface UserService {

    /**
     * 查询所有用户（按创建时间倒序）
     * 
     * @return 用户列表
     */
    List<User> getAllUsers();

    /**
     * 根据ID查询用户
     * 
     * @param id 用户ID
     * @return 用户对象（Optional）
     */
    Optional<User> getUserById(String id);

    /**
     * 根据用户名查询用户
     * 
     * @param username 用户名
     * @return 用户对象（Optional）
     */
    Optional<User> getUserByUsername(String username);

    /**
     * 新增用户
     * 
     * @param user 用户对象
     * @return 保存后的用户对象
     * @throws IllegalArgumentException 参数校验失败时抛出
     */
    User saveUser(User user);

    /**
     * 更新用户信息
     * 
     * @param id 用户ID
     * @param user 新的用户信息
     * @return 更新后的用户对象（Optional）
     */
    Optional<User> updateUser(String id, User user);

    /**
     * 删除用户
     * 
     * @param id 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(String id);

    /**
     * 根据角色查询用户
     * 
     * @param role 用户角色（Admin/Sales）
     * @return 用户列表
     */
    List<User> getUsersByRole(String role);

    /**
     * 根据姓名模糊查询用户
     * 
     * @param name 姓名关键词
     * @return 用户列表
     */
    List<User> searchUsersByName(String name);

    /**
     * 启用/禁用用户账号
     * 
     * @param id 用户ID
     * @param status 账号状态（ACTIVE/DISABLED）
     * @return 是否操作成功
     */
    boolean updateUserStatus(String id, String status);
}
