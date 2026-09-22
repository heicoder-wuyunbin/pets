-- ============================================================
-- 宠老板复刻 · M4 商品与库存 · MySQL DDL
-- 模块: goods-service / stock-service
-- 主键: Redis 号段发号器生成 bigint
-- 乐观锁: version 字段, MyBatis-Plus @Version
-- ============================================================

USE `pets`;

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

-- ============================================================
-- 6. stock — 库存表
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
-- 7. stock_in — 入库单表
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
-- 8. stock_in_item — 入库明细表
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
-- 9. stock_out — 出库单表
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
-- 10. stock_out_item — 出库明细表
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
