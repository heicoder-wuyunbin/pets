package com.pets.stock.domain.entity;

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
 * 入库单主表。
 */
@Data
@TableName("stock_in")
public class StockIn {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    /** 入库单号 */
    private String no;

    private String supplier;

    private BigDecimal totalAmount;

    /** 状态 0已取消 1已入库 */
    private Integer status;

    private String remark;

    private Long createUserId;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
