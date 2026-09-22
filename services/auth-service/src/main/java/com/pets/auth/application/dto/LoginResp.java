package com.pets.auth.application.dto;

import java.util.List;

public record LoginResp(
        String token,
        Long shopId,
        String shopName,
        String roleCode,
        String roleName,
        boolean needSelect,
        List<ShopOption> shops
) {
}
