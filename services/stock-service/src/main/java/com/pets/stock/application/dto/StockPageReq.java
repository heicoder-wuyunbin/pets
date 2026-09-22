package com.pets.stock.application.dto;

import lombok.Data;

/**
 * 库存分页请求。
 */
@Data
public class StockPageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private String keyword;
    /** 仅查询预警 */
    private Boolean warnOnly;
}
