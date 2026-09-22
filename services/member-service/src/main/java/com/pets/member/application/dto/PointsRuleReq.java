package com.pets.member.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PointsRuleReq {
    /** 消费1元得多少积分 */
    private BigDecimal ratio;

    /** 是否启用 */
    private Integer enable;

    /** 满多少元才开始计积分 */
    private BigDecimal offset;
}
