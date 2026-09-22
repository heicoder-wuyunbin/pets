package com.pets.system.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 新增员工请求。
 */
@Data
public class ClerkAddReq {

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @NotBlank(message = "姓名不能为空")
    private String truename;

    @NotNull(message = "角色不能为空")
    private Long roleId;

    @NotBlank(message = "初始密码不能为空")
    private String password;
}
