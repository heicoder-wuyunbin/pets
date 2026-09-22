-- ============================================================
-- 宠老板复刻 · M2 店铺概况+基础配置 · MySQL DDL
-- 模块: shop-service / system-service
-- 主键: Redis 号段发号器生成 bigint
-- 乐观锁: version 字段, MyBatis-Plus @Version
-- ============================================================

USE `pets`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. 扩展 shop 表（门店管理所需字段）
-- ============================================================
ALTER TABLE `shop`
    ADD COLUMN `logo`        VARCHAR(255) DEFAULT NULL COMMENT '店铺Logo' AFTER `shop_name`,
    ADD COLUMN `bind_phone`  VARCHAR(20)  DEFAULT NULL COMMENT '绑定手机号' AFTER `logo`,
    ADD COLUMN `sms_remain`  INT          NOT NULL DEFAULT 0 COMMENT '短信余量' AFTER `bind_phone`,
    ADD COLUMN `addr`        VARCHAR(255) DEFAULT NULL COMMENT '店铺地址' AFTER `sms_remain`,
    ADD COLUMN `telephone`   VARCHAR(50)  DEFAULT NULL COMMENT '联系电话' AFTER `addr`;

-- ============================================================
-- 2. sys_param — 系统参数配置表
-- ============================================================
DROP TABLE IF EXISTS `sys_param`;
CREATE TABLE `sys_param` (
    `id`           BIGINT       NOT NULL              COMMENT '主键ID (Redis号段)',
    `shop_id`      BIGINT       NOT NULL DEFAULT 0    COMMENT '租户ID(0=全局默认)',
    `group_key`    VARCHAR(50)  NOT NULL              COMMENT '分组键',
    `param_key`    VARCHAR(100) NOT NULL              COMMENT '参数键',
    `param_value`  VARCHAR(500) DEFAULT NULL          COMMENT '参数值',
    `remark`       VARCHAR(255) DEFAULT NULL          COMMENT '说明',
    `version`      INT          NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shop_param_key` (`shop_id`, `param_key`),
    INDEX `idx_group_key` (`group_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统参数配置表';

-- ============================================================
-- 3. operation_log — 操作日志表
-- ============================================================
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id`           BIGINT       NOT NULL              COMMENT '主键ID (Redis号段)',
    `shop_id`      BIGINT       NOT NULL DEFAULT 0    COMMENT '租户ID',
    `user_id`      BIGINT       DEFAULT NULL          COMMENT '操作人ID',
    `user_name`    VARCHAR(50)  DEFAULT NULL          COMMENT '操作人姓名',
    `module`       VARCHAR(50)  NOT NULL              COMMENT '模块',
    `action`       VARCHAR(50)  NOT NULL              COMMENT '动作',
    `params`       TEXT         DEFAULT NULL          COMMENT '关键参数(脱敏)',
    `ip`           VARCHAR(50)  DEFAULT NULL          COMMENT '来源IP',
    `status`       TINYINT      NOT NULL DEFAULT 1    COMMENT '1成功 0失败',
    `fail_reason`  VARCHAR(500) DEFAULT NULL          COMMENT '失败原因',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_time` (`shop_id`, `create_time`),
    INDEX `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================================
-- 4. notice — 公告与行业资讯表
-- ============================================================
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
    `id`           BIGINT       NOT NULL              COMMENT '主键ID (Redis号段)',
    `shop_id`      BIGINT       NOT NULL DEFAULT 0    COMMENT '租户ID(0=公共)',
    `type`         TINYINT      NOT NULL DEFAULT 1    COMMENT '1公告 2行业资讯',
    `title`        VARCHAR(200) NOT NULL              COMMENT '标题',
    `content`      TEXT         DEFAULT NULL          COMMENT '内容',
    `status`       TINYINT      NOT NULL DEFAULT 1    COMMENT '0下架 1上架',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发布时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop_type_status` (`shop_id`, `type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告与行业资讯表';

SET FOREIGN_KEY_CHECKS = 1;
