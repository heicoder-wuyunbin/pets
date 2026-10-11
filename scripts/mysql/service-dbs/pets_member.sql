-- ============================================================
-- pets_member — member-service 独立库
-- 表: member / member_pet / member_account / card_type / member_card
--     member_account_log / timescard_template / member_timescard
-- 来源: 07-m5-ddl.sql + 09-seed-data.sql (§7~§13, §19)
-- ============================================================
CREATE DATABASE IF NOT EXISTS `pets_member` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pets_member`;

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

-- ============================================================
-- 种子数据 (09-seed-data.sql)
-- ============================================================
-- member (§7)
INSERT INTO `member` (`id`, `shop_id`, `mobile`, `name`, `gender`, `birthday`, `member_level`, `tags`, `remark`, `create_time`) VALUES
(100001, 80351, '13800001111', '张伟', 1, '1990-05-12', 3, '老客,高消费',   '金卡会员,常带金毛来洗澡', '2026-09-10 10:00:00'),
(100002, 80351, '13800002222', '李娜', 2, '1988-08-20', 2, '猫奴',           '家里养两只猫',           '2026-09-08 09:00:00'),
(100003, 80351, '13800003333', '王强', 1, '1995-03-15', 1, NULL,             '新养泰迪',               '2026-09-15 10:00:00'),
(100004, 80351, '13800004444', '赵敏', 2, '1992-11-30', 3, '老客,美容常客',  '比熊定期美容',           '2026-09-16 11:00:00'),
(100005, 80351, '13800005555', '刘洋', 1, '1987-07-01', 2, '田园犬',         '领养田园犬',             '2026-09-19 13:00:00');

-- member_pet (§8)
INSERT INTO `member_pet` (`id`, `member_id`, `pet_name`, `pet_type`, `breed`, `gender`, `weight`, `birthday`, `vaccine`) VALUES
(100001, 100001, '旺财', '狗', '金毛',       1, 28.50, '2023-04-10', '已完成三针,狂犬已打'),
(100002, 100001, '喵喵', '猫', '英国短毛猫', 2,  4.20, '2022-06-15', '已完成三针'),
(100003, 100002, '球球', '狗', '泰迪',       1,  5.50, '2023-09-20', '已完成两针,狂犬待打'),
(100004, 100003, '橘子', '猫', '橘猫',       1,  6.00, '2021-12-01', '已完成全部疫苗'),
(100005, 100004, '贝贝', '狗', '比熊',       2,  4.80, '2024-02-14', '首针已打'),
(100006, 100005, '大黄', '狗', '中华田园犬', 1, 18.00, '2020-05-01', '已绝育,疫苗齐全');

-- member_account (§9)
INSERT INTO `member_account` (`id`, `member_id`, `balance`, `points`, `total_consume`, `total_recharge`) VALUES
(100001, 100001, 252.00, 248, 444.00, 500.00),
(100002, 100002, 200.00,  80, 334.00, 280.00),
(100003, 100003,  50.00, 188, 338.00, 238.00),
(100004, 100004, 800.00, 156, 156.00, 956.00),
(100005, 100005,   0.00, 105, 105.00, 105.00);

-- card_type (§10)
INSERT INTO `card_type` (`id`, `shop_id`, `name`, `face_value`, `discount`, `valid_days`, `status`) VALUES
(100001, 80351, '储值卡500赠50',  500.00, 1.00, 365, 1),
(100002, 80351, '银卡(1000元9折)', 1000.00, 0.90, 365, 1);

-- member_card (§11)
INSERT INTO `member_card` (`id`, `member_id`, `card_type_id`, `balance`, `expire_time`, `create_time`) VALUES
(100001, 100001, 100001, 252.00, '2027-09-10 23:59:59', '2026-09-10 10:05:00'),
(100002, 100004, 100002, 800.00, '2027-09-16 23:59:59', '2026-09-16 11:05:00');

-- timescard_template (§12)
INSERT INTO `timescard_template` (`id`, `shop_id`, `name`, `service_id`, `times`, `price`, `valid_days`, `status`) VALUES
(100001, 80351, '小型犬洗护10次卡', 100001, 10, 680.00, 180, 1),
(100002, 80351, '猫咪美容5次卡',    100002,  5, 540.00, 180, 1);

-- member_timescard (§13)
INSERT INTO `member_timescard` (`id`, `member_id`, `template_id`, `remain_times`, `expire_time`, `create_time`) VALUES
(100001, 100001, 100001, 7, '2027-03-10 23:59:59', '2026-09-10 11:00:00'),
(100002, 100002, 100002, 3, '2027-03-10 23:59:59', '2026-09-12 10:00:00');

-- member_account_log (§19)
INSERT INTO `member_account_log` (`id`, `member_id`, `type`, `amount`, `balance_after`, `points`, `points_after`, `order_id`, `remark`, `create_time`) VALUES
(100001, 100001, 1,  500.00,  500.00,    0,   0, NULL,   '储值卡充值500',     '2026-09-10 10:05:00'),
(100002, 100001, 2, -248.00,  252.00,  248, 248, 100001, '余额消费-成猫粮+洗护','2026-09-15 10:30:00'),
(100003, 100002, 1,  280.00,  280.00,    0,   0, NULL,   '储值卡充值280',     '2026-09-08 09:05:00'),
(100004, 100002, 2,  -80.00,  200.00,   80,  80, 100005, '余额消费-洗护',     '2026-09-17 14:00:00'),
(100005, 100003, 1,  238.00,  238.00,    0,   0, NULL,   '储值卡充值238',     '2026-09-05 10:05:00'),
(100006, 100003, 2, -188.00,   50.00,  188, 188, 100007, '余额消费-幼犬粮',    '2026-09-18 11:30:00'),
(100007, 100004, 1,  956.00,  956.00,    0,   0, NULL,   '银卡充值956',       '2026-09-16 11:05:00'),
(100008, 100004, 2, -156.00,  800.00,  156, 156, 100003, '余额消费-美容+玩具', '2026-09-16 16:00:00'),
(100009, 100005, 1,  105.00,  105.00,    0,   0, NULL,   '储值卡充值105',     '2026-09-06 13:05:00'),
(100010, 100005, 2, -105.00,    0.00,  105, 105, 100008, '余额消费-牵引绳+驱虫','2026-09-19 16:00:00');
