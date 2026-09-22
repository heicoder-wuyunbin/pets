-- ============================================================
-- 宠老板复刻 · M1 登录鉴权 · MySQL DDL
-- 模块: auth-service
-- 字符集: utf8mb4 / 引擎: InnoDB
-- 主键: Redis 号段发号器生成 bigint
-- 乐观锁: version 字段, MyBatis-Plus @Version
-- ============================================================

-- 库已由 docker-compose MYSQL_DATABASE=pets 创建, 此处仅 USE
USE `pets`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. chain_group — 连锁集团表（顶层租户）
-- ============================================================
DROP TABLE IF EXISTS `chain_group`;
CREATE TABLE `chain_group` (
    `id`            BIGINT       NOT NULL              COMMENT '集团ID (Redis号段)',
    `group_name`    VARCHAR(100) NOT NULL              COMMENT '集团名称',
    `contact_name`  VARCHAR(50)  DEFAULT NULL          COMMENT '联系人',
    `contact_phone` VARCHAR(20)  DEFAULT NULL          COMMENT '联系电话',
    `status`        TINYINT      NOT NULL DEFAULT 1    COMMENT '0禁用 1启用',
    `version`       INT          NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='连锁集团表';

-- ============================================================
-- 2. shop — 店铺表
-- ============================================================
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop` (
    `id`              BIGINT       NOT NULL              COMMENT '店铺ID (shop_id)',
    `chain_group_id`  BIGINT       NOT NULL              COMMENT '所属集团',
    `shop_name`       VARCHAR(100) NOT NULL              COMMENT '店铺名称',
    `level`           INT          NOT NULL DEFAULT 1    COMMENT '版本等级',
    `expire_time`     DATETIME     DEFAULT NULL          COMMENT '到期时间',
    `status`          TINYINT      NOT NULL DEFAULT 1    COMMENT '0禁用 1启用',
    `version`         INT          NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_chain_group_id` (`chain_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺表';

-- ============================================================
-- 3. account — 登录账号表（人维度）
--    手机号全局唯一, 一人一号, 跨集团复用
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
-- 4. role — 角色表
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
-- 5. clerk — 员工雇佣关系表（岗位维度）
--    一人可在多集团/多店任职, 离职再入职新建记录
--    复合唯一索引: (account_id, shop_id, status) 控制同一人同一店仅一条在职
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
