package com.pets.goods.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 服务项目保存请求。
 */
@Data
public class ServiceSaveReq {

    private Long id;

    private Long categoryId;

    @NotBlank(message = "服务名称不能为空")
    private String name;

    private BigDecimal price;

    private Integer duration;

    private String description;

    private Integer canCard = 0;

    private Integer status = 1;
}
