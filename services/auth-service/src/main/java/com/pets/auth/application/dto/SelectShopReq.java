package com.pets.auth.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SelectShopReq {

    @NotNull(message = "岗位ID不能为空")
    private Long clerkId;
}
