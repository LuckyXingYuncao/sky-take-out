-- ============================================
-- 食光外卖平台 - 数据库初始化脚本
-- 使用前请先创建数据库：CREATE DATABASE sky_take_out;
-- ============================================

USE sky_take_out;

-- ----------------------------
-- 员工表
-- ----------------------------
CREATE TABLE IF NOT EXISTS employee (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(32) NOT NULL COMMENT '姓名',
    username VARCHAR(32) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(64) NOT NULL COMMENT '密码',
    phone VARCHAR(11) NOT NULL COMMENT '手机号',
    sex VARCHAR(2) NOT NULL COMMENT '性别',
    id_number VARCHAR(18) NOT NULL COMMENT '身份证号',
    status INT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_user BIGINT COMMENT '创建人',
    update_user BIGINT COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='员工信息';

-- 默认管理员密码为 e10adc3949ba59abbe56e057f20f883e (MD5加密的123456)
INSERT INTO employee (id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user)
VALUES (1, '管理员', 'admin', 'e10adc3949ba59abbe56e057f20f883e', '13812345678', '1', '110101199001010001', 1, NOW(), NOW(), 1, 1);

-- ----------------------------
-- 分类表
-- ----------------------------
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    type INT COMMENT '类型 1:菜品分类 2:套餐分类',
    name VARCHAR(32) NOT NULL COMMENT '分类名称',
    sort INT DEFAULT 0 COMMENT '排序',
    status INT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_user BIGINT COMMENT '创建人',
    update_user BIGINT COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品及套餐分类';

-- ----------------------------
-- 菜品表
-- ----------------------------
CREATE TABLE IF NOT EXISTS dish (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(32) NOT NULL COMMENT '菜品名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    image VARCHAR(255) COMMENT '图片',
    description VARCHAR(255) COMMENT '描述',
    status INT DEFAULT 1 COMMENT '状态 0:停售 1:起售',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_user BIGINT COMMENT '创建人',
    update_user BIGINT COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品';

-- ----------------------------
-- 菜品口味表
-- ----------------------------
CREATE TABLE IF NOT EXISTS dish_flavor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    name VARCHAR(32) COMMENT '口味名称',
    value VARCHAR(255) COMMENT '口味值列表',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_user BIGINT COMMENT '创建人',
    update_user BIGINT COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品口味关系';

-- ----------------------------
-- 套餐表
-- ----------------------------
CREATE TABLE IF NOT EXISTS setmeal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    name VARCHAR(32) NOT NULL COMMENT '套餐名称',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    image VARCHAR(255) COMMENT '图片',
    description VARCHAR(255) COMMENT '描述',
    status INT DEFAULT 1 COMMENT '状态 0:停售 1:起售',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_user BIGINT COMMENT '创建人',
    update_user BIGINT COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐';

-- ----------------------------
-- 套餐菜品关系表
-- ----------------------------
CREATE TABLE IF NOT EXISTS setmeal_dish (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    setmeal_id BIGINT NOT NULL COMMENT '套餐ID',
    dish_id BIGINT NOT NULL COMMENT '菜品ID',
    name VARCHAR(32) COMMENT '菜品名称(冗余)',
    price DECIMAL(10,2) COMMENT '菜品原价(冗余)',
    copies INT DEFAULT 1 COMMENT '份数',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_user BIGINT COMMENT '创建人',
    update_user BIGINT COMMENT '修改人'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='套餐菜品关系';

-- ----------------------------
-- 用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    openid VARCHAR(45) COMMENT '微信openid',
    name VARCHAR(32) COMMENT '昵称',
    phone VARCHAR(11) COMMENT '手机号',
    sex VARCHAR(2) COMMENT '性别',
    id_number VARCHAR(18) COMMENT '身份证号',
    avatar VARCHAR(500) COMMENT '头像',
    create_time DATETIME COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户信息';

-- ----------------------------
-- 地址簿表
-- ----------------------------
CREATE TABLE IF NOT EXISTS address_book (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    consignee VARCHAR(50) COMMENT '收货人',
    sex VARCHAR(2) COMMENT '性别',
    phone VARCHAR(11) COMMENT '手机号',
    province_code VARCHAR(12) COMMENT '省份编码',
    province_name VARCHAR(32) COMMENT '省份名称',
    city_code VARCHAR(12) COMMENT '城市编码',
    city_name VARCHAR(32) COMMENT '城市名称',
    district_code VARCHAR(12) COMMENT '区县编码',
    district_name VARCHAR(32) COMMENT '区县名称',
    detail VARCHAR(200) COMMENT '详细地址',
    label VARCHAR(100) COMMENT '标签',
    is_default TINYINT(1) DEFAULT 0 COMMENT '是否默认 0:否 1:是',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    create_user BIGINT COMMENT '创建人',
    update_user BIGINT COMMENT '修改人',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '是否删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='地址簿';

-- ----------------------------
-- 购物车表
-- ----------------------------
CREATE TABLE IF NOT EXISTS shopping_cart (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(32) COMMENT '商品名称',
    image VARCHAR(255) COMMENT '图片',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    dish_id BIGINT COMMENT '菜品ID',
    setmeal_id BIGINT COMMENT '套餐ID',
    dish_flavor VARCHAR(50) COMMENT '口味',
    number INT DEFAULT 1 COMMENT '数量',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额',
    create_time DATETIME COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车';

-- ----------------------------
-- 订单表
-- ----------------------------
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    number VARCHAR(50) COMMENT '订单号',
    status INT DEFAULT 1 COMMENT '状态 1:待付款 2:待确认 3:已确认 4:派送中 5:已完成 6:已取消',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    address_book_id BIGINT COMMENT '地址ID',
    order_time DATETIME COMMENT '下单时间',
    checkout_time DATETIME COMMENT '支付时间',
    pay_method INT COMMENT '支付方式 1:微信 2:支付宝',
    pay_status INT COMMENT '支付状态 0:未支付 1:已支付 2:已退款',
    amount DECIMAL(10,2) COMMENT '实收金额',
    remark VARCHAR(100) COMMENT '备注',
    phone VARCHAR(11) COMMENT '手机号',
    address VARCHAR(255) COMMENT '地址',
    user_name VARCHAR(32) COMMENT '用户名',
    consignee VARCHAR(50) COMMENT '收货人',
    cancel_reason VARCHAR(255) COMMENT '取消原因',
    rejection_reason VARCHAR(255) COMMENT '拒单原因',
    cancel_time DATETIME COMMENT '取消时间',
    estimated_delivery_time DATETIME COMMENT '预计送达时间',
    delivery_status INT COMMENT '配送状态 1:立即送出 0:选择具体时间',
    delivery_time DATETIME COMMENT '送达时间',
    pack_amount INT DEFAULT 0 COMMENT '打包费',
    tableware_number INT DEFAULT 1 COMMENT '餐具数量',
    tableware_status INT DEFAULT 1 COMMENT '餐具数量状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ----------------------------
-- 订单详情表
-- ----------------------------
CREATE TABLE IF NOT EXISTS order_detail (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    name VARCHAR(32) COMMENT '商品名称',
    image VARCHAR(255) COMMENT '图片',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    dish_id BIGINT COMMENT '菜品ID',
    setmeal_id BIGINT COMMENT '套餐ID',
    dish_flavor VARCHAR(50) COMMENT '口味',
    number INT DEFAULT 1 COMMENT '数量',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细';