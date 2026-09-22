package com.pets.trade.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表 VO。
 */
@Data
public class OrderListVO {
    private Long id;
    private String orderNo;
    private String memberName;
    private BigDecimal total;
    private BigDecimal payable;
    private BigDecimal actual;
    private String payType;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;
}
