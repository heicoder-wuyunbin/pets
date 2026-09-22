package com.pets.market.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pets.market.adapter.out.MarketingActivityMapper;
import com.pets.market.application.dto.MarketingCalcReq;
import com.pets.market.application.dto.MarketingCalcResult;
import com.pets.market.domain.entity.MarketingActivity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 营销优惠计算器。
 * 遍历启用且在有效期内的活动，取最优优惠（优惠金额最大）。
 * 支持：满减、折扣；限定商品匹配。
 */
@Component
@RequiredArgsConstructor
public class MarketingCalculator {

    private final MarketingActivityMapper activityMapper;

    public MarketingCalcResult calculate(MarketingCalcReq req) {
        if (req.getTotalAmount() == null || req.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return MarketingCalcResult.none(req.getTotalAmount() != null ? req.getTotalAmount() : BigDecimal.ZERO);
        }

        LocalDateTime now = LocalDateTime.now();
        List<MarketingActivity> activities = activityMapper.selectList(
                new LambdaQueryWrapper<MarketingActivity>()
                        .eq(MarketingActivity::getShopId, req.getShopId())
                        .eq(MarketingActivity::getStatus, 1)
                        .and(w -> w.isNull(MarketingActivity::getStartTime).or().le(MarketingActivity::getStartTime, now))
                        .and(w -> w.isNull(MarketingActivity::getEndTime).or().ge(MarketingActivity::getEndTime, now))
        );

        BigDecimal bestDiscount = BigDecimal.ZERO;
        MarketingActivity best = null;

        for (MarketingActivity act : activities) {
            // 限定商品匹配：若 goodsIds 非空，需至少一个商品命中
            if (act.getGoodsIds() != null && !act.getGoodsIds().isBlank()) {
                List<Long> allowIds = Arrays.stream(act.getGoodsIds().split(","))
                        .map(String::trim).filter(s -> !s.isEmpty())
                        .map(Long::valueOf).toList();
                boolean hit = req.getGoodsIds() != null && req.getGoodsIds().stream().anyMatch(allowIds::contains);
                if (!hit) continue;
            }

            BigDecimal discount = calcDiscount(act, req.getTotalAmount());
            if (discount.compareTo(bestDiscount) > 0) {
                bestDiscount = discount;
                best = act;
            }
        }

        if (best == null) {
            return MarketingCalcResult.none(req.getTotalAmount());
        }

        MarketingCalcResult result = new MarketingCalcResult();
        result.setActivityId(best.getId());
        result.setActivityName(best.getName());
        result.setDiscountAmount(bestDiscount);
        result.setPayableAmount(req.getTotalAmount().subtract(bestDiscount).max(BigDecimal.ZERO));
        return result;
    }

    private BigDecimal calcDiscount(MarketingActivity act, BigDecimal total) {
        if (act.getType() == 1) {
            // 满减
            if (act.getThreshold() == null || total.compareTo(act.getThreshold()) >= 0) {
                return act.getDiscountValue() != null ? act.getDiscountValue() : BigDecimal.ZERO;
            }
            return BigDecimal.ZERO;
        } else if (act.getType() == 2) {
            // 折扣：discountValue 为折扣率(0-1)，优惠 = 原价 * (1 - 折扣率)
            BigDecimal rate = act.getDiscountValue() != null ? act.getDiscountValue() : BigDecimal.ONE;
            if (rate.compareTo(BigDecimal.ONE) >= 0) return BigDecimal.ZERO;
            return total.multiply(BigDecimal.ONE.subtract(rate)).setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }
}
