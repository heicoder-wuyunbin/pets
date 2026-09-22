package com.pets.stock.application.dto;

import lombok.Data;

import java.util.List;

/**
 * 出库单保存请求。
 */
@Data
public class StockOutSaveReq {
    /** 1盘亏 2报损 3退货出库 */
    private Integer type = 1;
    private String remark;
    private List<Item> items;

    @Data
    public static class Item {
        private Long goodsId;
        private Long skuId;
        private Integer qty;
    }
}
