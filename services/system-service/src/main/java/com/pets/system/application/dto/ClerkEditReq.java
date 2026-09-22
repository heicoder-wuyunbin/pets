package com.pets.system.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 编辑员工请求。
 */
@Data
public class ClerkEditReq {

    @NotNull(message = "员工ID不能为空")
    private Long id;

    private String truename;

    private Long roleId;
}
