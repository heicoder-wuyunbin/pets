package com.pets.trade.application.dto;

import lombok.Data;

/**
 * 支付流水分页请求。
 */
@Data
public class PaymentPageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private String payType;
    private Long orderId;
    private String startTime;
    private String endTime;
}
