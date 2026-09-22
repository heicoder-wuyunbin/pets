package com.pets.trade.application.dto;

import lombok.Data;

/**
 * 现金流水分页请求。
 */
@Data
public class CashbookPageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    /** 1收入 2支出 null=全部 */
    private Integer type;
    private String startTime;
    private String endTime;
}
