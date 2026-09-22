package com.pets.report.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/** 跨表只读：订单明细 */
@Data
@TableName("order_item")
public class OrderItemRO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long orderId;

    /** 1商品 2服务 */
    private Integer itemType;

    private Long itemId;

    private String itemName;

    private BigDecimal price;

    private Integer qty;

    private BigDecimal amount;
}
