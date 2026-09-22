package com.pets.shop.application.dto;

import lombok.Data;

/**
 * 门店详情 VO。
 */
@Data
public class ShopDetailVO {
    private Long id;
    private String shopName;
    private String logo;
    private String bindPhone;
    private Integer level;
    private String expireTime;
    private Integer smsRemain;
    private String addr;
    private String telephone;
}
