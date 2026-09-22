package com.pets.report.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TrendVO {
    private LocalDate date;
    private BigDecimal recharge;
    private BigDecimal consume;
}
