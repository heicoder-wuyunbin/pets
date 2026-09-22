package com.pets.trade.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务单 VO。
 */
@Data
public class WorkOrderVO {
    private Long id;
    private Long orderId;
    private Long orderItemId;
    private Long serviceId;
    private String serviceName;
    private Long clerkId;
    private Integer status;
    private String remark;
    private String pics;
    private LocalDateTime startTime;
    private LocalDateTime finishTime;
    private LocalDateTime createTime;
}
