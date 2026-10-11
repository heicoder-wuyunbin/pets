// ============================================================
// 宠老板复刻 · M1 登录鉴权 · MongoDB 初始化脚本
// 集合: sms_code (短信验证码留痕) / login_log (登录日志)
// 用法: mongosh "mongodb://root:root@192.168.110.200:27017/pets_auth?authSource=admin" 01-auth-init.js
// ============================================================

db = db.getSiblingDB('pets_auth');

// ============================================================
// 1. sms_code 集合 — 短信验证码留痕
//    实时校验走 Redis, 此处仅审计留痕
// ============================================================

db.createCollection('sms_code');

// 索引: 按手机号+业务类型查最近验证码
db.sms_code.createIndex(
    { phone: 1, biz_type: 1, create_time: -1 },
    { name: 'idx_phone_biz_time', background: true }
);

// TTL 索引: expire_at 到期后自动删除 (5 分钟)
db.sms_code.createIndex(
    { expire_at: 1 },
    { name: 'idx_ttl_expire', expireAfterSeconds: 0, background: true }
);

// ============================================================
// 2. login_log 集合 — 登录日志
//    高吞吐写入, 仅追加, TTL 90 天自动清理
// ============================================================

db.createCollection('login_log');

// 索引: 按手机号查登录历史
db.login_log.createIndex(
    { phone: 1, create_time: -1 },
    { name: 'idx_phone_time', background: true }
);

// 索引: 按账号查登录历史
db.login_log.createIndex(
    { account_id: 1, create_time: -1 },
    { name: 'idx_account_time', background: true }
);

// TTL 索引: 90 天后自动清理 (90 * 86400 = 7776000 秒)
db.login_log.createIndex(
    { create_time: 1 },
    { name: 'idx_ttl_create', expireAfterSeconds: 7776000, background: true }
);

// ============================================================
// 验证输出
// ============================================================
print('========================================');
print('MongoDB auth 初始化完成');
print('sms_code 索引:');
printjson(db.sms_code.getIndexes());
print('login_log 索引:');
printjson(db.login_log.getIndexes());
print('========================================');
