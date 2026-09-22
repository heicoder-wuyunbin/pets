package com.pets.goods.domain.entity;

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
 * 服务项目表。
 */
@Data
@TableName("service")
public class ServiceItem {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    private Long categoryId;

    private String name;

    private BigDecimal price;

    private Integer duration;

    private String description;

    /** 是否可用次卡 0否 1是 */
    private Integer canCard;

    /** 1上架 0下架 */
    private Integer status;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
