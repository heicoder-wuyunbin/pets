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
 * 支付流水表。
 */
@Data
@TableName("payment")
public class Payment {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    private Long orderId;

    private String orderNo;

    private String payType;

    private BigDecimal amount;

    /** 1成功 0失败 */
    private Integer status;

    private String bizNo;

    /** 0否 1是 */
    private Integer isRefund;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
