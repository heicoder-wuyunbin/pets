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
 * 库存表。
 * 唯一约束：(shop_id, goods_id, sku_id)
 * version 用于乐观锁防超卖。
 */
@Data
@TableName("stock")
public class Stock {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    private Long goodsId;

    private Long skuId;

    /** 当前库存数量 */
    private Integer quantity;

    /** 预警阈值 */
    private Integer warnQty;

    /** 入库均价（移动平均） */
    private BigDecimal avgCost;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
