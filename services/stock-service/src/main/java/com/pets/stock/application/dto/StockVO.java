package com.pets.stock.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存 VO。
 */
@Data
public class StockVO {
    private Long id;
    private Long goodsId;
    private Long skuId;
    /** 冗余商品名/SKU 名，调用方可二次填充 */
    private String goodsName;
    private String skuName;
    private Integer quantity;
    private Integer warnQty;
    private BigDecimal avgCost;
    private LocalDateTime updateTime;
}
