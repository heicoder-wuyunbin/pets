package com.pets.member.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("member_account_log")
public class MemberAccountLog {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long memberId;

    /** 1充值 2消费 3退款 4积分得 5积分抵 */
    private Integer type;

    private BigDecimal amount;

    private BigDecimal balanceAfter;

    private Integer points;

    private Integer pointsAfter;

    private Long orderId;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
