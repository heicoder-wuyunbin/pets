package com.pets.member.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountLogVO {
    private Long id;
    private Integer type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Integer points;
    private Integer pointsAfter;
    private Long orderId;
    private String remark;
    private LocalDateTime createTime;
}
