package com.pets.trade.adapter.in;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.annotation.AutoLog;
import com.pets.common.web.PageResult;
import com.pets.common.web.R;
import com.pets.trade.application.OrderService;
import com.pets.trade.application.dto.OrderDetailVO;
import com.pets.trade.application.dto.OrderListVO;
import com.pets.trade.application.dto.OrderPageReq;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 订单管理接口。
 */
@RestController
@RequestMapping("/api/v1/trade/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/page")
    public R<List<OrderListVO>> page(@RequestBody OrderPageReq req) {
        Page<OrderListVO> page = orderService.page(req);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }

    @PostMapping("/detail")
    public R<OrderDetailVO> detail(@RequestBody Map<String, Long> body) {
        return R.ok(orderService.detail(body.get("orderId")));
    }

    @AutoLog(module = "订单管理", action = "取消订单")
    @PostMapping("/cancel")
    public R<Void> cancel(@RequestBody Map<String, Long> body) {
        orderService.cancel(body.get("orderId"));
        return R.ok();
    }

    @AutoLog(module = "订单管理", action = "订单退款")
    @PostMapping("/refund")
    public R<Void> refund(@RequestBody Map<String, Long> body) {
        orderService.refund(body.get("orderId"));
        return R.ok();
    }
}
