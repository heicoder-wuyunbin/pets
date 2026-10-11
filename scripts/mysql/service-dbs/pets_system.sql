-- ============================================================
-- pets_system — system-service 独立库
-- 自有表: sys_param / operation_log
-- 跨服务只读复制表: account / role / clerk (auth-service)
-- 来源: 03-m2-ddl + 01-auth-ddl + 02/04 种子
-- ============================================================
CREATE DATABASE IF NOT EXISTS `pets_system` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pets_system`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. sys_param — 系统参数配置表
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
-- 2. operation_log — 操作日志表
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
-- 3. account — 登录账号表（跨服务只读复制）
-- ============================================================
DROP TABLE IF EXISTS `account`;
CREATE TABLE `account` (
    `id`          BIGINT       NOT NULL              COMMENT '账号ID (Redis号段)',
    `phone`       VARCHAR(20)  NOT NULL              COMMENT '登录手机号(+86)',
    `password`    VARCHAR(100) NOT NULL              COMMENT 'BCrypt密文',
    `truename`    VARCHAR(50)  DEFAULT NULL          COMMENT '真实姓名',
    `status`      TINYINT      NOT NULL DEFAULT 1    COMMENT '0禁用 1启用',
    `version`     INT          NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录账号表(人维度)';

-- ============================================================
-- 4. role — 角色表（跨服务只读复制）
-- ============================================================
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
    `id`          BIGINT      NOT NULL              COMMENT '角色ID (Redis号段)',
    `shop_id`     BIGINT      NOT NULL DEFAULT 0   COMMENT '所属店铺(0=系统内置)',
    `role_name`   VARCHAR(50) NOT NULL              COMMENT '角色名称',
    `role_code`   VARCHAR(50) NOT NULL              COMMENT '角色编码',
    `is_system`   TINYINT     NOT NULL DEFAULT 0    COMMENT '是否系统内置(不可删)',
    `status`      TINYINT     NOT NULL DEFAULT 1    COMMENT '0禁用 1启用',
    `version`     INT         NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shop_role_code` (`shop_id`, `role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ============================================================
-- 5. clerk — 员工雇佣关系表（跨服务只读复制）
-- ============================================================
DROP TABLE IF EXISTS `clerk`;
CREATE TABLE `clerk` (
    `id`              BIGINT      NOT NULL              COMMENT '雇佣记录ID (Redis号段)',
    `account_id`      BIGINT      NOT NULL              COMMENT '关联账号',
    `shop_id`         BIGINT      NOT NULL              COMMENT '所属店铺',
    `chain_group_id`  BIGINT      NOT NULL              COMMENT '所属集团(冗余, 签发JWT免关联)',
    `role_id`         BIGINT      NOT NULL              COMMENT '角色ID',
    `is_super`        TINYINT     NOT NULL DEFAULT 0    COMMENT '是否该店超级管理员',
    `status`          TINYINT     NOT NULL DEFAULT 1    COMMENT '0离职 1在职',
    `hire_date`       DATE        DEFAULT NULL          COMMENT '入职日期',
    `leave_date`      DATE        DEFAULT NULL          COMMENT '离职日期',
    `version`         INT         NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_account_shop_active` (`account_id`, `shop_id`, `status`),
    INDEX `idx_account_id` (`account_id`),
    INDEX `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='员工雇佣关系表(岗位维度)';

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 种子数据
-- ============================================================
-- sys_param (04-m2-seed §1)
INSERT INTO `sys_param` (`id`, `shop_id`, `group_key`, `param_key`, `param_value`, `remark`) VALUES
(1, 0, 'cashier', 'default_discount', '1.0', '收银台默认折扣'),
(2, 0, 'cashier', 'allow_negative_stock', '0', '是否允许负库存销售'),
(3, 0, 'sms', 'sms_enabled', '1', '是否启用短信通知'),
(4, 0, 'appointment', 'morning_start', '09:00', '预约早间开始时间'),
(5, 0, 'appointment', 'evening_end', '21:00', '预约晚间结束时间'),
(6, 0, 'member', 'register_gift_integral', '100', '会员注册赠送积分'),
(7, 0, 'stock', 'shortage_threshold', '10', '库存不足预警阈值');

-- account / role / clerk (02-auth-seed)
INSERT INTO `account` (`id`, `phone`, `password`, `truename`, `status`)
VALUES (10001, '15638538859', '$2a$10$N9qo8uLOick3lxQwCh6nXu.fJ0o5KZQxLm4uU5p6o3m4Z7m8n9o0', '超级管理员', 1);

INSERT INTO `role` (`id`, `shop_id`, `role_name`, `role_code`, `is_system`, `status`) VALUES
(1, 0, '超级管理员', 'SUPER_ADMIN', 1, 1),
(2, 0, '店员',       'CLERK',       1, 1);

INSERT INTO `clerk` (`id`, `account_id`, `shop_id`, `chain_group_id`, `role_id`, `is_super`, `status`, `hire_date`)
VALUES (20001, 10001, 80351, 1, 1, 1, 1, '2026-09-01');
