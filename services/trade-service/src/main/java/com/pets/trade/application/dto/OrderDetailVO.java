package com.pets.trade.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情 VO。
 */
@Data
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private Long memberId;
    private String memberName;
    private Long clerkId;
    private String clerkName;
    private BigDecimal total;
    private BigDecimal discount;
    private BigDecimal payable;
    private BigDecimal actual;
    private String payType;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
    private List<OrderItemVO> items;
    private List<PaymentVO> payments;
}
