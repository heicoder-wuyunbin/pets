-- ============================================================
-- pets_stock — stock-service 独立库
-- 表: stock / stock_in / stock_in_item / stock_out / stock_out_item
-- 来源: 06-m4-ddl.sql (§6~§10) + 09-seed-data.sql (§6)
-- ============================================================
CREATE DATABASE IF NOT EXISTS `pets_stock` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pets_stock`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. stock — 库存表
-- ============================================================
DROP TABLE IF EXISTS `stock`;
CREATE TABLE `stock` (
    `id`           BIGINT        NOT NULL              COMMENT '主键ID',
    `shop_id`      BIGINT        NOT NULL              COMMENT '租户ID',
    `goods_id`     BIGINT        NOT NULL              COMMENT '商品ID',
    `sku_id`       BIGINT        NOT NULL DEFAULT 0    COMMENT 'SKU ID(0=无规格)',
    `quantity`     INT           NOT NULL DEFAULT 0    COMMENT '当前库存',
    `warn_qty`     INT           NOT NULL DEFAULT 0    COMMENT '预警阈值',
    `version`      INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shop_goods_sku` (`shop_id`, `goods_id`, `sku_id`),
    INDEX `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='库存表';

-- ============================================================
-- 2. stock_in — 入库单表
-- ============================================================
DROP TABLE IF EXISTS `stock_in`;
CREATE TABLE `stock_in` (
    `id`           BIGINT        NOT NULL              COMMENT '主键ID',
    `shop_id`      BIGINT        NOT NULL              COMMENT '租户ID',
    `supplier_id`  BIGINT        DEFAULT NULL          COMMENT '供应商ID(可空)',
    `supplier_name` VARCHAR(100) DEFAULT NULL          COMMENT '供应商名称',
    `total`        DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '总成本',
    `clerk_id`     BIGINT        DEFAULT NULL          COMMENT '经办人ID',
    `remark`       VARCHAR(255)  DEFAULT NULL          COMMENT '备注',
    `status`       TINYINT       NOT NULL DEFAULT 1    COMMENT '1已入库',
    `version`      INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_time` (`shop_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库单表';

-- ============================================================
-- 3. stock_in_item — 入库明细表
-- ============================================================
DROP TABLE IF EXISTS `stock_in_item`;
CREATE TABLE `stock_in_item` (
    `id`           BIGINT        NOT NULL              COMMENT '主键ID',
    `in_id`        BIGINT        NOT NULL              COMMENT '入库单ID',
    `goods_id`     BIGINT        NOT NULL              COMMENT '商品ID',
    `sku_id`       BIGINT        NOT NULL DEFAULT 0    COMMENT 'SKU ID',
    `goods_name`   VARCHAR(200)  NOT NULL              COMMENT '商品名(快照)',
    `qty`          INT           NOT NULL DEFAULT 0    COMMENT '数量',
    `cost`         DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '成本单价',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_in_id` (`in_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='入库明细表';

-- ============================================================
-- 4. stock_out — 出库单表
-- ============================================================
DROP TABLE IF EXISTS `stock_out`;
CREATE TABLE `stock_out` (
    `id`           BIGINT        NOT NULL              COMMENT '主键ID',
    `shop_id`      BIGINT        NOT NULL              COMMENT '租户ID',
    `out_type`     VARCHAR(20)   NOT NULL              COMMENT '出库类型:退货/报损/领用',
    `total`        DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '总金额',
    `clerk_id`     BIGINT        DEFAULT NULL          COMMENT '经办人ID',
    `remark`       VARCHAR(255)  DEFAULT NULL          COMMENT '备注',
    `status`       TINYINT       NOT NULL DEFAULT 1    COMMENT '1已出库',
    `version`      INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_time` (`shop_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库单表';

-- ============================================================
-- 5. stock_out_item — 出库明细表
-- ============================================================
DROP TABLE IF EXISTS `stock_out_item`;
CREATE TABLE `stock_out_item` (
    `id`           BIGINT        NOT NULL              COMMENT '主键ID',
    `out_id`       BIGINT        NOT NULL              COMMENT '出库单ID',
    `goods_id`     BIGINT        NOT NULL              COMMENT '商品ID',
    `sku_id`       BIGINT        NOT NULL DEFAULT 0    COMMENT 'SKU ID',
    `goods_name`   VARCHAR(200)  NOT NULL              COMMENT '商品名(快照)',
    `qty`          INT           NOT NULL DEFAULT 0    COMMENT '数量',
    `cost`         DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '成本单价',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_out_id` (`out_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='出库明细表';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 种子数据 (09-seed-data.sql §6)
-- ============================================================
INSERT INTO `stock` (`id`, `shop_id`, `goods_id`, `sku_id`, `quantity`, `warn_qty`) VALUES
(100001, 80351, 100001, 100001, 50, 10),
(100002, 80351, 100001, 100002, 30,  5),
(100003, 80351, 100002, 100003, 25,  5),
(100004, 80351, 100003, 100004, 80, 15),
(100005, 80351, 100004, 100005, 15,  5),
(100006, 80351, 100005, 100006, 100, 20),
(100007, 80351, 100006, 100007, 60, 10);
