package com.pets.report.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.report.adapter.out.OrderItemROMapper;
import com.pets.report.adapter.out.SaleOrderROMapper;
import com.pets.report.adapter.out.WorkOrderROMapper;
import com.pets.report.application.dto.RankVO;
import com.pets.report.domain.entity.OrderItemRO;
import com.pets.report.domain.entity.SaleOrderRO;
import com.pets.report.domain.entity.WorkOrderRO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 销售排行服务：商品销量、服务次数、员工业绩。
 */
@Service
@RequiredArgsConstructor
public class RankService {

    private final OrderItemROMapper orderItemMapper;
    private final SaleOrderROMapper orderMapper;
    private final WorkOrderROMapper workOrderMapper;

    /**
     * 商品销量排行（order_item where item_type=1）。
     */
    public List<RankVO> goodsRank(LocalDateTime start, LocalDateTime end, int topN) {
        Long shopId = currentShopId();
        List<Long> orderIds = findOrderIds(shopId, start, end);
        if (orderIds.isEmpty()) return List.of();

        List<OrderItemRO> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItemRO>()
                        .in(OrderItemRO::getOrderId, orderIds)
                        .eq(OrderItemRO::getItemType, 1));

        Map<Long, long[]> sumMap = new LinkedHashMap<>(); // itemId -> [qty, amount]
        Map<Long, String> nameMap = new LinkedHashMap<>();
        for (OrderItemRO it : items) {
            sumMap.computeIfAbsent(it.getItemId(), k -> new long[2]);
            long[] arr = sumMap.get(it.getItemId());
            arr[0] += it.getQty() != null ? it.getQty() : 0;
            arr[1] += it.getAmount() != null ? it.getAmount().longValue() : 0;
            nameMap.putIfAbsent(it.getItemId(), it.getItemName());
        }

        return sumMap.entrySet().stream()
                .map(e -> {
                    RankVO vo = new RankVO();
                    vo.setItemId(e.getKey());
                    vo.setItemName(nameMap.get(e.getKey()));
                    vo.setCount(e.getValue()[0]);
                    vo.setAmount(BigDecimal.valueOf(e.getValue()[1]));
                    return vo;
                })
                .sorted(Comparator.comparing(RankVO::getCount).reversed())
                .limit(topN > 0 ? topN : 10)
                .toList();
    }

    /**
     * 服务次数排行（work_order）。
     */
    public List<RankVO> serviceRank(LocalDateTime start, LocalDateTime end, int topN) {
        Long shopId = currentShopId();
        List<WorkOrderRO> workOrders = workOrderMapper.selectList(
                new LambdaQueryWrapper<WorkOrderRO>()
                        .eq(WorkOrderRO::getShopId, shopId)
                        .eq(WorkOrderRO::getStatus, 2)
                        .ge(start != null, WorkOrderRO::getCreateTime, start)
                        .lt(end != null, WorkOrderRO::getCreateTime, end));

        Map<Long, Long> countMap = new LinkedHashMap<>();
        Map<Long, String> nameMap = new LinkedHashMap<>();
        for (WorkOrderRO w : workOrders) {
            countMap.merge(w.getServiceId(), 1L, Long::sum);
            nameMap.putIfAbsent(w.getServiceId(), w.getServiceName());
        }

        return countMap.entrySet().stream()
                .map(e -> {
                    RankVO vo = new RankVO();
                    vo.setItemId(e.getKey());
                    vo.setItemName(nameMap.get(e.getKey()));
                    vo.setCount(e.getValue());
                    vo.setAmount(BigDecimal.ZERO);
                    return vo;
                })
                .sorted(Comparator.comparing(RankVO::getCount).reversed())
                .limit(topN > 0 ? topN : 10)
                .toList();
    }

    /**
     * 员工业绩排行（sale_order 按 clerk_id 汇总 actual）。
     */
    public List<RankVO> clerkRank(LocalDateTime start, LocalDateTime end, int topN) {
        Long shopId = currentShopId();
        List<SaleOrderRO> orders = orderMapper.selectList(
                new LambdaQueryWrapper<SaleOrderRO>()
                        .eq(SaleOrderRO::getShopId, shopId)
                        .eq(SaleOrderRO::getStatus, 1)
                        .ge(start != null, SaleOrderRO::getCreateTime, start)
                        .lt(end != null, SaleOrderRO::getCreateTime, end));

        Map<Long, long[]> sumMap = new LinkedHashMap<>(); // clerkId -> [count, amount]
        for (SaleOrderRO o : orders) {
            if (o.getClerkId() == null) continue;
            sumMap.computeIfAbsent(o.getClerkId(), k -> new long[2]);
            long[] arr = sumMap.get(o.getClerkId());
            arr[0] += 1;
            arr[1] += o.getActual() != null ? o.getActual().longValue() : 0;
        }

        return sumMap.entrySet().stream()
                .map(e -> {
                    RankVO vo = new RankVO();
                    vo.setItemId(e.getKey());
                    vo.setItemName("员工#" + e.getKey());
                    vo.setCount(e.getValue()[0]);
                    vo.setAmount(BigDecimal.valueOf(e.getValue()[1]));
                    return vo;
                })
                .sorted(Comparator.comparing(RankVO::getAmount).reversed())
                .limit(topN > 0 ? topN : 10)
                .toList();
    }

    private List<Long> findOrderIds(Long shopId, LocalDateTime start, LocalDateTime end) {
        return orderMapper.selectList(
                new LambdaQueryWrapper<SaleOrderRO>()
                        .eq(SaleOrderRO::getShopId, shopId)
                        .eq(SaleOrderRO::getStatus, 1)
                        .ge(start != null, SaleOrderRO::getCreateTime, start)
                        .lt(end != null, SaleOrderRO::getCreateTime, end)
        ).stream().map(SaleOrderRO::getId).toList();
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) throw new BizException("未登录或租户上下文缺失");
        return ctx.getShopId();
    }
}
