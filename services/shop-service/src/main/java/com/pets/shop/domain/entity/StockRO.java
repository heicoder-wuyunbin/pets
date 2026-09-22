package com.pets.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/** 跨表只读：库存 */
@Data
@TableName("stock")
public class StockRO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    private Long goodsId;

    private Integer quantity;
}
