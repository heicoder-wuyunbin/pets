# 🐘 Pets Project - 宠老板复刻 K3S 集群部署运维文档

本项目是宠老板 SaaS 宠物店管理系统的复刻实现，采用 Spring Boot 4 微服务 + React 前端架构，部署在 k3s Kubernetes 集群上，服务于课堂教学演示和实际生产级中间件部署。

## 🧭 项目概览

本集群包含一台主节点（Master）和两台从节点（Worker），三节点共构成一个高可用的 Kubernetes 集群。

*   **集群版本**: v1.36.4+k3s1
*   **容器运行时**: containerd v2.3.4
*   **环境类型**: 教学演示 / 中间件负载均衡
*   **技术栈**: Spring Boot 4 + Java 25 + MyBatis-Plus + React 18 + Ant Design 5 + Vite

## 🔑 节点登录信息 (VM Console/SSH)

| 角色 | 名称 | IP 地址 | Kubernetes Role |
| :--- | :--- | :--- | :--- |
| **主节点** | k3s-master | `192.168.168.131` | Control Plane / 核心管理节点 |
| **从节点 1** | k3s-worker1 | `192.168.168.132` | Worker Node (标签: app-host=w1) |
| **从节点 2** | k3s-worker2 | `192.168.168.133` | Worker Node (标签: app-host=w2) |

### SSH 登录凭证 (教学用途)
**警告：本密码仅为本次课程演示的临时账号，不可用于生产环境！实际部署请勿将明文密码记录在 README 中。**

*   **用户名**: `root`
*   **密码**: `root`

## 🌐 应用访问入口

| 入口 | URL | 说明 |
| :--- | :--- | :--- |
| **前端页面** | `http://192.168.168.132:30080` | 浏览器访问，登录后使用全功能 |
| Gateway API | `http://192.168.168.132:30900` | 后端 API 直连（调试用） |
| 健康检查 | `http://192.168.168.132:30900/actuator/health` | Gateway 健康状态 |

### 测试账号
**警告：以下为教学演示用测试账号，不可用于生产环境！**

| 角色 | 账号 | 密码 | 说明 |
| :--- | :--- | :--- | :--- |
| 超级管理员 | `15638538859` | `Admin@2026` | SUPER_ADMIN，店铺：示例宠物店 |

**登录 API 示例**：
```bash
curl -X POST http://192.168.168.132:30080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"15638538859","password":"Admin@2026"}'
```

## 📂 服务部署映射

### 应用服务 (app 命名空间)

| 服务名称 | 端口 | 镜像 | 调度节点 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| pets-gateway | 9000 | pets/gateway:1.0.0 | w1 (NodePort 30900) | API 网关，路由 + JWT 鉴权 |
| auth-service | 9001 | pets/auth-service:1.0.0 | w1 | 认证服务，登录/注册/验证码 |
| shop-service | 9002 | pets/shop-service:1.0.0 | w1 | 店铺管理 |
| system-service | 9003 | pets/system-service:1.0.0 | w1 | 系统管理，店员/操作日志 |
| trade-service | 9004 | pets/trade-service:1.0.0 | w2 | 交易服务，收银台/订单/支付 |
| goods-service | 9005 | pets/goods-service:1.0.0 | w1 | 商品与服务项管理 |
| stock-service | 9006 | pets/stock-service:1.0.0 | w2 | 库存管理，扣减/入库/出库 |
| member-service | 9007 | pets/member-service:1.0.0 | w2 | 会员中心，会员卡/积分/储值 |
| market-service | 9008 | pets/market-service:1.0.0 | w2 | 营销活动，优惠计算 |
| report-service | 9009 | pets/report-service:1.0.0 | w2 | 数据统计，营业日报/会员统计 |
| frontend | 80 | pets/frontend:1.0.0 | w1 (NodePort 30080) | 前端 nginx 静态 + API 反代 |

### 中间件 (middleware 命名空间)

| 服务名称 | 运行节点 (IP) | Port/Address | 说明 |
| :--- | :--- | :--- | :--- |
| MySQL | Service/Pod | 30306 (client) / 3306 (集群内) | InnoDB / pets 库 34 张表 |
| Redis | Service/Pod | 30379 (client) / 6379 (集群内) | 缓存 / 号段发号器 |
| MongoDB | Service/Pod | 30317 (client) / 27017 (集群内) | sms_code + login_log (TTL 索引) |
| Nacos | Service/Pod | 30848 (client) / 8848 (集群内) | 配置中心 + 服务发现 |
| Meilisearch | Service/Pod | 30370 (client) | 搜索引擎 / 本地索引 |
| RocketMQ Broker | Service/Pod | 10911 (client) | 消息队列 |

