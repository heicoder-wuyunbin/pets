package com.pets.system.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.id.service.IdService;
import com.pets.system.adapter.out.AccountMapper;
import com.pets.system.adapter.out.ClerkMapper;
import com.pets.system.adapter.out.RoleMapper;
import com.pets.system.application.dto.ClerkAddReq;
import com.pets.system.application.dto.ClerkEditReq;
import com.pets.system.application.dto.ClerkPageReq;
import com.pets.system.application.dto.ClerkResetPwdReq;
import com.pets.system.application.dto.ClerkStatusReq;
import com.pets.system.application.dto.ClerkVO;
import com.pets.system.domain.entity.Account;
import com.pets.system.domain.entity.Clerk;
import com.pets.system.domain.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 员工管理服务：分页/新增/编辑/状态切换/重置密码。
 */
@Service
@RequiredArgsConstructor
public class ClerkService {

    private final ClerkMapper clerkMapper;
    private final AccountMapper accountMapper;
    private final RoleMapper roleMapper;
    private final IdService idService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public Page<ClerkVO> page(ClerkPageReq req) {
        Long shopId = currentShopId();
        LambdaQueryWrapper<Clerk> wrapper = new LambdaQueryWrapper<Clerk>()
                .eq(Clerk::getShopId, shopId)
                .orderByDesc(Clerk::getCreateTime);
        if (req.getStatus() != null) {
            wrapper.eq(Clerk::getStatus, req.getStatus());
        }

        Page<Clerk> page = clerkMapper.selectPage(
                new Page<>(req.getPageNum(), req.getPageSize()), wrapper);

        // 收集 accountId 与 roleId，批量查询 account 和 role
        Map<Long, Account> accountMap = Map.of();
        Map<Long, Role> roleMap = Map.of();
        if (!page.getRecords().isEmpty()) {
            var accountIds = page.getRecords().stream().map(Clerk::getAccountId).toList();
            var roleIds = page.getRecords().stream().map(Clerk::getRoleId).toList();
            accountMap = accountMapper.selectBatchIds(accountIds).stream()
                    .collect(Collectors.toMap(Account::getId, a -> a));
            roleMap = roleMapper.selectBatchIds(roleIds).stream()
                    .collect(Collectors.toMap(Role::getId, r -> r));
        }

        // 关键字过滤（在内存中按姓名/手机号过滤，简化实现）
        Map<Long, Account> finalAccountMap = accountMap;
        Map<Long, Role> finalRoleMap = roleMap;
        var filtered = page.getRecords().stream()
                .filter(c -> {
                    if (req.getKeyword() == null || req.getKeyword().isBlank()) {
                        return true;
                    }
                    Account acc = finalAccountMap.get(c.getAccountId());
                    String kw = req.getKeyword();
                    return (acc != null && (acc.getPhone() != null && acc.getPhone().contains(kw)
                            || acc.getTruename() != null && acc.getTruename().contains(kw)));
                })
                .toList();

        Page<ClerkVO> result = new Page<>(page.getCurrent(), page.getSize(), filtered.size());
        result.setRecords(filtered.stream().map(c -> toVO(c, finalAccountMap, finalRoleMap)).toList());
        return result;
    }

    public void add(ClerkAddReq req) {
        Long shopId = currentShopId();
        Long chainGroupId = currentChainGroupId();

        // 手机号唯一性校验
        Account existing = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getPhone, req.getPhone()));
        if (existing != null) {
            throw new BizException("手机号已存在");
        }

        // 创建账号
        Account account = new Account();
        account.setId(idService.next("account"));
        account.setPhone(req.getPhone());
        account.setPassword(passwordEncoder.encode(req.getPassword()));
        account.setTruename(req.getTruename());
        account.setStatus(1);
        accountMapper.insert(account);

        // 创建雇佣关系
        Clerk clerk = new Clerk();
        clerk.setId(idService.next("clerk"));
        clerk.setAccountId(account.getId());
        clerk.setShopId(shopId);
        clerk.setChainGroupId(chainGroupId);
        clerk.setRoleId(req.getRoleId());
        clerk.setIsSuper(0);
        clerk.setStatus(1);
        clerk.setHireDate(LocalDate.now());
        clerkMapper.insert(clerk);
    }

    public void edit(ClerkEditReq req) {
        Clerk clerk = getOwnedClerk(req.getId());
        if (req.getTruename() != null) {
            Account account = accountMapper.selectById(clerk.getAccountId());
            if (account != null) {
                account.setTruename(req.getTruename());
                accountMapper.updateById(account);
            }
        }
        if (req.getRoleId() != null) {
            clerk.setRoleId(req.getRoleId());
            clerkMapper.updateById(clerk);
        }
    }

    public void status(ClerkStatusReq req) {
        Clerk clerk = getOwnedClerk(req.getId());
        clerk.setStatus(req.getStatus());
        if (req.getStatus() == 0) {
            clerk.setLeaveDate(LocalDate.now());
        }
        clerkMapper.updateById(clerk);
    }

    public void resetPwd(ClerkResetPwdReq req) {
        Clerk clerk = getOwnedClerk(req.getId());
        Account account = accountMapper.selectById(clerk.getAccountId());
        if (account == null) {
            throw new BizException("账号不存在");
        }
        account.setPassword(passwordEncoder.encode(req.getNewPassword()));
        accountMapper.updateById(account);
    }

    private Clerk getOwnedClerk(Long id) {
        Clerk clerk = clerkMapper.selectById(id);
        if (clerk == null) {
            throw new BizException("员工不存在");
        }
        if (!clerk.getShopId().equals(currentShopId())) {
            throw new BizException("无权操作该员工");
        }
        return clerk;
    }

    private ClerkVO toVO(Clerk clerk, Map<Long, Account> accountMap, Map<Long, Role> roleMap) {
        ClerkVO vo = new ClerkVO();
        vo.setId(clerk.getId());
        vo.setAccountId(clerk.getAccountId());
        vo.setRoleId(clerk.getRoleId());
        vo.setIsSuper(clerk.getIsSuper());
        vo.setStatus(clerk.getStatus());
        vo.setHireDate(clerk.getHireDate());
        Account acc = accountMap.get(clerk.getAccountId());
        if (acc != null) {
            vo.setPhone(acc.getPhone());
            vo.setTruename(acc.getTruename());
        }
        Role role = roleMap.get(clerk.getRoleId());
        if (role != null) {
            vo.setRoleName(role.getRoleName());
        }
        return vo;
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) {
            throw new BizException("未登录或租户上下文缺失");
        }
        return ctx.getShopId();
    }

    private Long currentChainGroupId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        return ctx != null ? ctx.getChainGroupId() : null;
    }
}
