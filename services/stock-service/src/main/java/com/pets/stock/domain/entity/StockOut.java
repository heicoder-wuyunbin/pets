package com.pets.stock.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 出库单主表。
 */
@Data
@TableName("stock_out")
public class StockOut {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    /** 出库单号 */
    private String no;

    /** 类型 1盘亏 2报损 3退货出库 */
    private Integer type;

    private Integer totalQty;

    private String remark;

    private Long createUserId;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
