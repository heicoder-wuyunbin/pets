-- ============================================================
-- 宠老板复刻 · M6 营销与数据统计 · MySQL DDL
-- 模块: market-service / report-service
-- 主键: Redis 号段发号器生成 bigint
-- 乐观锁: version 字段, MyBatis-Plus @Version
-- ============================================================

USE `pets`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. marketing_activity — 营销活动表
-- ============================================================
DROP TABLE IF EXISTS `marketing_activity`;
CREATE TABLE `marketing_activity` (
    `id`              BIGINT        NOT NULL              COMMENT '主键ID',
    `shop_id`         BIGINT        NOT NULL              COMMENT '租户ID',
    `name`            VARCHAR(100)  NOT NULL              COMMENT '活动名',
    `type`            TINYINT       NOT NULL              COMMENT '1满减 2折扣',
    `threshold`       DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '满减门槛(满X元)/折扣基础',
    `discount_value`  DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '减额(满减) 或 折扣率(0-1)',
    `goods_ids`       VARCHAR(500)  DEFAULT NULL          COMMENT '限定商品ID(逗号分隔,空=全部)',
    `start_time`      DATETIME      DEFAULT NULL          COMMENT '开始时间',
    `end_time`        DATETIME      DEFAULT NULL          COMMENT '结束时间',
    `status`          TINYINT       NOT NULL DEFAULT 1    COMMENT '1启用 0停用',
    `version`         INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_status` (`shop_id`, `status`),
    INDEX `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营销活动表';

-- ============================================================
-- 2. daily_report — 营业日报表
-- ============================================================
DROP TABLE IF EXISTS `daily_report`;
CREATE TABLE `daily_report` (
    `id`             BIGINT        NOT NULL              COMMENT '主键ID',
    `shop_id`        BIGINT        NOT NULL              COMMENT '租户ID',
    `report_date`    DATE          NOT NULL              COMMENT '报表日期',
    `sale_amount`    DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '销售额',
    `order_count`    INT           NOT NULL DEFAULT 0    COMMENT '订单数',
    `refund_amount`  DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '退款额',
    `avg_order`      DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '客单价',
    `member_count`   INT           NOT NULL DEFAULT 0    COMMENT '消费会员数',
    `new_member`     INT           NOT NULL DEFAULT 0    COMMENT '新增会员数',
    `version`        INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shop_date` (`shop_id`, `report_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='营业日报表';

SET FOREIGN_KEY_CHECKS = 1;
