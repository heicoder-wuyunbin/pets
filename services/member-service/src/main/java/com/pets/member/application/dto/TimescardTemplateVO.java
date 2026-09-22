package com.pets.member.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TimescardTemplateVO {
    private Long id;
    private String name;
    private Long serviceId;
    private Integer times;
    private BigDecimal price;
    private Integer validDays;
    private Integer status;
    private LocalDateTime createTime;
}
