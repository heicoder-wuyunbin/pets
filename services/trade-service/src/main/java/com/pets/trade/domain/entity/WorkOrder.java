package com.pets.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务单表。
 */
@Data
@TableName("work_order")
public class WorkOrder {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    private Long orderId;

    private Long orderItemId;

    private Long serviceId;

    private String serviceName;

    private Long clerkId;

    /** 0待服务 1服务中 2已完成 3已取消 */
    private Integer status;

    private String remark;

    private String pics;

    private LocalDateTime startTime;

    private LocalDateTime finishTime;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
