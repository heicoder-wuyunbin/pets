package com.pets.system.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 员工状态切换请求。
 */
@Data
public class ClerkStatusReq {

    @NotNull(message = "员工ID不能为空")
    private Long id;

    /** 0禁用 1启用 */
    @NotNull(message = "状态不能为空")
    private Integer status;
}
