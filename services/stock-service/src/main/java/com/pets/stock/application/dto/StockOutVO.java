package com.pets.stock.application.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库单 VO。
 */
@Data
public class StockOutVO {
    private Long id;
    private String no;
    private Integer type;
    private Integer totalQty;
    private String remark;
    private LocalDateTime createTime;
    private List<Item> items;

    @Data
    public static class Item {
        private Long goodsId;
        private Long skuId;
        private Integer qty;
    }
}
