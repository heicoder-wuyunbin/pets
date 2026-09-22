package com.pets.trade.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 现金流水 VO。
 */
@Data
public class CashbookVO {
    private Long id;
    private Integer type;
    private BigDecimal amount;
    private String bizType;
    private Long orderId;
    private Long clerkId;
    private String memo;
    private LocalDateTime createTime;
}
