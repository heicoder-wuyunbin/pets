package com.pets.goods.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品 VO。
 */
@Data
public class GoodsVO {
    private Long id;
    private Long categoryId;
    private String name;
    private String barcode;
    private String unit;
    private BigDecimal retailPrice;
    private BigDecimal costPrice;
    private String pic;
    private Integer status;
    private LocalDateTime createTime;
    private List<GoodsSkuVO> skus;
}
