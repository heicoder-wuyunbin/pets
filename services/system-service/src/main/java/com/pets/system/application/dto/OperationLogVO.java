package com.pets.system.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志 VO。
 */
@Data
public class OperationLogVO {
    private Long id;
    private Long shopId;
    private Long userId;
    private String userName;
    private String module;
    private String action;
    private String params;
    private String ip;
    private Integer status;
    private String failReason;
    private LocalDateTime createTime;
}
