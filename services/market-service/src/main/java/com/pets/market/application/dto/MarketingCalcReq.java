package com.pets.market.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 营销计算请求：商品列表 + 总价。
 */
@Data
public class MarketingCalcReq {
    private Long shopId;

    /** 商品ID列表（用于匹配限定商品活动） */
    private List<Long> goodsIds;

    /** 订单原价总额 */
    private BigDecimal totalAmount;
}
