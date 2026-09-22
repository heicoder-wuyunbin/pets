package com.pets.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细表。
 */
@Data
@TableName("order_item")
public class OrderItem {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long orderId;

    /** 1商品 2服务 */
    private Integer itemType;

    private Long itemId;

    private String itemName;

    private Long skuId;

    private BigDecimal price;

    private Integer qty;

    private BigDecimal discount;

    private BigDecimal amount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
