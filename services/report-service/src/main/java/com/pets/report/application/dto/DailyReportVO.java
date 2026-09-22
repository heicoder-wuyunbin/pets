package com.pets.report.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DailyReportVO {
    private LocalDate reportDate;
    private BigDecimal saleAmount;
    private Integer orderCount;
    private BigDecimal refundAmount;
    private BigDecimal avgOrder;
    private Integer memberCount;
    private Integer newMember;
}
