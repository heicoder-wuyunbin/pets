package com.pets.report.application.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RankVO {
    private Long itemId;
    private String itemName;
    private Long count;
    private BigDecimal amount;
}
