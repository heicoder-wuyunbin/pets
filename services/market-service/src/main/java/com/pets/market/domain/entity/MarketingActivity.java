package com.pets.market.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("marketing_activity")
public class MarketingActivity {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    private String name;

    /** 1满减 2折扣 */
    private Integer type;

    private BigDecimal threshold;

    private BigDecimal discountValue;

    /** 限定商品ID(逗号分隔,空=全部) */
    private String goodsIds;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 1启用 0停用 */
    private Integer status;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
