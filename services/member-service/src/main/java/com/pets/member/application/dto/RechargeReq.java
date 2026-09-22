package com.pets.member.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RechargeReq {
    private Long memberId;

    private Long cardTypeId;

    /** 充值金额 */
    private BigDecimal amount;
}
