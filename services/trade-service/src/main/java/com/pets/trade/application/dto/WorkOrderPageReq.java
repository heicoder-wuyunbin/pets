package com.pets.trade.application.dto;

import lombok.Data;

/**
 * 服务单分页请求。
 */
@Data
public class WorkOrderPageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private Integer status;
    private Long clerkId;
}
