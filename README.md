# 🍜 食光外卖平台 (Sky Take-Out)

一个基于 Spring Boot 的外卖点餐平台后端系统，支持管理端和用户端双端业务。

---

## ✨ 功能特性

### 管理端
- 员工管理（登录、CRUD、状态管理）
- 菜品分类管理
- 菜品管理（口味配置、图片上传）
- 套餐管理（菜品组合、起售停售）
- 订单管理（接单、拒单、派送、完成、搜索）
- 数据统计（营业额、用户数、订单量、销量 Top10）
- 工作台（今日运营数据概览）
- 店铺营业状态控制

### 用户端
- 微信登录（JWT 认证）
- 菜品/套餐浏览
- 购物车管理
- 地址簿管理
- 下单与支付（演示模式）
- 历史订单查询
- 催单、再来一单、取消订单

### 实时推送
- WebSocket 来单提醒
- WebSocket 催单提醒

---

## 🛠 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 2.7.3 | 基础框架 |
| MyBatis | 2.2.0 | ORM 框架 |
| MySQL | 8.0 | 数据库 |
| Redis | 7.x | 缓存 + Token 管理 |
| Druid | 1.2.1 | 数据库连接池 |
| JWT (jjwt) | 0.9.1 | 登录鉴权 |
| WebSocket | JSR 356 | 实时推送 |
| Knife4j | 3.0.2 | API 文档 |
| PageHelper | 1.3.0 | 分页插件 |
| Lombok | 1.18.20 | 简化代码 |

---

## 📁 项目结构

```
sky-take-out/
├── sky-common/          # 公共模块（工具类、异常、常量）
├── sky-pojo/            # 数据模型（Entity、DTO、VO）
├── sky-server/          # 主服务（Controller、Service、Mapper）
├── db/                  # 数据库初始化脚本
└── uploads/             # 上传文件目录（运行时生成）
```

---

## 🚀 快速开始

### 环境要求
- JDK 8+
- MySQL 8.0+
- Redis 7.x+
- Maven 3.6+

### 1. 创建数据库

```sql
CREATE DATABASE sky_take_out DEFAULT CHARACTER SET utf8mb4;
```

然后执行 `db/init.sql` 脚本建表。

### 2. 配置

复制配置文件模板，按需修改：

```bash
# Windows
copy sky-server\src\main\resources\application-dev.yml.example sky-server\src\main\resources\application-dev.yml

# Mac/Linux
cp sky-server/src/main/resources/application-dev.yml.example sky-server/src/main/resources/application-dev.yml
```

编辑 `application-dev.yml`，修改数据库连接信息、Redis 连接信息等。

### 3. 启动

```bash
# 安装依赖
mvn clean install -DskipTests

# 启动服务
cd sky-server
mvn spring-boot:run
```

### 4. 访问

| 入口 | 地址 |
|------|------|
| API 文档 | http://localhost:8080/doc.html |
| 管理端登录 | http://localhost:8080/admin/employee/login |
| 管理端默认账号 | admin / 123456 |

---

## 📖 API 文档

项目启动后访问 [http://localhost:8080/doc.html](http://localhost:8080/doc.html) 查看完整的 Swagger 接口文档。

---

## ⚠️ 注意事项

- 本项目的支付模块使用 **模拟数据**，适合学习和演示，生产环境请对接真实支付接口
- 微信登录在 `dev` 环境下可通过 `/user/user/dev-login` 接口模拟登录
- `application-dev.yml` 包含敏感配置，已加入 `.gitignore`，请勿提交到公共仓库
- 生产环境请使用 `application-prod.yml` 并设置 `spring.profiles.active=prod`

---

## 📄 License

本项目仅供学习交流使用。