# AutoMaster 数据库脚本

## 目录说明

本目录存放 AutoMaster 项目的数据库脚本文件。

## 文件列表

### schema.sql

**用途**：数据库建表脚本

**包含内容**：
- `users` 表 - 用户管理
- `customers` 表 - 客户管理
- `cars` 表 - 车辆管理
- `transactions` 表 - 交易订单管理
- `system_logs` 表 - 系统日志（可选）

**使用方法**：

```bash
# 方式一：使用 MySQL 命令行
mysql -u root -p < src/main/resources/sql/schema.sql

# 方式二：在 MySQL 客户端中执行
source /path/to/automaster-java/src/main/resources/sql/schema.sql;
```

## 注意事项

1. **执行顺序**：先执行 `schema.sql` 创建表结构
2. **数据库名称**：默认使用 `automaster` 数据库
3. **字符集**：使用 UTF-8 字符集
4. **初始数据**：`schema.sql` 中包含默认管理员账号等初始化数据

## 数据库配置

项目使用的数据库配置位于 `application.properties` 或 `application-dev.properties` 文件中：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/automaster?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=your_password
```

## 表结构说明

### users 表

用户管理表，存储系统用户信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(36) | 用户ID（UUID） |
| username | VARCHAR(50) | 用户名（唯一） |
| password | VARCHAR(100) | 密码（BCrypt加密） |
| role | VARCHAR(20) | 角色（Admin/Sales） |
| name | VARCHAR(50) | 姓名 |

### customers 表

客户管理表，存储买家和卖家信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(36) | 客户ID（UUID） |
| name | VARCHAR(50) | 客户姓名 |
| phone | VARCHAR(20) | 联系电话（唯一） |
| type | VARCHAR(10) | 客户类型（Buyer/Seller） |
| notes | TEXT | 备注 |
| date_added | DATETIME | 录入时间 |

### cars 表

车辆管理表，存储车辆库存信息。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(36) | 车辆ID（UUID） |
| make | VARCHAR(50) | 品牌 |
| model | VARCHAR(50) | 型号 |
| year | INT | 年份 |
| price | INT | 售价（元） |
| cost_price | INT | 成本价（元） |
| mileage | INT | 里程（公里） |
| color | VARCHAR(30) | 颜色 |
| vin | VARCHAR(50) | VIN码（唯一） |
| status | VARCHAR(20) | 状态（AVAILABLE/SOLD/PENDING/MAINTENANCE） |
| description | TEXT | 描述 |
| image_url | VARCHAR(500) | 图片URL |
| date_added | DATETIME | 入库时间 |
| deposit | INT | 定金（预定时使用） |
| customer_id | VARCHAR(36) | 关联客户ID（预定时使用） |

### transactions 表

交易订单表，存储销售和收购记录。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | VARCHAR(36) | 交易ID（UUID） |
| car_id | VARCHAR(36) | 关联车辆ID |
| customer_id | VARCHAR(36) | 关联客户ID |
| price | INT | 成交价（元） |
| date | DATETIME | 交易日期 |
| type | VARCHAR(20) | 交易类型（Sale/Purchase） |
| handled_by_user_id | VARCHAR(36) | 经手人ID |
| status | VARCHAR(20) | 交易状态（PENDING/COMPLETED） |
| deposit | INT | 定金（预定时使用） |
| final_price | INT | 最终成交价（预定转销售时填写） |

## 业务流程

### 预定流程

1. 客户预定车辆 → 车辆状态改为 `PENDING`
2. 填写定金和客户信息 → 创建状态为 `PENDING` 的交易记录
3. 客户完成支付 → 调用完成预定接口，填写最终成交价
4. 交易状态改为 `COMPLETED`，车辆状态改为 `SOLD`

### 直接销售流程

1. 客户直接购买 → 创建状态为 `COMPLETED` 的交易记录
2. 车辆状态自动改为 `SOLD`

## 维护说明

- **备份**：建议每天自动备份数据库
- **索引**：已为常用查询字段添加索引
- **外键**：已设置外键约束，保证数据一致性
- **日志**：建议定期清理系统日志表

## 联系方式

如有问题，请联系开发团队。
