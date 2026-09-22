package com.pets.auth.application.dto;

public record ShopOption(
        Long clerkId,
        Long shopId,
        String shopName,
        String roleName
) {
}
