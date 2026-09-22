package com.pets.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 跨表只读：销售订单 */
@Data
@TableName("sale_order")
public class SaleOrderRO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    private Long memberId;

    private Integer status;

    private LocalDateTime createTime;
}
