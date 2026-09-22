package com.pets.market.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MarketingVO {
    private Long id;
    private String name;
    private Integer type;
    private BigDecimal threshold;
    private BigDecimal discountValue;
    private String goodsIds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;
    private LocalDateTime createTime;
}