### 中间件凭证
| 服务 | 用户名 | 密码 | 备注 |
| :--- | :--- | :--- | :--- |
| MySQL | root | root | pets 库 |
| Redis | (无密码) | - | - |
| MongoDB | root | root | authSource=admin |
| Nacos | nacos | nacos | - |

## 🚀 快速部署流程

### 前置条件
- 本地 Mac 已安装 Docker + sshpass
- k3s 集群 3 节点 Running，中间件已部署在 `middleware` 命名空间
- 项目代码已 clone 到 `/Users/wuyunbin/workspace/pets`

### 1. 后端编译
```bash
cd /Users/wuyunbin/workspace/pets
mvn clean package -DskipTests
```

### 2. 构建镜像（arm64，与 k3s VM 架构一致）
```bash
# 9 个业务服务
for svc in auth-service shop-service system-service trade-service goods-service stock-service member-service market-service report-service; do
  docker build -t pets/$svc:1.0.0 services/$svc/
done
# 网关
docker build -t pets/gateway:1.0.0 pets-gateway/
# 前端
cd frontend && npm run build && docker build -t pets/frontend:1.0.0 . && cd ..
```

### 3. 导入镜像到 worker 节点
```bash
# 打包所有镜像
docker save -o /tmp/pets-images.tar \
  pets/auth-service:1.0.0 pets/shop-service:1.0.0 pets/system-service:1.0.0 \
  pets/trade-service:1.0.0 pets/goods-service:1.0.0 pets/stock-service:1.0.0 \
  pets/member-service:1.0.0 pets/market-service:1.0.0 pets/report-service:1.0.0 \
  pets/gateway:1.0.0 pets/frontend:1.0.0

# 导入到两个 worker 节点（master 不调度业务 pod，无需导入）
for ip in 132 133; do
  sshpass -p root scp /tmp/pets-images.tar root@192.168.168.$ip:/tmp/
  sshpass -p root ssh root@192.168.168.$ip "k3s ctr image import /tmp/pets-images.tar && rm /tmp/pets-images.tar"
done
```

### 4. 部署到 k3s
```bash
# 从 Mac 通过 SSH apply（--validate=false 避免 TLS 超时）
cat /Users/wuyunbin/workspace/pets/deploy/k8s/app/*.yaml | \
  sshpass -p root ssh root@192.168.168.131 \
  'export KUBECONFIG=/etc/rancher/k3s/k3s.yaml && kubectl apply -f - --validate=false'
```

### 5. 初始化数据库
```bash
# 导入 DDL（幂等，已存在的表会跳过）
cd /Users/wuyunbin/workspace/pets/scripts/mysql
cat 03-m2-ddl.sql 05-m3-ddl.sql 06-m4-ddl.sql 07-m5-ddl.sql 08-m6-ddl.sql | \
  sshpass -p root ssh root@192.168.168.131 \
  'export KUBECONFIG=/etc/rancher/k3s/k3s.yaml && kubectl exec -i -n middleware deploy/mysql -- mysql -uroot -proot --force -D pets'

# 导入 seed 数据
cat 04-m2-seed.sql 09-seed-data.sql | \
  sshpass -p root ssh root@192.168.168.131 \
  'export KUBECONFIG=/etc/rancher/k3s/k3s.yaml && kubectl exec -i -n middleware deploy/mysql -- mysql -uroot -proot --force -D pets'
```

### 6. 验证
```bash
# Pod 状态（应全部 1/1 Running）
sshpass -p root ssh root@192.168.168.131 \
  'export KUBECONFIG=/etc/rancher/k3s/k3s.yaml && kubectl get pods -n app'

# 登录测试
curl -X POST http://192.168.168.132:30080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"phone":"15638538859","password":"Admin@2026"}'
```

## 🛠️ 巡检与故障排查 (Troubleshooting)

当出现问题时，请按照以下「四层巡检法」（由浅入深）快速定位：

### 第一层：物理/虚拟机层面（VM Lifecycle）
确保三台 VM (`u26`, `u26-worker01`, `u26-worker02`) 在 Parallels 中处于 **Running** 状态，且 Mac 本地机器可以 Ping 通其静态 IP 地址 (`192.168.168.13x`)。

