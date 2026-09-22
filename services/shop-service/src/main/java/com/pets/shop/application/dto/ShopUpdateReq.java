package com.pets.shop.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 门店资料更新请求。
 */
@Data
public class ShopUpdateReq {

    @NotBlank(message = "店铺名称不能为空")
    private String shopName;

    private String logo;

    private String addr;

    private String telephone;
}
