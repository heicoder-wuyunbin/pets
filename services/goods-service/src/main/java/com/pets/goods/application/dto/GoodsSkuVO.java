package com.pets.goods.application.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品SKU VO。
 */
@Data
public class GoodsSkuVO {
    private Long id;
    private Long goodsId;
    private String specName;
    private BigDecimal price;
}
