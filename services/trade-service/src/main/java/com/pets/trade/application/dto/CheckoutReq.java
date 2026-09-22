package com.pets.trade.application.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 收银台结算请求。
 */
@Data
public class CheckoutReq {

    /** 会员ID(可空=散客) */
    private Long memberId;

    private String memberName;

    /** 支付方式: cash/wechat/alipay/card */
    @NotNull(message = "支付方式不能为空")
    private String payType;

    /** 折扣率(%), 默认100 */
    private BigDecimal discount = new BigDecimal("100");

    private String remark;

    @NotEmpty(message = "购物清单不能为空")
    private List<CheckoutItem> items;

    @Data
    public static class CheckoutItem {
        /** 1商品 2服务 */
        @NotNull(message = "明细类型不能为空")
        private Integer itemType;

        @NotNull(message = "商品/服务ID不能为空")
        private Long itemId;

        private String itemName;

        private Long skuId;

        @NotNull(message = "单价不能为空")
        private BigDecimal price;

        @NotNull(message = "数量不能为空")
        private Integer qty;

        /** 明细折扣率(%) */
        private BigDecimal discount = new BigDecimal("100");
    }
}
