package com.pets.trade.adapter.in;

import com.pets.common.annotation.AutoLog;
import com.pets.common.web.R;
import com.pets.trade.application.CheckoutService;
import com.pets.trade.application.dto.CheckoutReq;
import com.pets.trade.application.dto.CheckoutResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收银台结算接口。
 */
@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @AutoLog(module = "收银台", action = "收银结算")
    @PostMapping("/checkout")
    public R<CheckoutResult> checkout(@Valid @RequestBody CheckoutReq req) {
        return R.ok(checkoutService.checkout(req));
    }
}
