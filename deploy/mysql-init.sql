-- 首次启动(空 datadir)时由 MySQL 官方镜像自动执行
-- 创建业务库 pets 与 Nacos 配置库 nacos
CREATE DATABASE IF NOT EXISTS pets DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS nacos DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
