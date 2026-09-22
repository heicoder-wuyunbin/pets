package com.pets.goods.application.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 服务项目 VO。
 */
@Data
public class ServiceVO {
    private Long id;
    private Long categoryId;
    private String name;
    private BigDecimal price;
    private Integer duration;
    private String description;
    private Integer canCard;
    private Integer status;
    private LocalDateTime createTime;
}
