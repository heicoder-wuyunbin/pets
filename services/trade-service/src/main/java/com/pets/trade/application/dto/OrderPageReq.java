package com.pets.trade.application.dto;

import lombok.Data;

/**
 * 订单分页查询请求。
 */
@Data
public class OrderPageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    /** 订单状态: 0待支付 1已完成 2已取消 3退款中 4已退款, null=全部 */
    private Integer status;
    private Long clerkId;
    private String keyword;
    private String startTime;
    private String endTime;
}
