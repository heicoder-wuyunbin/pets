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
 * 现金流水表。
 */
@Data
@TableName("cashbook")
public class Cashbook {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    /** 1收入 2支出 */
    private Integer type;

    private BigDecimal amount;

    /** 收款/找零/提现/退款 */
    private String bizType;

    private Long orderId;

    private Long clerkId;

    private String memo;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
