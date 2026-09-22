package com.pets.stock.adapter.in;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.annotation.AutoLog;
import com.pets.common.web.PageResult;
import com.pets.common.web.R;
import com.pets.stock.application.StockOutService;
import com.pets.stock.application.dto.StockOutSaveReq;
import com.pets.stock.application.dto.StockOutVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 出库单接口。
 */
@RestController
@RequestMapping("/api/v1/stock/out")
@RequiredArgsConstructor
public class StockOutController {

    private final StockOutService stockOutService;

    @AutoLog(module = "库存管理", action = "创建出库单")
    @PostMapping("/create")
    public R<String> create(@Valid @RequestBody StockOutSaveReq req) {
        return R.ok(stockOutService.create(req));
    }

    @PostMapping("/page")
    public R<List<StockOutVO>> page(@RequestBody Map<String, Object> body) {
        Integer pageNum = body.get("pageNum") != null ? Integer.parseInt(body.get("pageNum").toString()) : 1;
        Integer pageSize = body.get("pageSize") != null ? Integer.parseInt(body.get("pageSize").toString()) : 20;
        String keyword = body.get("keyword") != null ? body.get("keyword").toString() : null;
        Page<StockOutVO> page = stockOutService.page(pageNum, pageSize, keyword);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }
}
