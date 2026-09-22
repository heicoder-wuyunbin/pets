package com.pets.trade.application.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收银台结算结果。
 */
@Data
public class CheckoutResult {
    private Long orderId;
    private String orderNo;
    private BigDecimal total;
    private BigDecimal payable;
    private BigDecimal actualReceived;
    /** 营销优惠金额 */
    private BigDecimal marketingDiscount;
    /** 命中的营销活动名 */
    private String activityName;
}
