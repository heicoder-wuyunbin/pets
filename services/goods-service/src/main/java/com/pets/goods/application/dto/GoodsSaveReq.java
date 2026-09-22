package com.pets.goods.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品保存请求（新增/编辑）。
 */
@Data
public class GoodsSaveReq {

    private Long id;

    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String barcode;

    private String unit;

    private BigDecimal retailPrice;

    private BigDecimal costPrice;

    private String pic;

    private Integer status = 1;

    /** SKU 列表 */
    private List<GoodsSkuReq> skus;

    @Data
    public static class GoodsSkuReq {
        private Long id;
        private String specName;
        private BigDecimal price;
    }
}
