package com.pets.market.application.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 营销计算结果：命中的活动 + 优惠金额。
 */
@Data
public class MarketingCalcResult {
    /** 命中的活动ID */
    private Long activityId;

    /** 活动名 */
    private String activityName;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 应收 = 原价 - 优惠 */
    private BigDecimal payableAmount;

    public static MarketingCalcResult none(BigDecimal total) {
        MarketingCalcResult r = new MarketingCalcResult();
        r.setActivityId(null);
        r.setActivityName(null);
        r.setDiscountAmount(BigDecimal.ZERO);
        r.setPayableAmount(total);
        return r;
    }
}
