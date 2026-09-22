# 🐛 K3S 部署踩坑记录

本文档记录宠老板复刻项目从本地开发迁移到 k3s 集群部署过程中遇到的所有关键问题与解决方案，供后续部署参考。

---

## 一、集群连接与节点发现

### 1.1 Parallels VM 网段混淆
**现象**：默认扫描 `10.37.129.x` 网段（Parallels 默认 host-only），SSH 22 端口全部 closed。

**根因**：VM 实际使用 Bridged 网络，IP 在 `192.168.168.13x` 网段，非默认 host-only 网段。

**解决**：阅读 `README.md` 中节点信息表，确认真实 IP：
- master: `192.168.168.131`
- worker1: `192.168.168.132`
- worker2: `192.168.168.133`

### 1.2 SSH Host Key 冲突
**现象**：`ssh root@192.168.168.131` 报 `REMOTE HOST IDENTIFICATION HAS CHANGED`，密码认证被禁用。

**根因**：VM 重建后 host key 变化，`~/.ssh/known_hosts` 存有旧 key。

**解决**：
```bash
ssh-keygen -R 192.168.168.131
ssh-keygen -R 192.168.168.132
ssh-keygen -R 192.168.168.133
# 之后用 sshpass 重连
sshpass -p root ssh -o StrictHostKeyChecking=no root@192.168.168.131
```

### 1.3 kubectl 必须配置 KUBECONFIG
**现象**：SSH 登录 master 后 `kubectl get nodes` 报 `The connection to the server 127.0.0.1:6443 was refused`。

**根因**：k3s 的 kubeconfig 不在默认路径，需显式指定。

**解决**：
```bash
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
kubectl get nodes
```

---

## 二、镜像构建与导入

### 2.1 架构一致性验证
**关键点**：Mac 本地为 arm64 (Apple Silicon)，k3s VM 为 `aarch64` (Ubuntu 26 ARM)，架构一致，可直接 `docker build` + `k3s ctr image import`，无需 buildx 跨架构构建。

**验证命令**：
```bash
uname -m          # 本地: arm64
docker info | grep Architecture  # aarch64
kubectl get nodes -o wide | grep arch  # arm64
```

### 2.2 nginx:alpine 镜像源 403
**现象**：`docker build` 前端镜像时 `nginx:alpine` 拉取失败，阿里云镜像源返回 403 Forbidden。

**根因**：阿里云镜像源对 `nginx:alpine` tag 限制或同步缺失。

**解决**：改用 1ms.run 镜像源 + 指定版本 tag：
```dockerfile
FROM docker.1ms.run/library/nginx:1.27-alpine
```

**教训**：避免用 `alpine` 这种浮动 tag，指定具体版本更稳定；多准备几个镜像源备用（`docker.1ms.run`、`daocloud.io`、`aliyuncs.com`）。

### 2.3 master 节点磁盘不足导致 scp 失败
**现象**：`scp` 镜像 tar 到 master 报 `write remote "/tmp/xxx.tar": Failure`，`k3s ctr image import` 报 `unexpected EOF`。

**根因**：master 节点 2C2G，磁盘/内存紧张，大文件传输中断。

**解决**：所有业务 Pod 通过 `nodeSelector` 调度到 worker 节点，master 只跑 control plane，无需导入业务镜像。只在 worker1/worker2 导入即可。

### 2.4 镜像导入必须覆盖所有调度节点
**规则**：`imagePullPolicy: IfNotPresent` + 本地镜像，Pod 调度到哪个节点，镜像就必须导入该节点。

**本例分配**：
- w1 (worker1): gateway, auth, shop, system, goods, frontend → 导入这些镜像
- w2 (worker2): trade, stock, member, market, report → 导入这些镜像
- master: 不调度业务 pod，无需导入

**稳妥做法**：所有镜像都导入两个 worker 节点，避免调度时缺镜像。

---

## 三、数据库初始化

### 3.1 k3s MySQL 缺业务表
**现象**：登录返回 500，日志报 `Table 'pets.goods' doesn't exist`。

**根因**：k3s 集群的 MySQL 只有最初部署的 5 张基础表（account/chain_group/clerk/role/shop），M2-M6 的 29 张业务表从未导入。

