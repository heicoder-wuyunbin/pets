package com.pets.trade.application.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细 VO。
 */
@Data
public class OrderItemVO {
    private Long id;
    private Integer itemType;
    private Long itemId;
    private String itemName;
    private BigDecimal price;
    private Integer qty;
    private BigDecimal discount;
    private BigDecimal amount;
}
