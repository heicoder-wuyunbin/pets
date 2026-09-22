package com.pets.member.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MemberListVO {
    private Long id;
    private String mobile;
    private String name;
    private Integer gender;
    private Integer memberLevel;
    private String tags;
    private BigDecimal balance;
    private Integer points;
    private BigDecimal totalConsume;
    private LocalDateTime createTime;
}