**解决**：本地 `scripts/mysql/` 下有分模块 DDL 脚本，通过 kubectl exec 导入：
```bash
cd /Users/wuyunbin/workspace/pets/scripts/mysql
cat 03-m2-ddl.sql 05-m3-ddl.sql 06-m4-ddl.sql 07-m5-ddl.sql 08-m6-ddl.sql | \
  sshpass -p root ssh root@192.168.168.131 \
  'export KUBECONFIG=/etc/rancher/k3s/k3s.yaml && kubectl exec -i -n middleware deploy/mysql -- mysql -uroot -proot --force -D pets'
```

**教训**：DDL 脚本用 `--force` 跳过已存在的表（如 shop），幂等导入。

### 3.2 shop 表缺失业务列
**现象**：登录后查询 shop 报 `Unknown column 'logo' in 'field list'`。

**根因**：k3s 的 shop 表是最初 M1 阶段建的简版，缺 M2 补充的 `logo/bind_phone/sms_remain/addr/telephone` 列。DDL 脚本用 `CREATE TABLE IF NOT EXISTS`，不会给已存在的表补列。

**解决**：手动 ALTER：
```sql
ALTER TABLE shop
  ADD COLUMN logo varchar(255) DEFAULT NULL AFTER shop_name,
  ADD COLUMN bind_phone varchar(20) DEFAULT NULL AFTER logo,
  ADD COLUMN sms_remain int NOT NULL DEFAULT 0 AFTER bind_phone,
  ADD COLUMN addr varchar(255) DEFAULT NULL AFTER sms_remain,
  ADD COLUMN telephone varchar(20) DEFAULT NULL AFTER addr;
```

**教训**：实体类字段与表结构要对齐，部署前用 `DESC <table>` 核对。

### 3.3 stock 表缺 avg_cost 列
**现象**：库存查询报 `Unknown column 'avg_cost' in 'field list'`。

**根因**：Stock 实体有 `avgCost` 字段，但 DDL 脚本里 stock 表没建这列（后期补的需求）。

**解决**：
```sql
ALTER TABLE stock ADD COLUMN avg_cost decimal(10,2) NOT NULL DEFAULT 0.00 AFTER quantity;
```

### 3.4 seed 数据缺失
**现象**：接口返回 `code=0` 但 `data` 为空数组。

**根因**：只导入了 DDL（表结构），没导入 seed 数据（04-m2-seed.sql、09-seed-data.sql）。

**解决**：补导 seed 脚本，验证后库内 member 5 条、order 12 条、service 4 条等。

---

## 四、资源调度与稳定性

### 4.1 master 节点 OOMKilled
**现象**：auth-service 旧版 pod 在 master 上 `OOMKilled` 反复重启。

**根因**：master 2C2G，已跑 control plane + middleware（mysql/redis/mongodb/nacos 等），再跑业务 pod 内存不够。

**解决**：所有业务 Deployment 加 `nodeSelector: app-host: w1/w2`，强制调度到 worker。master 只负责集群管理。

### 4.2 worker 内存不均导致 OOM
**现象**：w1 上跑 6 个服务（gateway+auth+shop+system+goods+frontend），auth 反复 OOMKilled；w2 上服务少，内存空闲。

**根因**：初始分配不均，w1 内存压力过大。

**解决**：负载均衡重分配：
- w1: gateway, auth, shop, system, goods, frontend（6个，含轻量 nginx）
- w2: trade, stock, member, market, report（5个）

**教训**：部署后用 `kubectl top nodes` 观察内存，超 80% 的节点要迁移 pod。

### 4.3 Spring Boot 启动慢触发 liveness 重启
**现象**：pod 状态 `Running` 但 `0/1`，events 显示 `Liveness probe failed: context deadline exceeded`，被 kill 后重启循环。

**根因**：Spring Boot 4 + JVM 在 2C4G worker 上启动需 90-120s，原配置 `livenessProbe.initialDelaySeconds: 120` 不够。

**解决**：调大 liveness 延迟到 240s，readiness 保持 60s（不通过只是不接流量，不会杀 pod）：
```yaml
readinessProbe:
  httpGet:
    path: /actuator/health
    port: <PORT>
  initialDelaySeconds: 60
  periodSeconds: 10
  failureThreshold: 30    # 60 + 30*10 = 360s 容忍
livenessProbe:
  httpGet:
    path: /actuator/health
    port: <PORT>
  initialDelaySeconds: 240  # 给足启动时间
  periodSeconds: 30
```

### 4.4 shop-service Pending（CPU requests 不足）
**现象**：shop-service 从 w1 迁到 w2 后 `Pending`，events 报 `Insufficient cpu`。

**根因**：w2 已跑 5 个服务，CPU requests 累计超过节点容量，新 pod 无法调度。

