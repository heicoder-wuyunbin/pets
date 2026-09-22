package com.pets.goods.application.dto;

import lombok.Data;

/**
 * 分类 VO。
 */
@Data
public class CategoryVO {
    private Long id;
    private Long parentId;
    private String name;
    private Integer sort;
}
