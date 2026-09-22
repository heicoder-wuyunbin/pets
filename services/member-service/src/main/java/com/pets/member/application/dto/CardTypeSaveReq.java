package com.pets.member.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CardTypeSaveReq {
    private Long id;

    @NotBlank(message = "卡名不能为空")
    private String name;

    private BigDecimal faceValue;

    private BigDecimal discount;

    private Integer validDays;

    private Integer status = 1;
}
