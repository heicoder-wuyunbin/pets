package com.pets.system.application.dto;

import lombok.Data;

/**
 * 操作日志分页查询请求。
 */
@Data
public class OperationLogPageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    private String module;
    private String userName;
    /** 起始时间 yyyy-MM-dd */
    private String startTime;
    /** 结束时间 yyyy-MM-dd */
    private String endTime;
}
