package com.pets.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 店铺表。
 */
@Data
@TableName("shop")
public class Shop {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long chainGroupId;

    private String shopName;

    private String logo;

    private String bindPhone;

    private Integer smsRemain;

    private String addr;

    private String telephone;

    private Integer level;

    private LocalDateTime expireTime;

    private Integer status;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
