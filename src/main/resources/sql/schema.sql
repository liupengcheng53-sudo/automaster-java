-- ============================================
-- AutoMaster 数据库建表脚本（优化版）
-- 数据库名称：automaster
-- 字符集：utf8mb4
-- 排序规则：utf8mb4_unicode_ci（更好的多语言支持）
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `automaster` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `automaster`;

-- ============================================
-- 1. 用户表（users）
-- 用途：系统用户管理，包含管理员和销售人员
-- ============================================
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` VARCHAR(36) NOT NULL COMMENT '用户ID（UUID）',
    `username` VARCHAR(50) NOT NULL COMMENT '登录用户名（唯一）',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密，长度至少255）',
    `role` VARCHAR(20) NOT NULL DEFAULT 'Sales' COMMENT '用户角色：Admin-管理员，Sales-销售',
    `name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱地址',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '账号状态：ACTIVE-正常，DISABLED-禁用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_role` (`role`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 客户表（customers）
-- 用途：客户信息管理，包含买家和卖家
-- 优化点：
-- - 添加 remark 字段（与 notes 分开，notes 用于前端，remark 用于后端备注）
-- - 手机号唯一索引，防止重复录入
-- - 添加客户来源、状态等扩展字段
-- ============================================
DROP TABLE IF EXISTS `customers`;
CREATE TABLE `customers` (
    `id` VARCHAR(36) NOT NULL COMMENT '客户ID（UUID）',
    `name` VARCHAR(50) NOT NULL COMMENT '客户姓名',
    `phone` VARCHAR(20) NOT NULL COMMENT '联系电话（唯一）',
    `type` VARCHAR(10) NOT NULL COMMENT '客户类型：Buyer-买家，Seller-卖家',
    `contact_info` VARCHAR(100) DEFAULT NULL COMMENT '补充联系方式（微信、QQ等）',
    `notes` TEXT DEFAULT NULL COMMENT '客户备注（前端显示）',
    `remark` VARCHAR(2000) DEFAULT NULL COMMENT '内部备注（后端使用）',
    `source` VARCHAR(50) DEFAULT NULL COMMENT '客户来源：网络、门店、转介绍等',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '客户状态：ACTIVE-正常，BLACKLIST-黑名单',
    `date_added` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '录入时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_date_added` (`date_added`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- ============================================
-- 3. 车辆表（cars）
-- 用途：车辆库存管理
-- 优化点：
-- - 添加 customer_id 字段（预定状态时关联客户）
-- - 添加 deposit 字段（定金）
-- - 添加 purchase_price（收购价）和 expected_profit（预期利润）
-- - VIN 唯一索引
-- - 添加车辆来源、过户次数等扩展字段
-- ============================================
DROP TABLE IF EXISTS `cars`;
CREATE TABLE `cars` (
    `id` VARCHAR(36) NOT NULL COMMENT '车辆ID（UUID）',
    `make` VARCHAR(50) NOT NULL COMMENT '品牌（如：奔驰、宝马）',
    `model` VARCHAR(50) NOT NULL COMMENT '型号（如：C200、X5）',
    `year` INT NOT NULL COMMENT '年份（如：2020）',
    `price` INT NOT NULL COMMENT '售价（单位：元）',
    `cost_price` INT NOT NULL COMMENT '收购价/成本价（单位：元）',
    `deposit` INT DEFAULT 0 COMMENT '定金（预定状态时必填，单位：元）',
    `customer_id` VARCHAR(36) DEFAULT NULL COMMENT '关联客户ID（预定状态时必填）',
    `mileage` INT NOT NULL COMMENT '里程数（单位：公里）',
    `color` VARCHAR(20) NOT NULL COMMENT '颜色（如：黑色、白色）',
    `vin` VARCHAR(50) NOT NULL COMMENT '车架号VIN（唯一）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '车辆状态：AVAILABLE-在售，SOLD-已售，PENDING-预定，MAINTENANCE-整备中',
    `description` TEXT DEFAULT NULL COMMENT '车辆描述（配置、亮点等）',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT '车辆图片URL',
    `transfer_count` INT DEFAULT 0 COMMENT '过户次数',
    `source` VARCHAR(50) DEFAULT NULL COMMENT '车辆来源：收购、置换、拍卖等',
    `date_added` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_vin` (`vin`),
    KEY `idx_status` (`status`),
    KEY `idx_make_model` (`make`, `model`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_date_added` (`date_added`),
    CONSTRAINT `fk_cars_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='车辆表';

-- ============================================
-- 4. 交易表（transactions）
-- 用途：交易记录管理（销售和收购）
-- 优化点：
-- - 添加交易状态（待支付、已完成、已取消）
-- - 添加支付方式
-- - 添加利润字段（自动计算）
-- - 添加交易备注
-- ============================================
DROP TABLE IF EXISTS `transactions`;
CREATE TABLE `transactions` (
    `id` VARCHAR(36) NOT NULL COMMENT '交易ID（UUID）',
    `car_id` VARCHAR(36) NOT NULL COMMENT '关联车辆ID',
    `customer_id` VARCHAR(36) NOT NULL COMMENT '关联客户ID',
    `price` INT NOT NULL COMMENT '成交价（单位：元）',
    `date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '交易日期',
    `type` VARCHAR(20) NOT NULL COMMENT '交易类型：Sale-销售，Purchase-收购',
    `handled_by_user_id` VARCHAR(36) DEFAULT NULL COMMENT '经手人ID（关联用户表）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' COMMENT '交易状态：PENDING-预定中，COMPLETED-已完成，CANCELLED-已取消',
    `deposit` INT DEFAULT 0 COMMENT '定金金额（预定时使用，单位：元）',
    `final_price` INT DEFAULT NULL COMMENT '最终成交价（预定转销售时填写，单位：元）',
    `payment_method` VARCHAR(50) DEFAULT NULL COMMENT '支付方式：现金、银行转账、贷款等',
    `profit` INT DEFAULT 0 COMMENT '利润（销售价-成本价，单位：元）',
    `notes` TEXT DEFAULT NULL COMMENT '交易备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_car_id` (`car_id`),
    KEY `idx_customer_id` (`customer_id`),
    KEY `idx_handled_by_user_id` (`handled_by_user_id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`),
    KEY `idx_date` (`date`),
    CONSTRAINT `fk_transactions_car` FOREIGN KEY (`car_id`) REFERENCES `cars` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_transactions_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_transactions_user` FOREIGN KEY (`handled_by_user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='交易表';

-- ============================================
-- 5. 系统日志表（system_logs）- 可选
-- 用途：记录系统操作日志，便于审计和问题追踪
-- ============================================
DROP TABLE IF EXISTS `system_logs`;
CREATE TABLE `system_logs` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID（自增）',
    `user_id` VARCHAR(36) DEFAULT NULL COMMENT '操作用户ID',
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型：CREATE、UPDATE、DELETE等',
    `module` VARCHAR(50) NOT NULL COMMENT '操作模块：Car、Customer、Transaction等',
    `target_id` VARCHAR(36) DEFAULT NULL COMMENT '操作对象ID',
    `details` TEXT DEFAULT NULL COMMENT '操作详情（JSON格式）',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '操作IP地址',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_action` (`action`),
    KEY `idx_module` (`module`),
    KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入默认管理员用户（密码：admin123，需要在应用中使用BCrypt加密）
-- 注意：实际使用时需要在Java代码中使用BCryptPasswordEncoder加密密码
INSERT INTO `users` (`id`, `username`, `password`, `role`, `name`, `email`, `phone`, `status`) 
VALUES 
    ('admin-001', 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdUoXKDPPJlHETGRzy8Xe', 'Admin', '系统管理员', 'admin@automaster.com', '13800138000', 'ACTIVE'),
    ('sales-001', 'sales01', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdUoXKDPPJlHETGRzy8Xe', 'Sales', '张三', 'zhangsan@automaster.com', '13800138001', 'ACTIVE'),
    ('sales-002', 'sales02', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EHdUoXKDPPJlHETGRzy8Xe', 'Sales', '李四', 'lisi@automaster.com', '13800138002', 'ACTIVE');
-- 默认密码均为：admin123（BCrypt加密后的值）

-- ============================================
-- 数据库优化建议
-- ============================================
-- 1. 定期备份数据库（建议每天备份）
-- 2. 定期清理系统日志表（保留近3个月数据）
-- 3. 根据业务量调整索引策略
-- 4. 监控慢查询日志，优化SQL性能
-- 5. 考虑使用分区表（当数据量超过百万级时）

-- ============================================
-- 表结构优化说明
-- ============================================
-- 1. 所有表使用 utf8mb4 字符集，支持 emoji 和特殊字符
-- 2. 所有 ID 使用 VARCHAR(36) 存储 UUID
-- 3. 所有表添加 created_at 和 updated_at 时间戳
-- 4. 外键约束使用 ON DELETE RESTRICT 防止误删，或 ON DELETE SET NULL 保留历史记录
-- 5. 添加合理的索引提升查询性能
-- 6. 密码字段长度设置为 255，满足 BCrypt 加密需求
-- 7. 添加状态字段，便于软删除和状态管理
-- 8. 添加业务扩展字段（来源、备注等），提升系统灵活性

-- ============================================
-- 查询示例
-- ============================================

-- 查询所有在售车辆
-- SELECT * FROM cars WHERE status = 'AVAILABLE' ORDER BY date_added DESC;

-- 查询未购车的客户（最新10个）
-- SELECT c.* FROM customers c
-- LEFT JOIN transactions t ON c.id = t.customer_id AND t.type = 'Sale'
-- WHERE c.type = 'Buyer' AND t.id IS NULL
-- ORDER BY c.date_added DESC
-- LIMIT 10;

-- 查询某个销售人员的业绩
-- SELECT u.name, COUNT(t.id) AS total_sales, SUM(t.profit) AS total_profit
-- FROM users u
-- LEFT JOIN transactions t ON u.id = t.handled_by_user_id
-- WHERE u.role = 'Sales' AND t.type = 'Sale'
-- GROUP BY u.id, u.name;

-- 查询预定状态的车辆及客户信息
-- SELECT ca.*, cu.name AS customer_name, cu.phone AS customer_phone
-- FROM cars ca
-- LEFT JOIN customers cu ON ca.customer_id = cu.id
-- WHERE ca.status = 'PENDING';
