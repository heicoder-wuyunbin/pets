package com.pets.goods.application.dto;

import lombok.Data;

/**
 * 商品分页请求。
 */
@Data
public class GoodsPageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private Long categoryId;
    private String keyword;
    private Integer status;
}
