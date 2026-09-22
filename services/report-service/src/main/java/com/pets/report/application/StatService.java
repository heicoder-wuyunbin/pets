package com.pets.report.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.report.adapter.out.MemberAccountLogROMapper;
import com.pets.report.adapter.out.MemberROMapper;
import com.pets.report.adapter.out.SaleOrderROMapper;
import com.pets.report.application.dto.LostMemberVO;
import com.pets.report.application.dto.MemberStatVO;
import com.pets.report.application.dto.TrendVO;
import com.pets.report.domain.entity.MemberAccountLogRO;
import com.pets.report.domain.entity.MemberRO;
import com.pets.report.domain.entity.SaleOrderRO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 经营统计服务：会员统计、充值消费趋势、流失预警。
 */
@Service
@RequiredArgsConstructor
public class StatService {

    private final MemberROMapper memberMapper;
    private final SaleOrderROMapper orderMapper;
    private final MemberAccountLogROMapper logMapper;

    public MemberStatVO memberStat() {
        Long shopId = currentShopId();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long total = memberMapper.selectCount(
                new LambdaQueryWrapper<MemberRO>().eq(MemberRO::getShopId, shopId));
        long newToday = memberMapper.selectCount(
                new LambdaQueryWrapper<MemberRO>().eq(MemberRO::getShopId, shopId)
                        .ge(MemberRO::getCreateTime, todayStart));

        // 今日活跃会员（有消费订单）
        List<SaleOrderRO> todayOrders = orderMapper.selectList(
                new LambdaQueryWrapper<SaleOrderRO>().eq(SaleOrderRO::getShopId, shopId)
                        .eq(SaleOrderRO::getStatus, 1)
                        .ge(SaleOrderRO::getCreateTime, todayStart));
        long activeToday = todayOrders.stream()
                .map(SaleOrderRO::getMemberId).filter(id -> id != null && id > 0).distinct().count();

        // 等级分布
        List<MemberRO> all = memberMapper.selectList(
                new LambdaQueryWrapper<MemberRO>().eq(MemberRO::getShopId, shopId));
        Map<Integer, Long> levelMap = all.stream()
                .collect(Collectors.groupingBy(MemberRO::getMemberLevel, Collectors.counting()));
        List<MemberStatVO.LevelDist> levelDist = levelMap.entrySet().stream()
                .map(e -> {
                    MemberStatVO.LevelDist d = new MemberStatVO.LevelDist();
                    d.setLevel(e.getKey());
                    d.setCount(e.getValue());
                    return d;
                }).sorted(Comparator.comparing(MemberStatVO.LevelDist::getLevel)).toList();

        MemberStatVO vo = new MemberStatVO();
        vo.setTotalMember(total);
        vo.setNewMemberToday(newToday);
        vo.setActiveMemberToday(activeToday);
        vo.setLevelDist(levelDist);
        return vo;
    }

    public List<TrendVO> trend(int days) {
        Long shopId = currentShopId();
        if (days <= 0) days = 7;
        LocalDateTime start = LocalDate.now().minusDays(days - 1).atStartOfDay();

        // 消费趋势：已完成订单 actual
        List<SaleOrderRO> orders = orderMapper.selectList(
                new LambdaQueryWrapper<SaleOrderRO>().eq(SaleOrderRO::getShopId, shopId)
                        .eq(SaleOrderRO::getStatus, 1)
                        .ge(SaleOrderRO::getCreateTime, start));

        // 充值趋势：账户流水 type=1
        List<MemberAccountLogRO> logs = logMapper.selectList(
                new LambdaQueryWrapper<MemberAccountLogRO>()
                        .eq(MemberAccountLogRO::getType, 1)
                        .ge(MemberAccountLogRO::getCreateTime, start));

        Map<LocalDate, BigDecimal> consumeMap = new LinkedHashMap<>();
        Map<LocalDate, BigDecimal> rechargeMap = new LinkedHashMap<>();
        for (SaleOrderRO o : orders) {
            LocalDate d = o.getCreateTime().toLocalDate();
            consumeMap.merge(d, o.getActual() != null ? o.getActual() : BigDecimal.ZERO, BigDecimal::add);
        }
        for (MemberAccountLogRO l : logs) {
            LocalDate d = l.getCreateTime().toLocalDate();
            rechargeMap.merge(d, l.getAmount() != null ? l.getAmount() : BigDecimal.ZERO, BigDecimal::add);
        }

        List<TrendVO> result = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = LocalDate.now().minusDays(i);
            TrendVO vo = new TrendVO();
            vo.setDate(d);
            vo.setConsume(consumeMap.getOrDefault(d, BigDecimal.ZERO));
            vo.setRecharge(rechargeMap.getOrDefault(d, BigDecimal.ZERO));
            result.add(vo);
        }
        return result;
    }

    /**
     * 流失预警：超过指定天数未消费的会员。
     */
    public List<LostMemberVO> lost(int days) {
        Long shopId = currentShopId();
        if (days <= 0) days = 30;
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);

        List<MemberRO> members = memberMapper.selectList(
                new LambdaQueryWrapper<MemberRO>().eq(MemberRO::getShopId, shopId));

        // 每个会员最后消费时间
        Map<Long, LocalDateTime> lastConsume = new LinkedHashMap<>();
        List<SaleOrderRO> orders = orderMapper.selectList(
                new LambdaQueryWrapper<SaleOrderRO>().eq(SaleOrderRO::getShopId, shopId)
                        .eq(SaleOrderRO::getStatus, 1));
        for (SaleOrderRO o : orders) {
            if (o.getMemberId() != null) {
                lastConsume.merge(o.getMemberId(), o.getCreateTime(),
                        (a, b) -> a.isAfter(b) ? a : b);
            }
        }

        List<LostMemberVO> result = new ArrayList<>();
        for (MemberRO m : members) {
            LocalDateTime last = lastConsume.get(m.getId());
            // 从未消费 或 超过阈值
            if (last == null || last.isBefore(threshold)) {
                LostMemberVO vo = new LostMemberVO();
                vo.setId(m.getId());
                vo.setName(m.getName());
                vo.setMobile(m.getMobile());
                vo.setLastConsumeTime(last);
                vo.setDaysSinceLastConsume(last != null ? ChronoUnit.DAYS.between(last.toLocalDate(), LocalDate.now()) : -1);
                result.add(vo);
            }
        }
        return result.stream()
                .sorted(Comparator.comparingLong(LostMemberVO::getDaysSinceLastConsume).reversed())
                .limit(50).toList();
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) throw new BizException("未登录或租户上下文缺失");
        return ctx.getShopId();
    }
}
