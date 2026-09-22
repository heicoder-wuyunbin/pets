package com.pets.report.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LostMemberVO {
    private Long id;
    private String name;
    private String mobile;
    private BigDecimal totalConsume;
    private LocalDateTime lastConsumeTime;
    private long daysSinceLastConsume;
}
