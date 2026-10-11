-- Nacos 默认管理员账号 (nacos/nacos)，密码 BCrypt 哈希前缀必须为 $2a$
-- 该哈希由 Spring Security BCryptPasswordEncoder 生成，明文为 nacos
INSERT INTO nacos.users (username, password, enabled)
VALUES ('nacos', '$2a$10$ssgs5n19uh5Lpvr77Vr0Y.SS63hawnvOBTtgtrbdLAuj.uws1Akey', 1)
ON DUPLICATE KEY UPDATE password = VALUES(password), enabled = 1;

INSERT INTO nacos.roles (username, role)
VALUES ('nacos', 'ROLE_ADMIN')
ON DUPLICATE KEY UPDATE role = VALUES(role);
