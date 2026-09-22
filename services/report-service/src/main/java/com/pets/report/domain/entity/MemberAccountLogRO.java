package com.pets.report.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 跨表只读：会员账户流水 */
@Data
@TableName("member_account_log")
public class MemberAccountLogRO {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Long memberId;

    /** 1充值 2消费 3退款 4积分得 5积分抵 */
    private Integer type;

    private BigDecimal amount;

    private LocalDateTime createTime;
}
