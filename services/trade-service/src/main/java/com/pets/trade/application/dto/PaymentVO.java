package com.pets.trade.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付流水 VO。
 */
@Data
public class PaymentVO {
    private Long id;
    private Long orderId;
    private String orderNo;
    private String payType;
    private BigDecimal amount;
    private Integer status;
    private String bizNo;
    private Integer isRefund;
    private LocalDateTime createTime;
}
