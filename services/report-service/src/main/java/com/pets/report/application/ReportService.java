package com.pets.report.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.report.adapter.out.SaleOrderROMapper;
import com.pets.report.application.dto.DailyReportVO;
import com.pets.report.domain.entity.SaleOrderRO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 营业报表服务：按日直查订单表汇总（替代 RocketMQ 异步预汇总）。
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleOrderROMapper saleOrderMapper;

    /**
     * 区间日报查询。
     */
    public List<DailyReportVO> daily(LocalDate startDate, LocalDate endDate) {
        Long shopId = currentShopId();
        if (startDate == null) startDate = LocalDate.now().minusDays(6);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.plusDays(1).atStartOfDay();

        List<SaleOrderRO> orders = saleOrderMapper.selectList(
                new LambdaQueryWrapper<SaleOrderRO>()
                        .eq(SaleOrderRO::getShopId, shopId)
                        .ge(SaleOrderRO::getCreateTime, start)
                        .lt(SaleOrderRO::getCreateTime, end)
                        .in(SaleOrderRO::getStatus, 1, 4) // 已完成/已退款
        );

        // 按日分组
        Map<LocalDate, List<SaleOrderRO>> grouped = new LinkedHashMap<>();
        for (SaleOrderRO o : orders) {
            LocalDate d = o.getCreateTime().toLocalDate();
            grouped.computeIfAbsent(d, k -> new ArrayList<>()).add(o);
        }

        List<DailyReportVO> result = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            DailyReportVO vo = new DailyReportVO();
            vo.setReportDate(d);
            List<SaleOrderRO> dayOrders = grouped.getOrDefault(d, List.of());
            BigDecimal sale = dayOrders.stream()
                    .filter(o -> o.getStatus() == 1)
                    .map(o -> o.getActual() != null ? o.getActual() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal refund = dayOrders.stream()
                    .filter(o -> o.getStatus() == 4)
                    .map(o -> o.getActual() != null ? o.getActual() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int count = (int) dayOrders.stream().filter(o -> o.getStatus() == 1).count();
            long memberCount = dayOrders.stream().filter(o -> o.getStatus() == 1)
                    .map(SaleOrderRO::getMemberId).filter(id -> id != null && id > 0).distinct().count();

            vo.setSaleAmount(sale);
            vo.setOrderCount(count);
            vo.setRefundAmount(refund);
            vo.setAvgOrder(count > 0 ? sale.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
            vo.setMemberCount((int) memberCount);
            vo.setNewMember(0);
            result.add(vo);
        }
        return result;
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) throw new BizException("未登录或租户上下文缺失");
        return ctx.getShopId();
    }
}
