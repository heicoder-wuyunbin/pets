package com.pets.member.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CardTypeVO {
    private Long id;
    private String name;
    private BigDecimal faceValue;
    private BigDecimal discount;
    private Integer validDays;
    private Integer status;
    private LocalDateTime createTime;
}
