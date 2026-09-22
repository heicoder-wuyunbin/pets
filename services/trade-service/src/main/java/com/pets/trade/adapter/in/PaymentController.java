package com.pets.trade.adapter.in;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.web.PageResult;
import com.pets.common.web.R;
import com.pets.trade.application.PaymentService;
import com.pets.trade.application.WorkOrderService;
import com.pets.trade.application.dto.CashbookPageReq;
import com.pets.trade.application.dto.CashbookVO;
import com.pets.trade.application.dto.PaymentPageReq;
import com.pets.trade.application.dto.PaymentVO;
import com.pets.trade.application.dto.WorkOrderPageReq;
import com.pets.trade.application.dto.WorkOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 支付流水、现金流水、服务单接口。
 */
@RestController
@RequestMapping("/api/v1/trade")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final WorkOrderService workOrderService;

    @PostMapping("/payment/page")
    public R<List<PaymentVO>> paymentPage(@RequestBody PaymentPageReq req) {
        Page<PaymentVO> page = paymentService.paymentPage(req);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }

    @PostMapping("/cashbook/page")
    public R<List<CashbookVO>> cashbookPage(@RequestBody CashbookPageReq req) {
        Page<CashbookVO> page = paymentService.cashbookPage(req);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }

    @PostMapping("/order-service/page")
    public R<List<WorkOrderVO>> workOrderPage(@RequestBody WorkOrderPageReq req) {
        Page<WorkOrderVO> page = workOrderService.page(req);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }

    @PostMapping("/order-service/complete")
    public R<Void> workOrderComplete(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        String remark = body.get("remark") != null ? body.get("remark").toString() : null;
        workOrderService.complete(id, remark);
        return R.ok();
    }
}
