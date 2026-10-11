-- ============================================================
-- pets_shop — shop-service 独立库
-- 自有表: shop / notice
-- 跨服务只读复制表: member (member-service) / sale_order (trade-service) / stock (stock-service)
-- 来源: 01/03/04 + 05/06/07 相关 DDL + 09 种子
-- ============================================================
CREATE DATABASE IF NOT EXISTS `pets_shop` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `pets_shop`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. shop — 店铺表（含 M2 门店管理扩展字段）
-- ============================================================
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop` (
    `id`              BIGINT       NOT NULL              COMMENT '店铺ID (shop_id)',
    `chain_group_id`  BIGINT       NOT NULL              COMMENT '所属集团',
    `shop_name`       VARCHAR(100) NOT NULL              COMMENT '店铺名称',
    `logo`            VARCHAR(255) DEFAULT NULL          COMMENT '店铺Logo',
    `bind_phone`      VARCHAR(20)  DEFAULT NULL          COMMENT '绑定手机号',
    `sms_remain`      INT          NOT NULL DEFAULT 0    COMMENT '短信余量',
    `addr`            VARCHAR(255) DEFAULT NULL          COMMENT '店铺地址',
    `telephone`       VARCHAR(50)  DEFAULT NULL          COMMENT '联系电话',
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
-- 2. notice — 公告与行业资讯表
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

-- ============================================================
-- 3. member — 会员表（跨服务只读复制）
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
-- 4. sale_order — 销售订单表（跨服务只读复制）
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
-- 5. stock — 库存表（跨服务只读复制）
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

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================
-- 种子数据
-- ============================================================
-- shop (02-auth-seed)
INSERT INTO `shop` (`id`, `chain_group_id`, `shop_name`, `level`, `expire_time`, `status`)
VALUES (80351, 1, '示例宠物店', 1, '2027-12-31 23:59:59', 1);

-- shop 扩展字段 (04-m2-seed)
UPDATE `shop` SET
    `logo` = '',
    `bind_phone` = '15638538859',
    `sms_remain` = 120,
    `addr` = '浙江省杭州市西湖区文三路1号',
    `telephone` = '0571-88888888'
WHERE `id` = 80351;

-- notice (04-m2-seed)
INSERT INTO `notice` (`id`, `shop_id`, `type`, `title`, `content`, `status`) VALUES
(1, 0, 1, '系统升级公告', '宠老板系统将于本周六凌晨进行版本升级，期间服务可能短暂中断，敬请谅解。', 1),
(2, 0, 1, '门店运营指南', '秋季宠物美容需求上升，建议门店提前备货洗护用品并安排技师排班。', 1),
(3, 0, 2, '宠物行业资讯：2026年宠物经济持续增长', '据行业报告，2026年国内宠物市场规模预计突破3000亿元，智能化、精细化服务成为趋势。', 1),
(4, 0, 2, '宠物健康：换季谨防皮肤病', '秋季气温变化大，宠物易患皮肤病，建议定期驱虫并保持居住环境干燥。', 1);

-- member (09-seed-data §7)
INSERT INTO `member` (`id`, `shop_id`, `mobile`, `name`, `gender`, `birthday`, `member_level`, `tags`, `remark`, `create_time`) VALUES
(100001, 80351, '13800001111', '张伟', 1, '1990-05-12', 3, '老客,高消费',   '金卡会员,常带金毛来洗澡', '2026-09-10 10:00:00'),
(100002, 80351, '13800002222', '李娜', 2, '1988-08-20', 2, '猫奴',           '家里养两只猫',           '2026-09-08 09:00:00'),
(100003, 80351, '13800003333', '王强', 1, '1995-03-15', 1, NULL,             '新养泰迪',               '2026-09-15 10:00:00'),
(100004, 80351, '13800004444', '赵敏', 2, '1992-11-30', 3, '老客,美容常客',  '比熊定期美容',           '2026-09-16 11:00:00'),
(100005, 80351, '13800005555', '刘洋', 1, '1987-07-01', 2, '田园犬',         '领养田园犬',             '2026-09-19 13:00:00');

-- sale_order (09-seed-data §14)
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

-- stock (09-seed-data §6)
INSERT INTO `stock` (`id`, `shop_id`, `goods_id`, `sku_id`, `quantity`, `warn_qty`) VALUES
(100001, 80351, 100001, 100001, 50, 10),
(100002, 80351, 100001, 100002, 30,  5),
(100003, 80351, 100002, 100003, 25,  5),
(100004, 80351, 100003, 100004, 80, 15),
(100005, 80351, 100004, 100005, 15,  5),
(100006, 80351, 100005, 100006, 100, 20),
(100007, 80351, 100006, 100007, 60, 10);
