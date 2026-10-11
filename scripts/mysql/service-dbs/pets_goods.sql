-- ============================================================
-- pets_goods — goods-service 独立库
-- 表: goods_category / goods / goods_sku / service_category / service
-- 来源: 06-m4-ddl.sql (§1~§5) + 09-seed-data.sql (§1~§5)
-- ============================================================
CREATE DATABASE IF NOT EXISTS `pets_goods` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pets_goods`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. goods_category — 商品分类表
-- ============================================================
DROP TABLE IF EXISTS `goods_category`;
CREATE TABLE `goods_category` (
    `id`           BIGINT       NOT NULL              COMMENT '主键ID',
    `shop_id`      BIGINT       NOT NULL              COMMENT '租户ID',
    `parent_id`    BIGINT       NOT NULL DEFAULT 0    COMMENT '父分类ID(0=根)',
    `name`         VARCHAR(50)  NOT NULL              COMMENT '分类名',
    `sort`         INT          NOT NULL DEFAULT 0    COMMENT '排序',
    `version`      INT          NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_parent` (`shop_id`, `parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- ============================================================
-- 2. goods — 商品表
-- ============================================================
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods` (
    `id`           BIGINT        NOT NULL              COMMENT '主键ID (Redis号段)',
    `shop_id`      BIGINT        NOT NULL              COMMENT '租户ID',
    `category_id`  BIGINT        DEFAULT NULL          COMMENT '分类ID',
    `name`         VARCHAR(200)  NOT NULL              COMMENT '商品名',
    `barcode`      VARCHAR(64)   DEFAULT NULL          COMMENT '条码',
    `unit`         VARCHAR(20)   DEFAULT NULL          COMMENT '单位',
    `retail_price` DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '零售价',
    `cost_price`   DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '成本价',
    `pic`          VARCHAR(255)  DEFAULT NULL          COMMENT '主图',
    `status`       TINYINT       NOT NULL DEFAULT 1    COMMENT '1上架 0下架',
    `version`      INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_category` (`shop_id`, `category_id`),
    INDEX `idx_barcode` (`barcode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================================
-- 3. goods_sku — 商品SKU表
-- ============================================================
DROP TABLE IF EXISTS `goods_sku`;
CREATE TABLE `goods_sku` (
    `id`           BIGINT        NOT NULL              COMMENT '主键ID',
    `goods_id`     BIGINT        NOT NULL              COMMENT '商品ID',
    `spec_name`    VARCHAR(100)  DEFAULT NULL          COMMENT '规格描述',
    `price`        DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '规格价',
    `version`      INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品SKU表';

-- ============================================================
-- 4. service_category — 服务分类表
-- ============================================================
DROP TABLE IF EXISTS `service_category`;
CREATE TABLE `service_category` (
    `id`           BIGINT       NOT NULL              COMMENT '主键ID',
    `shop_id`      BIGINT       NOT NULL              COMMENT '租户ID',
    `name`         VARCHAR(50)  NOT NULL              COMMENT '分类名',
    `sort`         INT          NOT NULL DEFAULT 0    COMMENT '排序',
    `version`      INT          NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务分类表';

-- ============================================================
-- 5. service — 服务项目表
-- ============================================================
DROP TABLE IF EXISTS `service`;
CREATE TABLE `service` (
    `id`           BIGINT        NOT NULL              COMMENT '主键ID (Redis号段)',
    `shop_id`      BIGINT        NOT NULL              COMMENT '租户ID',
    `category_id`  BIGINT        DEFAULT NULL          COMMENT '服务分类ID',
    `name`         VARCHAR(200)  NOT NULL              COMMENT '服务名',
    `price`        DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '价格',
    `duration`     INT           DEFAULT 0             COMMENT '时长(分钟)',
    `description`  VARCHAR(500)  DEFAULT NULL          COMMENT '描述',
    `can_card`     TINYINT       NOT NULL DEFAULT 0    COMMENT '是否可用次卡 0否 1是',
    `status`       TINYINT       NOT NULL DEFAULT 1    COMMENT '1上架 0下架',
    `version`      INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_category` (`shop_id`, `category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务项目表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 种子数据 (09-seed-data.sql §1~§5)
-- ============================================================
INSERT INTO `goods_category` (`id`, `shop_id`, `parent_id`, `name`, `sort`) VALUES
(100001, 80351, 0, '食品', 1),
(100002, 80351, 0, '用品', 2),
(100003, 80351, 0, '玩具', 3);

INSERT INTO `goods` (`id`, `shop_id`, `category_id`, `name`, `barcode`, `unit`, `retail_price`, `cost_price`, `status`) VALUES
(100001, 80351, 100001, '皇家成猫粮 2kg',  '6901234500011', '袋', 168.00, 120.00, 1),
(100002, 80351, 100001, '皇家幼犬粮 2kg',  '6901234500028', '袋', 188.00, 135.00, 1),
(100003, 80351, 100002, '膨润土猫砂 10L',  '6901234500035', '包',  58.00,  38.00, 1),
(100004, 80351, 100002, '宠物牵引绳 1.2米', '6901234500042', '条',  45.00,  25.00, 1),
(100005, 80351, 100003, '逗猫棒',          '6901234500059', '根',  18.00,   8.00, 1),
(100006, 80351, 100003, '宠物飞盘',         '6901234500066', '个',  25.00,  12.00, 1);

INSERT INTO `goods_sku` (`id`, `goods_id`, `spec_name`, `price`) VALUES
(100001, 100001, '鸡肉味 2kg', 168.00),
(100002, 100001, '鱼肉味 2kg', 178.00),
(100003, 100002, '牛肉味 2kg', 188.00),
(100004, 100003, '10L 无香型',  58.00),
(100005, 100004, '1.2米 红色',  45.00),
(100006, 100005, '标准款',      18.00),
(100007, 100006, '标准款 蓝色', 25.00);

INSERT INTO `service_category` (`id`, `shop_id`, `name`, `sort`) VALUES
(100001, 80351, '洗澡美容', 1),
(100002, 80351, '医疗保健', 2);

INSERT INTO `service` (`id`, `shop_id`, `category_id`, `name`, `price`, `duration`, `description`, `can_card`, `status`) VALUES
(100001, 80351, 100001, '小型犬洗护',     80.00,  60, '含洗浴、吹干、基础梳理', 1, 1),
(100002, 80351, 100001, '猫咪基础美容',   120.00, 90, '含洗浴、吹干、指甲修剪', 1, 1),
(100003, 80351, 100002, '疫苗接种',       150.00, 30, '犬猫常规疫苗注射',       0, 1),
(100004, 80351, 100002, '体内驱虫',        60.00, 20, '内服驱虫药',             0, 1);
