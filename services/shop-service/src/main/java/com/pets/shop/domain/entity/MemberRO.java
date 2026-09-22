package com.pets.shop.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 跨表只读：会员 */
@Data
@TableName("member")
public class MemberRO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long shopId;

    private LocalDateTime createTime;
}
