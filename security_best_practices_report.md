# 红队视角安全审查报告 · pets 项目

> 审查范围：`/Users/wuyunbin/workspace/pets`（auth-service / pets-gateway / pets-common / pets-id-generator + k8s 部署清单）
> 技术栈：Spring Boot 4.0.8 / Spring Cloud 2025.1 / Spring Cloud Alibaba 2025.1.0.0 / MyBatis-Plus 3.5.17 / JJWT 0.13.0 / MySQL / MongoDB / Redis / Nacos 3.x
> 审查日期：2026-09-20
> 视角：红队（外部攻击者 + 已混入内网的横向移动者）

---

## 执行摘要

项目整体架构清晰、分层规范，但当前安全基线薄弱，存在 **3 个 Critical 级**可直接导致全员账号沦陷的问题，另有 **6 个 High** 与若干 Medium 级风险。

最致命的组合链是：

> JWT 密钥硬编码且明文随源码/镜像/部署清单分发 → 红队可本地离线伪造任意账号的合法 token；同时下游服务直接信任 `X-User-Id` 等请求头，若能绕过网关或利用 `mutate().header()` 的追加语义，即可越权为任意租户。

建议在上线前优先修复 CRITICAL-01 / CRITICAL-02 / CRITICAL-03。

---

## 🔴 Critical（严重，必须上线前修复）

### CRITICAL-01 · JWT 密钥硬编码且强度不足，可离线伪造任意账号 token

**影响**：掌握源码或镜像的攻击者可签发任意 `account_id / clerk_id / shop_id / role_id` 的合法 JWT，等同于以任意员工身份登录任意门店后台。

**证据**：

