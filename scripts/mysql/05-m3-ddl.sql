-- ============================================================
-- 宠老板复刻 · M3 收银台+订单管理 · MySQL DDL
-- 模块: trade-service
-- 主键: Redis 号段发号器生成 bigint
-- 乐观锁: version 字段, MyBatis-Plus @Version
-- ============================================================

USE `pets`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. sale_order — 销售订单表
-- ============================================================
DROP TABLE IF EXISTS `sale_order`;
CREATE TABLE `sale_order` (
    `id`            BIGINT        NOT NULL              COMMENT '主键ID (Redis号段)',
    `shop_id`       BIGINT        NOT NULL              COMMENT '租户ID',
    `order_no`      VARCHAR(32)   NOT NULL              COMMENT '订单号',
    `member_id`     BIGINT        DEFAULT NULL          COMMENT '会员ID(可空=散客)',
    `member_name`   VARCHAR(50)   DEFAULT NULL          COMMENT '会员姓名(快照)',
    `clerk_id`      BIGINT        NOT NULL              COMMENT '收银员ID',
    `clerk_name`    VARCHAR(50)   DEFAULT NULL          COMMENT '收银员姓名(快照)',
    `total`         DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '订单合计',
    `discount`      DECIMAL(5,2)  NOT NULL DEFAULT 100  COMMENT '折扣率(%)',
    `payable`       DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '应付金额',
    `actual`        DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '实付金额',
    `pay_type`      VARCHAR(20)   DEFAULT NULL          COMMENT '支付方式:cash/wechat/alipay/card',
    `status`        TINYINT       NOT NULL DEFAULT 0    COMMENT '0待支付 1已完成 2已取消 3退款中 4已退款',
    `remark`        VARCHAR(255)  DEFAULT NULL          COMMENT '备注',
    `version`       INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    INDEX `idx_shop_status_time` (`shop_id`, `status`, `create_time`),
    INDEX `idx_member` (`member_id`),
    INDEX `idx_clerk` (`clerk_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售订单表';

-- ============================================================
-- 2. order_item — 订单明细表
-- ============================================================
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id`            BIGINT        NOT NULL              COMMENT '主键ID',
    `order_id`      BIGINT        NOT NULL              COMMENT '订单ID',
    `item_type`     TINYINT       NOT NULL DEFAULT 1    COMMENT '1商品 2服务',
    `item_id`       BIGINT        NOT NULL              COMMENT '商品ID或服务ID',
    `item_name`     VARCHAR(200)  NOT NULL              COMMENT '商品/服务名称(快照)',
    `sku_id`        BIGINT        DEFAULT NULL          COMMENT 'SKU ID(商品)',
    `price`         DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '单价',
    `qty`           INT           NOT NULL DEFAULT 1    COMMENT '数量',
    `discount`      DECIMAL(5,2)  NOT NULL DEFAULT 100  COMMENT '折扣率(%)',
    `amount`        DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '小计金额',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表';

-- ============================================================
-- 3. payment — 支付流水表
-- ============================================================
DROP TABLE IF EXISTS `payment`;
CREATE TABLE `payment` (
    `id`            BIGINT        NOT NULL              COMMENT '主键ID',
    `shop_id`       BIGINT        NOT NULL              COMMENT '租户ID',
    `order_id`      BIGINT        NOT NULL              COMMENT '订单ID',
    `order_no`      VARCHAR(32)   NOT NULL              COMMENT '订单号(冗余)',
    `pay_type`      VARCHAR(20)   NOT NULL              COMMENT '支付方式:cash/wechat/alipay/card',
    `amount`        DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '金额',
    `status`        TINYINT       NOT NULL DEFAULT 1    COMMENT '1成功 0失败',
    `biz_no`        VARCHAR(64)   DEFAULT NULL          COMMENT '外部流水号',
    `is_refund`     TINYINT       NOT NULL DEFAULT 0    COMMENT '是否退款 0否 1是',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_time` (`shop_id`, `create_time`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付流水表';

-- ============================================================
-- 4. cashbook — 现金流水表
-- ============================================================
DROP TABLE IF EXISTS `cashbook`;
CREATE TABLE `cashbook` (
    `id`            BIGINT        NOT NULL              COMMENT '主键ID',
    `shop_id`       BIGINT        NOT NULL              COMMENT '租户ID',
    `type`          TINYINT       NOT NULL              COMMENT '1收入 2支出',
    `amount`        DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '金额',
    `biz_type`      VARCHAR(20)   NOT NULL              COMMENT '业务类型:收款/找零/提现/退款',
    `order_id`      BIGINT        DEFAULT NULL          COMMENT '关联订单ID',
    `clerk_id`      BIGINT        DEFAULT NULL          COMMENT '经办人ID',
    `memo`          VARCHAR(255)  DEFAULT NULL          COMMENT '备注',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_time` (`shop_id`, `create_time`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='现金流水表';

-- ============================================================
-- 5. work_order — 服务单表
-- ============================================================
DROP TABLE IF EXISTS `work_order`;
CREATE TABLE `work_order` (
    `id`            BIGINT        NOT NULL              COMMENT '主键ID',
    `shop_id`       BIGINT        NOT NULL              COMMENT '租户ID',
    `order_id`      BIGINT        NOT NULL              COMMENT '来源订单ID',
    `order_item_id` BIGINT        NOT NULL              COMMENT '订单明细ID',
    `service_id`    BIGINT        NOT NULL              COMMENT '服务项目ID',
    `service_name`  VARCHAR(200)  NOT NULL              COMMENT '服务名称(快照)',
    `clerk_id`      BIGINT        DEFAULT NULL          COMMENT '技师ID',
    `status`        TINYINT       NOT NULL DEFAULT 0    COMMENT '0待服务 1服务中 2已完成 3已取消',
    `remark`        TEXT          DEFAULT NULL          COMMENT '备注',
    `pics`          TEXT          DEFAULT NULL          COMMENT '照片(JSON数组)',
    `start_time`    DATETIME      DEFAULT NULL          COMMENT '开始服务时间',
    `finish_time`   DATETIME      DEFAULT NULL          COMMENT '完成时间',
    `version`       INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_status` (`shop_id`, `status`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_clerk` (`clerk_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务单表';

SET FOREIGN_KEY_CHECKS = 1;
