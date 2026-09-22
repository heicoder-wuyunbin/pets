-- ============================================================
-- 宠老板复刻 · M5 会员中心 · MySQL DDL
-- 模块: member-service
-- 主键: Redis 号段发号器生成 bigint
-- 乐观锁: version 字段, MyBatis-Plus @Version
-- ============================================================

USE `pets`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. member — 会员表
-- ============================================================
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
    `id`            BIGINT        NOT NULL              COMMENT '主键ID',
    `shop_id`       BIGINT        NOT NULL              COMMENT '租户ID',
    `mobile`        VARCHAR(20)   NOT NULL              COMMENT '手机号',
    `name`          VARCHAR(50)   NOT NULL              COMMENT '姓名',
    `gender`        TINYINT       NOT NULL DEFAULT 0    COMMENT '性别 0未知 1男 2女',
    `birthday`      DATE          DEFAULT NULL          COMMENT '生日',
    `member_level`  INT           NOT NULL DEFAULT 1    COMMENT '会员等级',
    `tags`          VARCHAR(255)  DEFAULT NULL          COMMENT '标签(逗号分隔)',
    `remark`        VARCHAR(500)  DEFAULT NULL          COMMENT '备注',
    `version`       INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_shop_mobile` (`shop_id`, `mobile`),
    INDEX `idx_shop_name` (`shop_id`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员表';

-- ============================================================
-- 2. member_pet — 会员宠物档案表
-- ============================================================
DROP TABLE IF EXISTS `member_pet`;
CREATE TABLE `member_pet` (
    `id`           BIGINT        NOT NULL              COMMENT '主键ID',
    `member_id`    BIGINT        NOT NULL              COMMENT '会员ID',
    `pet_name`     VARCHAR(50)   NOT NULL              COMMENT '宠物名',
    `pet_type`     VARCHAR(20)   DEFAULT NULL          COMMENT '宠物类型(猫/狗)',
    `breed`        VARCHAR(50)   DEFAULT NULL          COMMENT '品种',
    `gender`       TINYINT       NOT NULL DEFAULT 0    COMMENT '性别 0未知 1公 2母',
    `weight`       DECIMAL(8,2)  DEFAULT NULL          COMMENT '体重(kg)',
    `birthday`     DATE          DEFAULT NULL          COMMENT '生日',
    `vaccine`      VARCHAR(255)  DEFAULT NULL          COMMENT '疫苗情况',
    `version`      INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员宠物档案表';

-- ============================================================
-- 3. member_account — 会员账户表(余额/积分)
-- ============================================================
DROP TABLE IF EXISTS `member_account`;
CREATE TABLE `member_account` (
    `id`               BIGINT        NOT NULL              COMMENT '主键ID',
    `member_id`        BIGINT        NOT NULL              COMMENT '会员ID',
    `balance`          DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '充值余额',
    `points`           INT           NOT NULL DEFAULT 0    COMMENT '积分',
    `total_consume`    DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '累计消费',
    `total_recharge`   DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '累计充值',
    `version`          INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员账户表';

-- ============================================================
-- 4. card_type — 卡种表
-- ============================================================
DROP TABLE IF EXISTS `card_type`;
CREATE TABLE `card_type` (
    `id`           BIGINT        NOT NULL              COMMENT '主键ID',
    `shop_id`      BIGINT        NOT NULL              COMMENT '租户ID',
    `name`         VARCHAR(50)   NOT NULL              COMMENT '卡名',
    `face_value`   DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '面额',
    `discount`     DECIMAL(4,2)  NOT NULL DEFAULT 1.00 COMMENT '折扣(如0.85=85折)',
    `valid_days`   INT           NOT NULL DEFAULT 0    COMMENT '有效期(天,0=永久)',
    `status`       TINYINT       NOT NULL DEFAULT 1    COMMENT '1启用 0停用',
    `version`      INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='卡种表';

-- ============================================================
-- 5. member_card — 会员卡表
-- ============================================================
DROP TABLE IF EXISTS `member_card`;
CREATE TABLE `member_card` (
    `id`             BIGINT        NOT NULL              COMMENT '主键ID',
    `member_id`      BIGINT        NOT NULL              COMMENT '会员ID',
    `card_type_id`   BIGINT        NOT NULL              COMMENT '卡种ID',
    `balance`        DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '卡内余额',
    `expire_time`    DATETIME      DEFAULT NULL          COMMENT '到期时间',
    `version`        INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员卡表';

-- ============================================================
-- 6. member_account_log — 会员账户流水表
-- ============================================================
DROP TABLE IF EXISTS `member_account_log`;
CREATE TABLE `member_account_log` (
    `id`           BIGINT        NOT NULL              COMMENT '主键ID',
    `member_id`    BIGINT        NOT NULL              COMMENT '会员ID',
    `type`         TINYINT       NOT NULL              COMMENT '1充值 2消费 3退款 4积分得 5积分抵',
    `amount`       DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '变动金额(正为增,负为减)',
    `balance_after` DECIMAL(12,2) NOT NULL DEFAULT 0   COMMENT '变动后余额',
    `points`       INT           NOT NULL DEFAULT 0    COMMENT '积分变动(正为增,负为减)',
    `points_after` INT           NOT NULL DEFAULT 0    COMMENT '变动后积分',
    `order_id`     BIGINT        DEFAULT NULL          COMMENT '关联订单ID',
    `remark`       VARCHAR(255)  DEFAULT NULL          COMMENT '备注',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_member_type` (`member_id`, `type`),
    INDEX `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员账户流水表';

-- ============================================================
-- 7. timescard_template — 次卡模板表
-- ============================================================
DROP TABLE IF EXISTS `timescard_template`;
CREATE TABLE `timescard_template` (
    `id`           BIGINT        NOT NULL              COMMENT '主键ID',
    `shop_id`      BIGINT        NOT NULL              COMMENT '租户ID',
    `name`         VARCHAR(50)   NOT NULL              COMMENT '模板名',
    `service_id`   BIGINT        DEFAULT NULL          COMMENT '绑定服务ID',
    `times`        INT           NOT NULL DEFAULT 0    COMMENT '次数',
    `price`        DECIMAL(12,2) NOT NULL DEFAULT 0    COMMENT '售价',
    `valid_days`   INT           NOT NULL DEFAULT 0    COMMENT '有效期(天,0=永久)',
    `status`       TINYINT       NOT NULL DEFAULT 1    COMMENT '1启用 0停用',
    `version`      INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_shop` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='次卡模板表';

-- ============================================================
-- 8. member_timescard — 会员次卡表
-- ============================================================
DROP TABLE IF EXISTS `member_timescard`;
CREATE TABLE `member_timescard` (
    `id`            BIGINT        NOT NULL              COMMENT '主键ID',
    `member_id`     BIGINT        NOT NULL              COMMENT '会员ID',
    `template_id`   BIGINT        NOT NULL              COMMENT '模板ID',
    `remain_times`  INT           NOT NULL DEFAULT 0    COMMENT '剩余次数',
    `expire_time`   DATETIME      DEFAULT NULL          COMMENT '到期时间',
    `version`       INT           NOT NULL DEFAULT 0    COMMENT '乐观锁',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员次卡表';

SET FOREIGN_KEY_CHECKS = 1;
