package com.pets.trade.application.dto;

import lombok.Data;

import java.math.BigDecimal;

/** 营销优惠计算结果（对接 market-service） */
@Data
public class MarketingCalcResult {
    private Long activityId;
    private String activityName;
    private BigDecimal discountAmount;
    private BigDecimal payableAmount;
}
