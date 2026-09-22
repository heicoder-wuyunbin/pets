package com.pets.member.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pets.common.exception.BizException;
import com.pets.member.adapter.out.MemberAccountLogMapper;
import com.pets.member.adapter.out.MemberAccountMapper;
import com.pets.member.application.dto.AccountLogVO;
import com.pets.member.domain.entity.MemberAccount;
import com.pets.member.domain.entity.MemberAccountLog;
import com.pets.id.service.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 账户服务：余额扣费（供收银）、累计消费、积分累加/抵现、流水查询。
 */
@Service
@RequiredArgsConstructor
public class AccountService {

    private final MemberAccountMapper accountMapper;
    private final MemberAccountLogMapper logMapper;
    private final IdService idService;

    /**
     * 余额扣费（收银用余额支付）。
     * 防透支：余额不足抛异常。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deductBalance(Long memberId, BigDecimal amount, Long orderId) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("扣费金额必须大于0");
        }
        MemberAccount acc = accountMapper.selectOne(
                new LambdaQueryWrapper<MemberAccount>().eq(MemberAccount::getMemberId, memberId)
                        .last("FOR UPDATE"));
        if (acc == null) throw new BizException("会员账户不存在");
        if (acc.getBalance().compareTo(amount) < 0) {
            throw new BizException("余额不足，当前余额：" + acc.getBalance());
        }
        acc.setBalance(acc.getBalance().subtract(amount));
        acc.setTotalConsume(acc.getTotalConsume().add(amount));
        int updated = accountMapper.updateById(acc);
        if (updated == 0) throw new BizException("扣费并发冲突，请重试");

        saveLog(memberId, 2, amount.negate(), acc.getBalance(), 0, acc.getPoints(), orderId, "消费扣费");
    }

    /**
     * 退款回补余额。
     */
    @Transactional(rollbackFor = Exception.class)
    public void refundBalance(Long memberId, BigDecimal amount, Long orderId) {
        MemberAccount acc = accountMapper.selectOne(
                new LambdaQueryWrapper<MemberAccount>().eq(MemberAccount::getMemberId, memberId)
                        .last("FOR UPDATE"));
        if (acc == null) throw new BizException("会员账户不存在");
        acc.setBalance(acc.getBalance().add(amount));
        acc.setTotalConsume(acc.getTotalConsume().subtract(amount));
        accountMapper.updateById(acc);
        saveLog(memberId, 3, amount, acc.getBalance(), 0, acc.getPoints(), orderId, "退款回补");
    }

    /**
     * 累加积分（收银支付后调用，本期同步调用）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void addPoints(Long memberId, int points, Long orderId) {
        if (points <= 0) return;
        MemberAccount acc = accountMapper.selectOne(
                new LambdaQueryWrapper<MemberAccount>().eq(MemberAccount::getMemberId, memberId)
                        .last("FOR UPDATE"));
        if (acc == null) throw new BizException("会员账户不存在");
        acc.setPoints(acc.getPoints() + points);
        accountMapper.updateById(acc);
        saveLog(memberId, 4, BigDecimal.ZERO, acc.getBalance(), points, acc.getPoints(), orderId, "消费得积分");
    }

    /**
     * 积分抵现（收银用积分抵扣金额）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void deductPoints(Long memberId, int points, Long orderId) {
        if (points <= 0) return;
        MemberAccount acc = accountMapper.selectOne(
                new LambdaQueryWrapper<MemberAccount>().eq(MemberAccount::getMemberId, memberId)
                        .last("FOR UPDATE"));
        if (acc == null) throw new BizException("会员账户不存在");
        if (acc.getPoints() < points) {
            throw new BizException("积分不足，当前积分：" + acc.getPoints());
        }
        acc.setPoints(acc.getPoints() - points);
        accountMapper.updateById(acc);
        saveLog(memberId, 5, BigDecimal.ZERO, acc.getBalance(), -points, acc.getPoints(), orderId, "积分抵现");
    }

    public MemberAccount getAccount(Long memberId) {
        return accountMapper.selectOne(
                new LambdaQueryWrapper<MemberAccount>().eq(MemberAccount::getMemberId, memberId));
    }

    public List<AccountLogVO> listLogs(Long memberId, Integer type) {
        LambdaQueryWrapper<MemberAccountLog> wrapper = new LambdaQueryWrapper<MemberAccountLog>()
                .eq(MemberAccountLog::getMemberId, memberId)
                .orderByDesc(MemberAccountLog::getCreateTime);
        if (type != null) wrapper.eq(MemberAccountLog::getType, type);
        return logMapper.selectList(wrapper).stream().map(l -> {
            AccountLogVO vo = new AccountLogVO();
            vo.setId(l.getId());
            vo.setType(l.getType());
            vo.setAmount(l.getAmount());
            vo.setBalanceAfter(l.getBalanceAfter());
            vo.setPoints(l.getPoints());
            vo.setPointsAfter(l.getPointsAfter());
            vo.setOrderId(l.getOrderId());
            vo.setRemark(l.getRemark());
            vo.setCreateTime(l.getCreateTime());
            return vo;
        }).toList();
    }

    private void saveLog(Long memberId, int type, BigDecimal amount, BigDecimal balanceAfter,
                         int points, int pointsAfter, Long orderId, String remark) {
        MemberAccountLog log = new MemberAccountLog();
        log.setId(idService.next("member_account_log"));
        log.setMemberId(memberId);
        log.setType(type);
        log.setAmount(amount);
        log.setBalanceAfter(balanceAfter);
        log.setPoints(points);
        log.setPointsAfter(pointsAfter);
        log.setOrderId(orderId);
        log.setRemark(remark);
        logMapper.insert(log);
    }
}
