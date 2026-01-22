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
        // 新增场景：isCreate=true，校验所有字段（包括密码）
        validateUser(user, true);

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

        // 修改场景：isCreate=false，跳过密码非空校验
        validateUser(user, false);

        // 检查用户名是否被其他用户占用
        Optional<User> userWithSameUsername = userRepository.findByUsername(user.getUsername());
        if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(id)) {
            throw new IllegalArgumentException("用户名已被其他用户使用");
        }

        // 更新字段：只更新非空的字段，空字段保留原有值
        User toUpdate = existingUser.get();
        // 用户名必须传（校验已保证）
        toUpdate.setUsername(user.getUsername());
        // 姓名必须传（校验已保证）
        toUpdate.setName(user.getName());

        // 角色：非空才更新，空则保留原有值
        if (user.getRole() != null && !user.getRole().trim().isEmpty()) {
            toUpdate.setRole(user.getRole());
        }
        // 邮箱：非空才更新
        if (user.getEmail() != null) {
            toUpdate.setEmail(user.getEmail());
        }
        // 电话：非空才更新
        if (user.getPhone() != null) {
            toUpdate.setPhone(user.getPhone());
        }
        // 状态：非空才更新，空则保留原有值
        if (user.getStatus() != null && !user.getStatus().trim().isEmpty()) {
            toUpdate.setStatus(user.getStatus());
        }

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
     * 用户参数校验（区分新增/修改场景）
     *
     * @param user     用户对象
     * @param isCreate 是否为新增场景（true=新增，false=修改）
     * @throws IllegalArgumentException 校验失败时抛出
     */
    private void validateUser(User user, boolean isCreate) {
        // 用户名：新增/修改都必须校验
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        // 真实姓名：新增/修改都必须校验
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("真实姓名不能为空");
        }

        // 密码：仅新增场景校验非空，修改场景跳过（不填则不改）
        if (isCreate && (user.getPassword() == null || user.getPassword().trim().isEmpty())) {
            throw new IllegalArgumentException("密码不能为空");
        }

        // 角色：
        // - 新增场景：必须校验非空 + 有效值
        // - 修改场景：非空才校验有效值，空则跳过（保留原有值）
        if (user.getRole() != null && !user.getRole().trim().isEmpty()) {
            String role = user.getRole().trim();
            if (!"Admin".equals(role) && !"Sales".equals(role)) {
                throw new IllegalArgumentException("用户角色只能是 Admin 或 Sales");
            }
        } else if (isCreate) {
            // 新增场景下角色为空，直接报错（后续会设默认值，但先校验提示）
            throw new IllegalArgumentException("用户角色不能为空");
        }

        // 状态：修改场景下如果传了值，才校验有效值（新增场景由默认值兜底）
        if (user.getStatus() != null && !user.getStatus().trim().isEmpty()) {
            String status = user.getStatus().trim();
            if (!"ACTIVE".equals(status) && !"DISABLED".equals(status)) {
                throw new IllegalArgumentException("账号状态只能是 ACTIVE 或 DISABLED");
            }
        }
    }
}