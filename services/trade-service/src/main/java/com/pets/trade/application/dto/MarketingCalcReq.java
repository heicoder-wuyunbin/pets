package com.pets.trade.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/** 营销优惠计算请求（对接 market-service） */
@Data
public class MarketingCalcReq {
    private Long shopId;
    private List<Long> goodsIds;
    private BigDecimal totalAmount;
}