### 第二层：操作系统层面（OS Service）
登录任一节点后，检查关键服务是否活跃：
*   **主节点 (`131`)**: `systemctl is-active k3s` (集群管理服务)
*   **从节点 (`132`, `133`)**: `systemctl is-active k3s-agent` (作为 Agent 加入集群的服务)
*   **快速检查**: `uptime` (观察负载均值)，`free -h` (观察内存使用情况)。

### 第三层：Kubernetes 集群层面（Cluster Health）
**此检查必须在主节点 (`131`) 上执行 `kubectl` 命令。**

1.  **获取集群概览**:
    ```bash
    # 登录主节点后，必须先配置KUBECONFIG路径才能使用 kubectl
    export KUBECONFIG=/etc/rancher/k3s/k3s.yaml

    # 检查集群状态 (应全部 READY)
    kubectl get nodes -o wide
    ```
2.  **检查 Pod 运行情况**:
    ```bash
    kubectl get pods -A # 查看所有命名空间的所有 Pod 状态 (应全部 Running)
    kubectl top nodes   # 观察节点内存使用 (超 80% 需迁移 pod)
    ```

### 第四层：应用/中间件层面（Application Layer）
当 Pod 处于 Running 但服务不可用时，问题通常在应用内部或网络转发上。

*   **查看 Pod 日志**: 若 Pod 容器内报错，请使用以下命令：
    ```bash
    kubectl logs -n app <pod-name> --tail=50
    kubectl logs -n middleware <pod-name> --tail=50
    ```
*   **验证中间件端口**: 如果是应用层问题，请使用 `nc` 命令或提供服务商的特定客户端来验证端口连通性（如 MySQl/Redis 的端口）。
*   **描述 Pod 排查调度问题**:
    ```bash
    kubectl describe pod -n app <pod-name>  # 看 Events 列，OOM/Pending/Probe 失败都在这
    ```

## 📌 关键约束与注意事项

### 资源调度
- **master 节点不跑业务 pod**：2C2G 内存仅够 control plane + middleware，业务 pod 必须用 `nodeSelector: app-host: w1/w2`
- **负载均衡**：w1 跑 6 个轻量服务，w2 跑 5 个，用 `kubectl top nodes` 监控，超 80% 迁移
- **JVM 堆内存**：`-Xms128m -Xmx256m` 适配小节点（Dockerfile 已配置）

### 健康探针
- **readinessProbe**: `initialDelaySeconds: 60`，不通过只断流量不杀 pod
- **livenessProbe**: `initialDelaySeconds: 240`，给 Spring Boot 启动留足时间，过短会 OOMKilled 循环

### 镜像管理
- **imagePullPolicy: IfNotPresent** + 本地镜像，Pod 调度到哪个节点镜像就必须导入该节点
- **arm64 架构**：Mac 本地与 k3s VM 一致，直接 `docker build`，无需 buildx 跨架构
- **base image 源**：`docker.1ms.run/library/xxx`，阿里云源对部分 tag 403

### 数据库
- **34 张表**：5 张基础表（M1）+ 29 张业务表（M2-M6），DDL 脚本在 `scripts/mysql/`
- **幂等导入**：`mysql --force` 跳过已存在的表/列
- **表结构核对**：实体类字段与表结构要对齐，用 `DESC <table>` 核对

## 📖 详细踩坑记录

完整的部署踩坑记录（含根因分析与解决方案）请见：[deploy/docs/pitfalls.md](deploy/docs/pitfalls.md)

涵盖 8 大类问题：
1. 集群连接与节点发现（网段混淆、SSH host key、KUBECONFIG）
2. 镜像构建与导入（架构、镜像源 403、磁盘不足、节点覆盖）
3. 数据库初始化（缺表、缺列、缺 seed）
4. 资源调度与稳定性（OOMKilled、负载不均、启动慢、Pending）
5. 配置与环境变量（DB_HOST 风格、跨 namespace、Spring Boot 4 前缀变化）
6. 前端部署（nginx 反代、SPA 路由、NodePort 设计）
7. API 路径（Controller 路径核对、结算字段名）
8. 部署流程清单

---
*本运维文档结合了实际部署中的所有踩坑经验，旨在成为您未来进行集群维护的指引。*
