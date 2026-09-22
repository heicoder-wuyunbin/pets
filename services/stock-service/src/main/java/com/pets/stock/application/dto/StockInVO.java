package com.pets.stock.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单 VO。
 */
@Data
public class StockInVO {
    private Long id;
    private String no;
    private String supplier;
    private BigDecimal totalAmount;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private List<Item> items;

    @Data
    public static class Item {
        private Long goodsId;
        private Long skuId;
        private Integer qty;
        private BigDecimal price;
    }
}
