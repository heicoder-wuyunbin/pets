package com.pets.system.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志表。
 */
@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    private Long userId;

    private String userName;

    private String module;

    private String action;

    private String params;

    private String ip;

    /** 1成功 0失败 */
    private Integer status;

    private String failReason;

    private LocalDateTime createTime;
}
