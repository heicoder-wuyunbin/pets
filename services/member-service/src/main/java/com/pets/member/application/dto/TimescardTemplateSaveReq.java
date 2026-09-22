package com.pets.member.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TimescardTemplateSaveReq {
    private Long id;

    @NotBlank(message = "模板名不能为空")
    private String name;

    private Long serviceId;

    private Integer times;

    private BigDecimal price;

    private Integer validDays;

    private Integer status = 1;
}
