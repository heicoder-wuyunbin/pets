-- ============================================================
-- pets_market — market-service 独立库
-- 表: marketing_activity
-- 来源: 08-m6-ddl.sql (§1) + 09-seed-data.sql (§20)
-- ============================================================
CREATE DATABASE IF NOT EXISTS `pets_market` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pets_market`;

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

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 种子数据 (09-seed-data.sql §20)
-- ============================================================
INSERT INTO `marketing_activity` (`id`, `shop_id`, `name`, `type`, `threshold`, `discount_value`, `goods_ids`, `start_time`, `end_time`, `status`) VALUES
(100001, 80351, '秋季满100减10',     1, 100.00,  10.00, NULL,                '2026-09-01 00:00:00', '2026-09-30 23:59:59', 1),
(100002, 80351, '全场9折(限时)',     2,   0.00,   0.90, NULL,                '2026-09-15 00:00:00', '2026-09-25 23:59:59', 1),
(100003, 80351, '双十一预热满200减30',1, 200.00,  30.00, '100001,100002',     '2026-10-20 00:00:00', '2026-11-11 23:59:59', 0);
