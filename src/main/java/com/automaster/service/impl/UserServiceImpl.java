package com.automaster.service.impl;

import com.automaster.entity.User;
import com.automaster.repository.UserRepository;
import com.automaster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 用户业务逻辑实现类
 * 
 * @author AutoMaster Team
 * @since 1.0.0
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAllOrderByCreatedAtDesc();
    }

    @Override
    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public User saveUser(User user) {
        // 参数校验
        validateUser(user);
        
        // 检查用户名是否已存在
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("用户名已存在");
        }
        
        // 设置默认值
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("Sales");
        }
        if (user.getStatus() == null || user.getStatus().trim().isEmpty()) {
            user.setStatus("ACTIVE");
        }
        
        return userRepository.save(user);
    }

    @Override
    public Optional<User> updateUser(String id, User user) {
        Optional<User> existingUser = userRepository.findById(id);
        if (existingUser.isEmpty()) {
            return Optional.empty();
        }
        
        // 参数校验
        validateUser(user);
        
        // 检查用户名是否被其他用户占用
        Optional<User> userWithSameUsername = userRepository.findByUsername(user.getUsername());
        if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(id)) {
            throw new IllegalArgumentException("用户名已被其他用户使用");
        }
        
        // 更新字段
        User toUpdate = existingUser.get();
        toUpdate.setUsername(user.getUsername());
        toUpdate.setName(user.getName());
        toUpdate.setRole(user.getRole());
        toUpdate.setEmail(user.getEmail());
        toUpdate.setPhone(user.getPhone());
        toUpdate.setStatus(user.getStatus());
        
        // 如果密码不为空，则更新密码（实际应用中应该加密）
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            toUpdate.setPassword(user.getPassword());
        }
        
        return Optional.of(userRepository.save(toUpdate));
    }

    @Override
    public boolean deleteUser(String id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    @Override
    public List<User> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }

    @Override
    public List<User> searchUsersByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllUsers();
        }
        return userRepository.findByNameContaining(name.trim());
    }

    @Override
    public boolean updateUserStatus(String id, String status) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            return false;
        }
        
        // 校验状态值
        if (!"ACTIVE".equals(status) && !"DISABLED".equals(status)) {
            throw new IllegalArgumentException("无效的状态值，只能是 ACTIVE 或 DISABLED");
        }
        
        User toUpdate = user.get();
        toUpdate.setStatus(status);
        userRepository.save(toUpdate);
        return true;
    }

    /**
     * 用户参数校验
     * 
     * @param user 用户对象
     * @throws IllegalArgumentException 校验失败时抛出
     */
    private void validateUser(User user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("真实姓名不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("用户角色不能为空");
        }
        
        // 校验角色值
        String role = user.getRole().trim();
        if (!"Admin".equals(role) && !"Sales".equals(role)) {
            throw new IllegalArgumentException("用户角色只能是 Admin 或 Sales");
        }
    }
}
