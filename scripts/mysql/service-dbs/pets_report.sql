-- ============================================================
-- pets_report — report-service 独立库
-- 自有表: daily_report
-- 跨服务只读复制表: sale_order / order_item / work_order (trade-service)
--                   member / member_account_log (member-service)
-- 来源: 08-m6-ddl + 05-m3-ddl + 07-m5-ddl + 09-seed-data.sql
-- ============================================================
CREATE DATABASE IF NOT EXISTS `pets_report` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pets_report`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. daily_report — 营业日报表
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

-- ============================================================
-- 2. sale_order — 销售订单表（跨服务只读复制）
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
-- 3. order_item — 订单明细表（跨服务只读复制）
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
-- 4. work_order — 服务单表（跨服务只读复制）
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

-- ============================================================
-- 5. member — 会员表（跨服务只读复制）
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
-- 6. member_account_log — 会员账户流水表（跨服务只读复制）
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

-- sale_order (§14)
INSERT INTO `sale_order` (`id`, `shop_id`, `order_no`, `member_id`, `member_name`, `clerk_id`, `clerk_name`, `total`, `discount`, `payable`, `actual`, `pay_type`, `status`, `remark`, `create_time`) VALUES
(100001, 80351, 'SO20260915001', 100001, '张伟', 20001, '超级管理员', 248.00, 100, 248.00, 248.00, 'card',  1, '会员余额消费+洗护', '2026-09-15 10:30:00'),
(100002, 80351, 'SO20260915002', 100002, '李娜', 20001, '超级管理员', 116.00, 100, 116.00, 116.00, 'cash',  1, '现金购买猫砂',     '2026-09-15 14:20:00'),
(100003, 80351, 'SO20260916001', 100004, '赵敏', 20001, '超级管理员', 156.00, 100, 156.00, 156.00, 'card',  1, '银卡余额消费',       '2026-09-16 16:00:00'),
(100004, 80351, 'SO20260916002', NULL,    NULL,  20001, '超级管理员',  25.00, 100,  25.00,  25.00, 'cash',  1, '散客购买飞盘',       '2026-09-16 17:30:00'),
(100005, 80351, 'SO20260917001', 100002, '李娜', 20001, '超级管理员',  80.00, 100,  80.00,  80.00, 'card',  1, '次卡外单次洗护',     '2026-09-17 14:00:00'),
(100006, 80351, 'SO20260918001', 100003, '王强', 20001, '超级管理员', 150.00, 100, 150.00, 150.00, 'wechat',1, '微信支付疫苗接种',   '2026-09-18 11:00:00'),
(100007, 80351, 'SO20260918002', 100003, '王强', 20001, '超级管理员', 188.00, 100, 188.00, 188.00, 'card',  1, '余额购买幼犬粮',     '2026-09-18 11:30:00'),
(100008, 80351, 'SO20260919001', 100005, '刘洋', 20001, '超级管理员', 105.00, 100, 105.00, 105.00, 'card',  1, '余额消费牵引绳+驱虫','2026-09-19 16:00:00'),
(100009, 80351, 'SO20260920001', 100001, '张伟', 20001, '超级管理员', 196.00, 100, 196.00, 196.00, 'wechat',1, '微信购买猫粮+玩具',  '2026-09-20 15:00:00'),
(100010, 80351, 'SO20260920002', NULL,    NULL,  20001, '超级管理员',  50.00, 100,  50.00,   0.00,  NULL,   2, '散客取消订单',       '2026-09-20 16:30:00'),
(100011, 80351, 'SO20260921001', 100002, '李娜', 20001, '超级管理员', 138.00, 100, 138.00, 138.00, 'cash',  1, '今日现金消费',       '2026-09-21 09:30:00'),
(100012, 80351, 'SO20260921002', 100004, '赵敏', 20001, '超级管理员', 120.00, 100, 120.00,   0.00,  NULL,   0, '今日待支付订单',     '2026-09-21 10:15:00');

-- order_item (§15)
INSERT INTO `order_item` (`id`, `order_id`, `item_type`, `item_id`, `item_name`, `sku_id`, `price`, `qty`, `discount`, `amount`, `create_time`) VALUES
(100001, 100001, 1, 100001, '皇家成猫粮 2kg',  100001, 168.00, 1, 100, 168.00, '2026-09-15 10:30:00'),
(100002, 100001, 2, 100001, '小型犬洗护',       NULL,   80.00, 1, 100,  80.00, '2026-09-15 10:30:00'),
(100003, 100002, 1, 100003, '膨润土猫砂 10L',   100004,  58.00, 2, 100, 116.00, '2026-09-15 14:20:00'),
(100004, 100003, 2, 100002, '猫咪基础美容',      NULL,  120.00, 1, 100, 120.00, '2026-09-16 16:00:00'),
(100005, 100003, 1, 100005, '逗猫棒',           100006,  18.00, 2, 100,  36.00, '2026-09-16 16:00:00'),
(100006, 100004, 1, 100006, '宠物飞盘',          100007, 25.00, 1, 100,  25.00, '2026-09-16 17:30:00'),
(100007, 100005, 2, 100001, '小型犬洗护',         NULL,   80.00, 1, 100,  80.00, '2026-09-17 14:00:00'),
(100008, 100006, 2, 100003, '疫苗接种',           NULL,  150.00, 1, 100, 150.00, '2026-09-18 11:00:00'),
(100009, 100007, 1, 100002, '皇家幼犬粮 2kg',   100003, 188.00, 1, 100, 188.00, '2026-09-18 11:30:00'),
(100010, 100008, 1, 100004, '宠物牵引绳 1.2米', 100005,  45.00, 1, 100,  45.00, '2026-09-19 16:00:00'),
(100011, 100008, 2, 100004, '体内驱虫',           NULL,   60.00, 1, 100,  60.00, '2026-09-19 16:00:00'),
(100012, 100009, 1, 100001, '皇家成猫粮 2kg',   100002, 178.00, 1, 100, 178.00, '2026-09-20 15:00:00'),
(100013, 100009, 1, 100005, '逗猫棒',           100006,  18.00, 1, 100,  18.00, '2026-09-20 15:00:00'),
(100014, 100010, 1, 100006, '宠物飞盘',          100007,  25.00, 2, 100,  50.00, '2026-09-20 16:30:00'),
(100015, 100011, 2, 100001, '小型犬洗护',         NULL,   80.00, 1, 100,  80.00, '2026-09-21 09:30:00'),
(100016, 100011, 1, 100003, '膨润土猫砂 10L',   100004,  58.00, 1, 100,  58.00, '2026-09-21 09:30:00'),
(100017, 100012, 2, 100002, '猫咪基础美容',      NULL,  120.00, 1, 100, 120.00, '2026-09-21 10:15:00');

-- work_order (§18)
INSERT INTO `work_order` (`id`, `shop_id`, `order_id`, `order_item_id`, `service_id`, `service_name`, `clerk_id`, `status`, `remark`, `start_time`, `finish_time`, `create_time`) VALUES
(100001, 80351, 100001, 100002, 100001, '小型犬洗护', 20001, 2, '金毛旺财,状态良好', '2026-09-15 10:35:00', '2026-09-15 11:35:00', '2026-09-15 10:30:00'),
(100002, 80351, 100003, 100004, 100002, '猫咪基础美容', 20001, 2, '比熊贝贝,毛发梳理', '2026-09-16 16:05:00', '2026-09-16 17:35:00', '2026-09-16 16:00:00'),
(100003, 80351, 100005, 100007, 100001, '小型犬洗护', 20001, 2, '泰迪球球',          '2026-09-17 14:05:00', '2026-09-17 15:05:00', '2026-09-17 14:00:00'),
(100004, 80351, 100006, 100008, 100003, '疫苗接种', 20001, 2, '泰迪球球,接种狂犬疫苗', '2026-09-18 11:05:00', '2026-09-18 11:35:00', '2026-09-18 11:00:00'),
(100005, 80351, 100008, 100011, 100004, '体内驱虫', 20001, 2, '田园犬大黄,内服驱虫药', '2026-09-19 16:05:00', '2026-09-19 16:25:00', '2026-09-19 16:00:00'),
(100006, 80351, 100011, 100015, 100001, '小型犬洗护', 20001, 0, '今日待服务-泰迪球球', NULL, NULL, '2026-09-21 09:30:00'),
(100007, 80351, 100012, 100017, 100002, '猫咪基础美容', 20001, 0, '今日待服务-比熊贝贝,订单待支付', NULL, NULL, '2026-09-21 10:15:00');

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

-- daily_report (§21)
INSERT INTO `daily_report` (`id`, `shop_id`, `report_date`, `sale_amount`, `order_count`, `refund_amount`, `avg_order`, `member_count`, `new_member`) VALUES
(100001, 80351, '2026-09-15', 364.00, 2, 0.00, 182.00, 2, 1),
(100002, 80351, '2026-09-16', 181.00, 2, 0.00,  90.50, 1, 1),
(100003, 80351, '2026-09-17',  80.00, 1, 0.00,  80.00, 1, 0),
(100004, 80351, '2026-09-18', 338.00, 2, 0.00, 169.00, 1, 0),
(100005, 80351, '2026-09-19', 105.00, 1, 0.00, 105.00, 1, 1),
(100006, 80351, '2026-09-20', 196.00, 1, 0.00, 196.00, 1, 0),
(100007, 80351, '2026-09-21', 138.00, 1, 0.00, 138.00, 1, 0);
