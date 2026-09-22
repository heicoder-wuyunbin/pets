package com.pets.market.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MarketingSaveReq {
    private Long id;

    @NotBlank(message = "活动名不能为空")
    private String name;

    /** 1满减 2折扣 */
    private Integer type;

    private BigDecimal threshold;

    private BigDecimal discountValue;

    private String goodsIds;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status = 1;
}