**解决**：shop 留在 w1（report 迁到 w2 释放 w1 空间），或降低 `resources.requests.cpu`。

**教训**：`resources.requests` 是调度依据，总和不能超节点容量；小节点用 `150m` 起步。

---

## 五、配置与环境变量

### 5.1 环境变量风格对齐
**关键点**：`application.properties` 用 `${DB_HOST:localhost}` 形式，k8s env 必须用 `DB_HOST`（不是 `SPRING_DATASOURCE_URL`）。

**业务服务 env**：
```yaml
env:
- name: DB_HOST
  value: "mysql.middleware.svc.cluster.local"
- name: DB_PORT
  value: "3306"
# ... DB_NAME/DB_USER/DB_PASSWORD/REDIS_HOST/MONGO_HOST 等
```

**gateway 特殊**：RouteConfig.java 用 `@Value("${auth.service.url:...}")`，env 用大写 + 下划线：
```yaml
- name: AUTH_SERVICE_URL
  value: "http://auth-service.app.svc.cluster.local:9001"
```

### 5.2 跨 namespace 访问中间件
**规则**：业务 pod 在 `app` 命名空间，中间件在 `middleware` 命名空间，DNS 名需带完整 namespace 后缀：`mysql.middleware.svc.cluster.local`。

### 5.3 Spring Boot 4 MongoDB 前缀变化
**历史坑**：Spring Boot 4 把 `spring.data.mongodb.uri` 改为 `spring.mongodb.uri`（环境变量 `SPRING_MONGODB_URI`），Redis 前缀不变（仍是 `spring.data.redis`）。

**本例配置**：
```properties
spring.mongodb.uri=mongodb://${MONGO_USER:root}:${MONGO_PASSWORD:root}@${MONGO_HOST:localhost}:${MONGO_PORT:27017}/${MONGO_DB:pets}?authSource=admin
```

---

## 六、前端部署

### 6.1 nginx 反代到 gateway
**配置**：前端 nginx 反代 `/api/v1/` 到集群内 gateway service：
```nginx
location /api/v1/ {
    proxy_pass http://pets-gateway.app.svc.cluster.local:9000;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

### 6.2 SPA 路由刷新 404
**解决**：nginx 加 `try_files $uri $uri/ /index.html;`，前端路由由 React Router 接管。

### 6.3 前端 NodePort 独立于 gateway
**设计**：
- frontend NodePort 30080 → 浏览器访问入口
- gateway NodePort 30900 → API 直连/调试用
- 前端通过 nginx 反代调 gateway，不直接暴露 gateway 给浏览器（避免跨域）

---

## 七、API 路径

### 7.1 Controller 路径核对
**教训**：接口 500 报 `No static resource xxx` 是路径错误，不是服务异常。

**本例正确路径**：
- 商品分页: `POST /api/v1/goods/page`（不是 `/goods/goods`）
- 订单分页: `POST /api/v1/trade/order/page`（不是 `/trade/page`）
- 营销分页: `POST /api/v1/marketing/page`（不是 `/marketing/activity/page`）
- 库存分页: `POST /api/v1/stock/page`

### 7.2 收银台结算字段
**坑点**：CheckoutReq.CheckoutItem 字段是 `itemId/itemType/qty/price`，不是 `goodsId/quantity`。

**正确请求**：
```json
{
  "items":[{"itemType":1,"itemId":100001,"itemName":"皇家成猫粮2kg","skuId":100001,"price":200.00,"qty":1}],
  "memberId":100001,
  "payType":"1"
}
```

---

## 八、部署流程清单

部署新服务时按以下顺序检查，避免重复踩坑：

1. [ ] 本地 `mvn package` 生成 jar
2. [ ] `docker build` 构建镜像（arm64，base image 用 docker.1ms.run）
3. [ ] `docker save` + `scp` 到 worker 节点 + `k3s ctr image import`
4. [ ] 编写 deploy yaml（nodeSelector + env + probe + resources）
5. [ ] `kubectl apply -f --validate=false`
6. [ ] `kubectl get pods -n app -o wide` 确认调度到 worker
7. [ ] `kubectl logs -n app <pod> --tail=50` 确认启动无异常
8. [ ] DDL 脚本导入 MySQL（`--force` 幂等）
9. [ ] seed 数据导入
10. [ ] `DESC <table>` 核对实体字段与表结构
11. [ ] curl 测试登录 + 核心接口
12. [ ] `kubectl top nodes` 确认内存未超 80%

---

*本文档持续更新，遇到新坑请补充。*
