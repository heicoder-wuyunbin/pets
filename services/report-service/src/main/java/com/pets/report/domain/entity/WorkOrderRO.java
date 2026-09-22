package com.pets.report.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 跨表只读：服务单 */
@Data
@TableName("work_order")
public class WorkOrderRO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    private Long serviceId;

    private String serviceName;

    private Long clerkId;

    private Integer status;

    private LocalDateTime createTime;
}
