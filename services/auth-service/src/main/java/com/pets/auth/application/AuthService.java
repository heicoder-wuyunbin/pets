package com.pets.auth.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pets.auth.adapter.out.AccountMapper;
import com.pets.auth.adapter.out.ChainGroupMapper;
import com.pets.auth.adapter.out.ClerkMapper;
import com.pets.auth.adapter.out.RoleMapper;
import com.pets.auth.adapter.out.ShopMapper;
import com.pets.auth.adapter.out.mongo.LoginLogRepository;
import com.pets.auth.adapter.out.mongo.SmsCodeRepository;
import com.pets.auth.application.dto.LoginReq;
import com.pets.auth.application.dto.LoginResp;
import com.pets.auth.application.dto.RegisterReq;
import com.pets.auth.application.dto.ResetPasswordReq;
import com.pets.auth.application.dto.SelectShopReq;
import com.pets.auth.application.dto.SendCodeReq;
import com.pets.auth.application.dto.ShopOption;
import com.pets.auth.domain.entity.Account;
import com.pets.auth.domain.entity.Clerk;
import com.pets.auth.domain.entity.Role;
import com.pets.auth.domain.entity.Shop;
import com.pets.auth.domain.model.LoginLog;
import com.pets.auth.domain.model.SmsCode;
import com.pets.auth.util.JwtUtil;
import com.pets.common.exception.BizException;
import com.pets.id.service.IdService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 鉴权核心服务：登录/选店/注册/重置密码/发码。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AccountMapper accountMapper;
    private final ClerkMapper clerkMapper;
    private final RoleMapper roleMapper;
    private final ShopMapper shopMapper;
    private final ChainGroupMapper chainGroupMapper;
    private final SmsCodeRepository smsCodeRepository;
    private final LoginLogRepository loginLogRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtUtil jwtUtil;
    private final IdService idService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public LoginResp login(LoginReq req) {
        Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getPhone, req.getPhone()));
        if (account == null) {
            throw new BizException("账号不存在");
        }
        if (!passwordEncoder.matches(req.getPassword(), account.getPassword())) {
            throw new BizException("手机号或密码错误");
        }

        List<Clerk> clerks = clerkMapper.selectList(
                new LambdaQueryWrapper<Clerk>()
                        .eq(Clerk::getAccountId, account.getId())
                        .eq(Clerk::getStatus, 1));
        if (clerks.isEmpty()) {
            throw new BizException("无有效岗位");
        }

        String ip = currentIp();
        String userAgent = currentUserAgent();

        // 单一在职岗位：直接签发带租户上下文的 JWT
        if (clerks.size() == 1) {
            Clerk clerk = clerks.get(0);
            Shop shop = shopMapper.selectById(clerk.getShopId());
            Role role = roleMapper.selectById(clerk.getRoleId());
            String token = jwtUtil.generateToken(account.getId(), clerk.getId(),
                    clerk.getShopId(), clerk.getChainGroupId(), clerk.getRoleId());
            saveLoginLog(account, clerk, ip, userAgent, 1, null);
            return new LoginResp(
                    token,
                    clerk.getShopId(),
                    shop != null ? shop.getShopName() : null,
                    role != null ? role.getRoleCode() : null,
                    role != null ? role.getRoleName() : null,
                    false,
                    null);
        }

        // 多岗位：下发预授权 token（仅 accountId），返回选店列表
        String preAuthToken = jwtUtil.generateToken(account.getId(), 0L, 0L, 0L, 0L);
        List<ShopOption> options = clerks.stream().map(clerk -> {
            Shop shop = shopMapper.selectById(clerk.getShopId());
            Role role = roleMapper.selectById(clerk.getRoleId());
            return new ShopOption(
                    clerk.getId(),
                    clerk.getShopId(),
                    shop != null ? shop.getShopName() : null,
                    role != null ? role.getRoleName() : null);
        }).toList();
        saveLoginLog(account, null, ip, userAgent, 1, null);
        return new LoginResp(preAuthToken, null, null, null, null, true, options);
    }

    public LoginResp selectShop(SelectShopReq req, Long accountId) {
        Clerk clerk = clerkMapper.selectById(req.getClerkId());
        if (clerk == null) {
            throw new BizException("岗位不存在");
        }
        if (!Integer.valueOf(1).equals(clerk.getStatus())) {
            throw new BizException("该岗位已离职");
        }
        if (!clerk.getAccountId().equals(accountId)) {
            throw new BizException("无权选择该岗位");
        }
        Shop shop = shopMapper.selectById(clerk.getShopId());
        Role role = roleMapper.selectById(clerk.getRoleId());
        String token = jwtUtil.generateToken(accountId, clerk.getId(),
                clerk.getShopId(), clerk.getChainGroupId(), clerk.getRoleId());
        return new LoginResp(
                token,
                clerk.getShopId(),
                shop != null ? shop.getShopName() : null,
                role != null ? role.getRoleCode() : null,
                role != null ? role.getRoleName() : null,
                false,
                null);
    }

    public LoginResp register(RegisterReq req) {
        String key = "sms:code:register:" + req.getPhone();
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached == null || !cached.equals(req.getCode())) {
            throw new BizException("验证码错误或已过期");
        }
        Long count = accountMapper.selectCount(
                new LambdaQueryWrapper<Account>().eq(Account::getPhone, req.getPhone()));
        if (count != null && count > 0) {
            throw new BizException("手机号已注册");
        }

        Account account = new Account();
        account.setId(idService.next("account"));
        account.setPhone(req.getPhone());
        account.setPassword(passwordEncoder.encode(req.getPassword()));
        account.setStatus(1);
        accountMapper.insert(account);

        markSmsCodeUsed(req.getPhone(), "register");

        // 新用户尚无岗位，下发预授权 token（仅 accountId）
        String token = jwtUtil.generateToken(account.getId(), 0L, 0L, 0L, 0L);
        return new LoginResp(token, null, null, null, null, false, null);
    }

    public void resetPassword(ResetPasswordReq req) {
        String key = "sms:code:reset_password:" + req.getPhone();
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached == null || !cached.equals(req.getCode())) {
            throw new BizException("验证码错误或已过期");
        }
        Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getPhone, req.getPhone()));
        if (account == null) {
            throw new BizException("账号不存在");
        }
        account.setPassword(passwordEncoder.encode(req.getNewPassword()));
        accountMapper.updateById(account);

        markSmsCodeUsed(req.getPhone(), "reset_password");
    }

    public void sendCode(SendCodeReq req) {
        if (!"register".equals(req.getBizType()) && !"reset_password".equals(req.getBizType())) {
            throw new BizException("不支持的验证码业务类型");
        }
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        String key = "sms:code:" + req.getBizType() + ":" + req.getPhone();
        stringRedisTemplate.opsForValue().set(key, code, Duration.ofSeconds(300));

        SmsCode smsCode = new SmsCode();
        smsCode.setPhone(req.getPhone());
        smsCode.setBizType(req.getBizType());
        smsCode.setCodeHash(passwordEncoder.encode(code));
        smsCode.setIp(currentIp());
        smsCode.setExpireAt(Instant.now().plusSeconds(300));
        smsCode.setUsed(false);
        smsCode.setCreateTime(Instant.now());
        smsCodeRepository.save(smsCode);

        // 模拟下发：仅打印，不真实发送短信
        log.info("SMS code (mock) for {}: {}", req.getPhone(), code);
    }

    private void markSmsCodeUsed(String phone, String bizType) {
        List<SmsCode> list = smsCodeRepository
                .findByPhoneAndBizTypeOrderByCreateTimeDesc(phone, bizType);
        if (!list.isEmpty()) {
            SmsCode latest = list.get(0);
            latest.setUsed(true);
            latest.setUsedAt(Instant.now());
            smsCodeRepository.save(latest);
        }
    }

    private void saveLoginLog(Account account, Clerk clerk, String ip, String userAgent,
                              Integer loginStatus, String failReason) {
        LoginLog loginLog = new LoginLog();
        loginLog.setAccountId(account.getId());
        loginLog.setClerkId(clerk != null ? clerk.getId() : null);
        loginLog.setShopId(clerk != null ? clerk.getShopId() : null);
        loginLog.setPhone(account.getPhone());
        loginLog.setLoginStatus(loginStatus);
        loginLog.setFailReason(failReason);
        loginLog.setIp(ip);
        loginLog.setUserAgent(userAgent);
        loginLog.setCreateTime(Instant.now());
        loginLogRepository.save(loginLog);
    }

    private String currentIp() {
        HttpServletRequest request = currentRequest();
        if (request == null) {
            return null;
        }
        String ip = request.getHeader("X-Forwarded-For");
        return (ip != null && !ip.isBlank()) ? ip : request.getRemoteAddr();
    }

    private String currentUserAgent() {
        HttpServletRequest request = currentRequest();
        return request != null ? request.getHeader("User-Agent") : null;
    }

    private HttpServletRequest currentRequest() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) {
            return sra.getRequest();
        }
        return null;
    }
}