- [services/auth-service/src/main/resources/application.yml:27-29](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/resources/application.yml#L27-L29)
  ```yaml
  pets:
    jwt:
      secret: pets-chonglaoban-jwt-secret-key-2026-must-be-32-bytes
      expire-minutes: 720
  ```
- [pets-gateway/src/main/resources/application.yml:17-18](file:///Users/wuyunbin/workspace/pets/pets-gateway/src/main/resources/application.yml#L17-L18) 完全相同的 secret。
- [services/auth-service/src/main/java/com/pets/auth/util/JwtUtil.java:30-32](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/util/JwtUtil.java#L30-L32) `Keys.hmacShaKeyFor(secret.getBytes(UTF_8))`，HS256 仅需 256 bit 密钥，人类可猜字符串 + 公开源码 → 可直接复刻。

**红队利用路径**：
1. 通过 GitHub 仓库、镜像层 `ENV`、k8s ConfigMap 任一渠道拿到 secret 原文。
2. 用 jjwt 0.13.0 一行代码签发 `sub=1, clerk_id=1, shop_id=1, role_id=1` 的 token。
3. 带 `Authorization: Bearer xxx` 走 NodePort 30900 直达网关 → 放行 → 越权。

**修复建议**：
- 密钥改由 **环境变量 `PETS_JWT_SECRET` 注入**，yml 中只写 `${PETS_JWT_SECRET}`，**移除默认值**。
- 密钥长度 ≥ 64 字节随机（`openssl rand -base64 64`），且 auth-service 与 gateway **共享同一密钥源**（推荐 K8s Secret + volume 挂载，而非 env）。
- 上线前 **轮换当前已泄露的密钥**，并审计是否有可疑 token 使用记录。
- 长期：迁移到 **RS256 / EdDSA（非对称）**，签发端私钥只在 auth-service，验证端只需公钥；gateway 与下游服务无密钥泄露面。

---

### CRITICAL-02 · 下游服务直接信任 `X-User-Id` 等请求头，存在头伪造越权

**影响**：攻击者绕过网关（k8s 内部直连 auth-service:9001）或在网关 `mutate().header()` 追加语义下夹带原始头，即可让下游把请求当作任意用户。

**证据**：

- 网关注入逻辑：[pets-gateway/src/main/java/com/pets/gateway/filter/JwtAuthGlobalFilter.java:90-96](file:///Users/wuyunbin/workspace/pets/pets-gateway/src/main/java/com/pets/gateway/filter/JwtAuthGlobalFilter.java#L90-L96)
  ```java
  ServerHttpRequest.Builder builder = exchange.getRequest().mutate()
          .header("X-User-Id", userId)
          .header("X-Trace-Id", traceId);
  ```
  `RequestBuilder.header(name, value)` 默认 **追加**，不会剥离客户端自带的同名头。客户端原始请求带 `X-User-Id: 1` 时，下游会看到多值头。
- 下游信任逻辑：[pets-common/src/main/java/com/pets/common/context/TenantFilter.java:19-24](file:///Users/wuyunbin/workspace/pets/pets-common/src/main/java/com/pets/common/context/TenantFilter.java#L19-L24)
  ```java
  ctx.setAccountId(parse(req.getHeader("X-User-Id")));
  ...
  ```
  `getHeader` 只返回第一个值，但 gateway 的追加行为使客户端原始值可能排在前。
- auth-service 在 k8s 暴露独立 Service：[deploy/k8s/app/auth-service.yaml:80-88](file:///Users/wuyunbin/workspace/pets/deploy/k8s/app/auth-service.yaml#L80-L88)，同 namespace 任意 pod 可直连 `auth-service:9001` 绕过网关。
- auth-service 的 `/select-shop` 接口完全信任该头：[services/auth-service/src/main/java/com/pets/auth/adapter/in/AuthController.java:55-65](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/adapter/in/AuthController.java#L55-L65)。

**红队利用路径**：
- **路径 A（直连）**：在集群内起一个 pod，`curl -H 'X-User-Id: 1' http://auth-service:9001/api/v1/auth/select-shop -d '{"clerkId":1}'`。
- **路径 B（头夹带）**：从外部经 gateway 请求，header 中同时带 `Authorization: Bearer <有效token>` 与 `X-User-Id: 1`，下游可能取到攻击者注入的值。

**修复建议**：
- 网关侧使用 `RequestBuilder.headers(headers -> headers.remove("X-User-Id").remove("X-Shop-Id")...)` 先剥离再添加，或直接 `mutate().request(r -> r.headers(h -> { h.remove(...); h.add(...); }))`。
- auth-service 侧增加 **NetworkPolicy** 限制只允许 `app/pets-gateway` 访问 9001 端口。
- 引入 **共享密钥签名头**：gateway 给下游的 `X-User-Id` 等 header 加 HMAC 签名（`X-Auth-Sig: HMAC-SHA256(secret, user_id|shop_id|...)`），下游校验签名后才采信；可封装进 `pets-common` 的 `TenantFilter`。
- 长期：改用 mTLS + SPIFFE/SPIRE 做服务身份认证，请求头只传标识不传权限。

---

### CRITICAL-03 · 短信验证码可被暴力枚举 + 短信轰炸 + 明文落日志

**影响**：红队可在分钟级内枚举任意手机号的验证码完成注册/重置密码接管账号；同时可对任意手机号发动短信轰炸。

**证据**：

- 验证码比对无错误次数计数：[services/auth-service/src/main/java/com/pets/auth/application/AuthService.java:142-146](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/application/AuthService.java#L142-L146) 与 [AuthService.java:168-172](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/application/AuthService.java#L168-L172)
  ```java
  String cached = stringRedisTemplate.opsForValue().get(key);
  if (cached == null || !cached.equals(req.getCode())) {
      throw new BizException("验证码错误或已过期");
  }
  ```
  Redis 中 code 是明文存储，6 位数字 = 100 万种组合，未限制尝试次数。
- 发码接口无频率限制：[AuthService.java:184-204](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/application/AuthService.java#L184-L204) 未检查同一手机号/IP 的发送间隔。
- **验证码明文落日志**：[AuthService.java:203](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/application/AuthService.java#L203)
  ```java
  log.info("SMS code (mock) for {}: {}", req.getPhone(), code);
  ```
  任何能读应用日志的人即可拿到所有验证码。
- 验证码用 `equals` 比对明文，未做**恒定时间比较**，存在理论时序侧信道。

**红队利用路径**：
1. 调用 `/api/v1/auth/send-code` 触发发送。
2. 并发打 `/api/v1/auth/register` 或 `/reset-password` 穷举 6 位 code，无任何拦截。
3. 命中后完成注册/重置，接管账号。

**修复建议**：
- **限流**：同一手机号 + bizType 组合 60s 内只能发一次，10 分钟内最多 3 次；同一 IP 每日最多 10 次。可用 Redis 自增 key + TTL 实现。
- **错误次数计数**：`sms:err:{bizType}:{phone}` 每错误一次自增并设 300s TTL，≥5 次直接拒绝并锁定手机号 30 分钟。
- **验证码存储**：Redis 中只存 BCrypt 哈希；比较改用 `passwordEncoder.matches(raw, hash)`（BCrypt 自身恒定时间）。
- **日志脱敏**：彻底删除验证码明文日志，最多打印 `phone` 末四位。
- **强度提升**：6 位扩展为 6 位字母数字混合（区分易混字符），或保留 6 位但叠加 IP 维度强制限流。

---

## 🟠 High（高危，建议尽快修复）

### HIGH-01 · 登录无频率限制 + 手机号枚举

**证据**：[AuthService.java:62-70](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/application/AuthService.java#L62-L70)
- 账号不存在抛"账号不存在"，密码错抛"手机号或密码错误"——可枚举手机号确认是否注册。
- 无登录失败次数限制，BCrypt 默认 strength=10，红队 GPU 可每秒数万次爆破弱密码。

**修复**：统一对外文案为"手机号或密码错误"；同一账号 5 分钟连续失败 ≥5 次锁定 30 分钟；BCrypt strength 提升至 12（[AuthService.java:60](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/application/AuthService.java#L60)）。

### HIGH-02 · k8s 部署清单明文写入所有中间件凭据

**证据**：
- [deploy/k8s/app/auth-service.yaml:31-55](file:///Users/wuyunbin/workspace/pets/deploy/k8s/app/auth-service.yaml#L31-L55)：`SPRING_DATASOURCE_PASSWORD=root`、`SPRING_MONGODB_URI=mongodb://root:root@...`、`NACOS_USERNAME/PASSWORD=nacos/nacos` 全部明文。
- [deploy/k8s/app/gateway.yaml:33-39](file:///Users/wuyunbin/workspace/pets/deploy/k8s/app/gateway.yaml#L33-L39)：Nacos 默认凭据。

**影响**：任何能 `kubectl get pod -o yaml` 或读 Git 的人都能拿到所有中间件 root 凭据；Nacos 被接管可读所有应用配置（含 JWT secret 若未来外置）。

**修复**：
- 改用 K8s Secret + `envFrom: secretRef` 或 `valueFrom.secretKeyRef`。
- 中间件密码改强随机串，MySQL/MongoDB/Nacos 全部禁用 root 直连，按服务建独立账号最小授权。
- Git 仓库 git历史中已有的明文凭据视为已泄露，须立即轮换。

### HIGH-03 · MongoDB / MySQL 弱口令 + useSSL=false + allowPublicKeyRetrieval=true

**证据**：
- [services/auth-service/src/main/resources/application.yml:6-16](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/resources/application.yml#L6-L16)：`root/root123`、`useSSL=false&allowPublicKeyRetrieval=true`。
- 生产清单沿用同样凭据：[auth-service.yaml:34-48](file:///Users/wuyunbin/workspace/pets/deploy/k8s/app/auth-service.yaml#L34-L48)。

**影响**：跨 namespace 流量明文传输 MySQL/MongoDB 凭据；`allowPublicKeyRetrieval=true` 接受服务端下发公钥，配合 MITM 可实施 RSA 密钥窃取。

**修复**：生产 MySQL 强制 `useSSL=true&requireSSL=true&verifyServerCertificate=true`；MongoDB URI 加 `?tls=true&tlsAllowInvalidCertificates=false`；账号改强口令。

### HIGH-04 · Actuator 端点未显式限制暴露面

**证据**：
- 白名单放通 `/actuator/health`：[pets-gateway/src/main/resources/application.yml:16](file:///Users/wuyunbin/workspace/pets/pets-gateway/src/main/resources/application.yml#L16)。
- auth-service 与 gateway 的 application.yml 均**未配置** `management.endpoints.web.exposure.include` / `management.endpoint.health.show-details`。
- Spring Boot 4 默认仅暴露 `health`，但 `health` 子端点默认 `show-details=never` 时仍会泄露依赖状态；若未来开发者为调试临时改成 `always`，会泄露 MySQL/Redis/MongoDB/Nacos 连通性与错误堆栈。

**修复**：显式 `management.endpoints.web.exposure.include=health`、`management.endpoint.health.show-details=never`、`management.endpoints.web.base-path=/internal-actuator` 并相应更新白名单，避免与公开路径冲突。

### HIGH-05 · 预授权 token 可被复用访问任意鉴权接口

**证据**：[AuthService.java:102-113](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/application/AuthService.java#L102-L113) 与 [AuthService.java:163](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/application/AuthService.java#L163)：多岗位与注册场景签发 `generateToken(accountId, 0L, 0L, 0L, 0L)` 的"预授权 token"，**该 token 签名合法、过期 720 分钟**，可被用于访问任意 `/api/v1/**` 鉴权路径。

**影响**：新注册用户在创建岗位前，已持有可通过网关鉴权的 token；`X-Shop-Id=0` 下游可能返回跨租户数据。

**修复**：
- JWT claims 增加 `type=pre_auth`，网关对 `type=pre_auth` 的 token 仅允许 `/api/v1/auth/select-shop` 路径，其余路径 403。
- 或：选店/注册阶段不签 JWT，改下发一次性 `select_token`（Redis 存 5 分钟），仅 `/select-shop` 接受，验证后换取正式 JWT。

### HIGH-06 · Gateway NodePort 直接对外暴露明文 HTTP

**证据**：[deploy/k8s/app/gateway.yaml:69-77](file:///Users/wuyunbin/workspace/pets/deploy/k8s/app/gateway.yaml#L69-L77) `type: NodePort` + `nodePort: 30900`，未配置 Ingress/TLS。

**影响**：JWT 在公网明文传输，红队 MITM 即可窃取；NodePort 直接暴露绕过 Ingress 的 WAF/限流。

**修复**：用 Ingress + cert-manager 自动签发 TLS 证书；或 gateway 内嵌 TLS；同时限制 NodePort 仅对内网开放。

---

## 🟡 Medium（中危，迭代修复）

### MED-01 · JWT 缺失 `iss/aud/jti/exp` 签发约定，无法撤销与防重放

**证据**：[JwtUtil.java:34-47](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/util/JwtUtil.java#L34-L47)。
- 无 `jti`：登出后 token 仍可用 720 分钟。
- 无 `iss/aud`：token 可跨服务复用。
- `expire-minutes:720`（12 小时）偏长。

**修复**：加 `jti`（UUID）、`iss=pets-auth`、`aud` 按服务区分；登录/登出时维护 Redis 黑名单；缩短 access token 至 30 分钟，配套 refresh token。

### MED-02 · 登录日志明文存储手机号，违反个保法

**证据**：[AuthService.java:223](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/application/AuthService.java#L223) `loginLog.setPhone(account.getPhone())`，[LoginLog.java:30](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/domain/model/LoginLog.java#L30) 明文字段。

**修复**：存脱敏后值（前 3 后 4）或对 phone 做可逆加密（KMS 管控密钥），日志与查询分别处理。

### MED-03 · TraceId 接受客户端任意输入，可造成日志注入

**证据**：[pets-common/src/main/java/com/pets/common/log/TraceIdFilter.java:26-31](file:///Users/wuyunbin/workspace/pets/pets-common/src/main/java/com/pets/common/log/TraceIdFilter.java#L26-L31) 与 [JwtAuthGlobalFilter.java:85-88](file:///Users/wuyunbin/workspace/pets/pets-gateway/src/main/java/com/pets/gateway/filter/JwtAuthGlobalFilter.java#L85-L88)，客户端传入的 `X-Trace-Id` 未经校验直接进 MDC 与响应头，红队可注入 `\n` 换行伪造日志行。

**修复**：校验 `^[A-Za-z0-9-]{8,64}$`，非法则丢弃重生成。

### MED-04 · Nacos 使用默认凭据 nacos/nacos

**证据**：[bootstrap.yml:8-13](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/resources/bootstrap.yml#L8-L13)（auth-service）与 gateway 同款；生产清单亦如此。
- Nacos 3.x 默认开启鉴权，但默认凭据本身就是公开的；一旦被攻陷，可读取/篡改所有服务配置。

**修复**：部署后强制改 Nacos 管理员口令（已在 project_memory 记录）；客户端账号按服务建独立只读账号；网络层限制 8848 仅对 app namespace 开放。

### MED-05 · BCrypt 默认强度（strength=10）

**证据**：[AuthService.java:60](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/application/AuthService.java#L60) `new BCryptPasswordEncoder()` 默认 10。

**修复**：`new BCryptPasswordEncoder(12)`（约 2 倍 GPU 成本）；同时配套 HIGH-01 的登录限流。

### MED-06 · `GlobalExceptionHandler` 兜底异常未记日志，难以取证

**证据**：[pets-common/src/main/java/com/pets/common/exception/GlobalExceptionHandler.java:32-35](file:///Users/wuyunbin/workspace/pets/pets-common/src/main/java/com/pets/common/exception/GlobalExceptionHandler.java#L32-L35) `handleException` 直接返回"系统异常"，无 `log.error`。

**影响**：红队探测时 500 错误不会被记录，安全运营无法回溯攻击。

**修复**：补 `log.error("unhandled exception", e)`，但**不要**把堆栈/类名透出到响应体。

---

## 🟢 Low（低危，可纳入技术债）

### LOW-01 · Dockerfile 用 `sh -c` 启动，JAVA_OPTS 可被注入
[services/auth-service/Dockerfile:18](file:///Users/wuyunbin/workspace/pets/services/auth-service/Dockerfile#L18)：`ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]`。若 k8s env 被攻击者注入恶意 JAVA_OPTS（如 `-Xlog:... -javaagent:@evil.jar`），可被利用。建议改 `ENTRYPOINT ["java", "-jar", "app.jar"]`，JVM 参数走 `JAVA_TOOL_OPTIONS` 环境变量由 JVM 原生处理。

### LOW-02 · 未配置 CORS
当前未见 CorsWebFilter 配置。前端跨域会失败，但不会因 `*` 通配出问题。建议显式配置允许的前端域名，禁用通配与 credentials。

### LOW-03 · 未引入依赖漏洞扫描
[pom.xml](file:///Users/wuyunbin/workspace/pets/pom.xml) 未配置 `org.owasp:dependency-check-maven` 或 `snyk` 插件，无法在构建期发现 CVE。建议接入 OWASP Dependency-Check 并设 failOnError。

---

## 修复优先级路线图

| 阶段 | 包含项 | 目标 |
|------|--------|------|
| P0（上线前） | CRITICAL-01/02/03 + HIGH-02/03/05/06 | 关闭可被外部直接利用的接管与越权链 |
| P1（上线后 1 周） | HIGH-01/04 + MED-01/03/04/05/06 | 收紧登录与日志侧信道、补齐撤销机制 |
| P2（迭代） | MED-02 + LOW-01/02/03 | 合规与工程规范 |

---

## 附：审查中确认的合规项（已做对的部分）

- MyBatis 全部走 `LambdaQueryWrapper` 与 `#{}` 占位符，无 SQL 字符串拼接，无注入面（[AccountMapper.java](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/adapter/out/AccountMapper.java)、[SegmentRepository.java:12](file:///Users/wuyunbin/workspace/pets/pets-id-generator/src/main/java/com/pets/id/repo/SegmentRepository.java#L12)）。
- 密码使用 BCrypt 哈希存储，未明文落库（[AuthService.java:156](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/application/AuthService.java#L156)）。
- 业务表统一加 `@Version` 乐观锁（[Account.java:31-32](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/domain/entity/Account.java#L31-L32)）。
- MongoDB `sms_code` / `login_log` 用 TTL 索引自动过期（[SmsCode.java:31-33](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/domain/model/SmsCode.java#L31-L33)、[LoginLog.java:43-45](file:///Users/wuyunbin/workspace/pets/services/auth-service/src/main/java/com/pets/auth/domain/model/LoginLog.java#L43-L45)）。
- 全局异常兜底未泄露堆栈（[GlobalExceptionHandler.java:32-35](file:///Users/wuyunbin/workspace/pets/pets-common/src/main/java/com/pets/common/exception/GlobalExceptionHandler.java#L32-L35)）。
- JWT 解析捕获 `JwtException`，未向客户端透出具体原因（[JwtAuthGlobalFilter.java:73-75](file:///Users/wuyunbin/workspace/pets/pets-gateway/src/main/java/com/pets/gateway/filter/JwtAuthGlobalFilter.java#L73-L75)）。
- 网关白名单显式列出公开端点，未用通配（[application.yml:10-16](file:///Users/wuyunbin/workspace/pets/pets-gateway/src/main/resources/application.yml#L10-L16)）。

---

*报告生成于 2026-09-20 · 贾维斯 · 红队视角*
