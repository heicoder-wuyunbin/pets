package com.pets.shop.adapter.in;

import com.pets.common.annotation.AutoLog;
import com.pets.common.web.R;
import com.pets.shop.application.ShopService;
import com.pets.shop.application.dto.ShopDetailVO;
import com.pets.shop.application.dto.ShopUpdateReq;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 门店管理接口。
 */
@RestController
@RequestMapping("/api/v1/shop")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping("/detail")
    public R<ShopDetailVO> detail() {
        return R.ok(shopService.detail());
    }

    @AutoLog(module = "门店管理", action = "编辑门店资料")
    @PostMapping("/update")
    public R<Void> update(@Valid @RequestBody ShopUpdateReq req) {
        shopService.update(req);
        return R.ok();
    }
}
