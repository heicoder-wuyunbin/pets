package com.pets.market.adapter.in;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.annotation.AutoLog;
import com.pets.common.web.PageResult;
import com.pets.common.web.R;
import com.pets.market.application.MarketingCalculator;
import com.pets.market.application.MarketingService;
import com.pets.market.application.dto.MarketingCalcReq;
import com.pets.market.application.dto.MarketingCalcResult;
import com.pets.market.application.dto.MarketingSaveReq;
import com.pets.market.application.dto.MarketingVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/marketing")
@RequiredArgsConstructor
public class MarketingController {

    private final MarketingService marketingService;
    private final MarketingCalculator marketingCalculator;

    @PostMapping("/page")
    public R<List<MarketingVO>> page(@RequestBody Map<String, Object> body) {
        Integer pageNum = body.get("pageNum") != null ? Integer.parseInt(body.get("pageNum").toString()) : 1;
        Integer pageSize = body.get("pageSize") != null ? Integer.parseInt(body.get("pageSize").toString()) : 20;
        Integer status = body.get("status") != null ? Integer.parseInt(body.get("status").toString()) : null;
        Page<MarketingVO> page = marketingService.page(pageNum, pageSize, status);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }

    @PostMapping("/list")
    public R<List<MarketingVO>> list() {
        return R.ok(marketingService.list());
    }

    @AutoLog(module = "营销中心", action = "保存活动")
    @PostMapping("/save")
    public R<Void> save(@Valid @RequestBody MarketingSaveReq req) {
        marketingService.save(req);
        return R.ok();
    }

    @AutoLog(module = "营销中心", action = "启停活动")
    @PostMapping("/status")
    public R<Void> status(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        marketingService.updateStatus(id, status);
        return R.ok();
    }

    /**
     * 营销优惠计算（供收银结算调用）。
     */
    @PostMapping("/calculate")
    public R<MarketingCalcResult> calculate(@RequestBody MarketingCalcReq req) {
        return R.ok(marketingCalculator.calculate(req));
    }
}
