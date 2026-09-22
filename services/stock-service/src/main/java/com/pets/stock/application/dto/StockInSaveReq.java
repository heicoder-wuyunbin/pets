package com.pets.stock.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 入库单保存请求。
 */
@Data
public class StockInSaveReq {
    private String supplier;
    private String remark;
    private List<Item> items;

    @Data
    public static class Item {
        private Long goodsId;
        private Long skuId;
        private Integer qty;
        private BigDecimal price;
    }
}
