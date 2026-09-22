package com.pets.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告与行业资讯表。
 */
@Data
@TableName("notice")
public class Notice {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    /** 1公告 2行业资讯 */
    private Integer type;

    private String title;

    private String content;

    /** 0下架 1上架 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
