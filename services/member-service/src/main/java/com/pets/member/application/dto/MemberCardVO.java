package com.pets.member.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberCardVO {
    private Long id;
    private Long memberId;
    private Long cardTypeId;
    private String cardTypeName;
    private BigDecimal balance;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;
}
