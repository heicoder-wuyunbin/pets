package com.pets.goods.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 分类保存请求。
 */
@Data
public class CategorySaveReq {
    private Long id;
    private Long parentId = 0L;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Integer sort = 0;
}
