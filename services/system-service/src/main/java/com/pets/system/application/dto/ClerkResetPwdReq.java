package com.pets.system.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 重置密码请求。
 */
@Data
public class ClerkResetPwdReq {

    @NotNull(message = "员工ID不能为空")
    private Long id;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
