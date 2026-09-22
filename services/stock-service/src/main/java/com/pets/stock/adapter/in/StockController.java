package com.pets.stock.adapter.in;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.web.PageResult;
import com.pets.common.web.R;
import com.pets.stock.application.StockService;
import com.pets.stock.application.dto.StockPageReq;
import com.pets.stock.application.dto.StockVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 库存接口：分页查询、扣减、回补、预警阈值设置。
 */
@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @PostMapping("/page")
    public R<List<StockVO>> page(@RequestBody StockPageReq req) {
        Page<StockVO> page = stockService.page(req);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }

    /**
     * 库存扣减（内部接口，供 trade-service 调用）。
     */
    @PostMapping("/deduct")
    public R<Integer> deduct(@RequestBody Map<String, Object> body) {
        Long shopId = Long.valueOf(body.get("shopId").toString());
        Long goodsId = Long.valueOf(body.get("goodsId").toString());
        Long skuId = body.get("skuId") != null ? Long.valueOf(body.get("skuId").toString()) : 0L;
        int qty = Integer.parseInt(body.get("qty").toString());
        int remaining = stockService.deduct(shopId, goodsId, skuId, qty);
        return R.ok(remaining);
    }

    /**
     * 库存回补（订单取消/退款）。
     */
    @PostMapping("/revert")
    public R<Void> revert(@RequestBody Map<String, Object> body) {
        Long shopId = Long.valueOf(body.get("shopId").toString());
        Long goodsId = Long.valueOf(body.get("goodsId").toString());
        Long skuId = body.get("skuId") != null ? Long.valueOf(body.get("skuId").toString()) : 0L;
        int qty = Integer.parseInt(body.get("qty").toString());
        stockService.revert(shopId, goodsId, skuId, qty);
        return R.ok();
    }

    /**
     * 设置预警阈值。
     */
    @PostMapping("/warn")
    public R<Void> updateWarnQty(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer warnQty = Integer.valueOf(body.get("warnQty").toString());
        stockService.updateWarnQty(id, warnQty);
        return R.ok();
    }
}
