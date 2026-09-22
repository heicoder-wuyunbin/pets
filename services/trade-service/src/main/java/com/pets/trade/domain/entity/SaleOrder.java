package com.pets.trade.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 销售订单表。
 */
@Data
@TableName("sale_order")
public class SaleOrder {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    private String orderNo;

    private Long memberId;

    private String memberName;

    private Long clerkId;

    private String clerkName;

    private BigDecimal total;

    private BigDecimal discount;

    private BigDecimal payable;

    private BigDecimal actual;

    private String payType;

    /** 0待支付 1已完成 2已取消 3退款中 4已退款 */
    private Integer status;

    private String remark;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
