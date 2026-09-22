package com.pets.stock.adapter.in;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.annotation.AutoLog;
import com.pets.common.web.PageResult;
import com.pets.common.web.R;
import com.pets.stock.application.StockInService;
import com.pets.stock.application.dto.StockInSaveReq;
import com.pets.stock.application.dto.StockInVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 入库单接口。
 */
@RestController
@RequestMapping("/api/v1/stock/in")
@RequiredArgsConstructor
public class StockInController {

    private final StockInService stockInService;

    @AutoLog(module = "库存管理", action = "创建入库单")
    @PostMapping("/create")
    public R<String> create(@Valid @RequestBody StockInSaveReq req) {
        return R.ok(stockInService.create(req));
    }

    @PostMapping("/page")
    public R<List<StockInVO>> page(@RequestBody Map<String, Object> body) {
        Integer pageNum = body.get("pageNum") != null ? Integer.parseInt(body.get("pageNum").toString()) : 1;
        Integer pageSize = body.get("pageSize") != null ? Integer.parseInt(body.get("pageSize").toString()) : 20;
        String keyword = body.get("keyword") != null ? body.get("keyword").toString() : null;
        Page<StockInVO> page = stockInService.page(pageNum, pageSize, keyword);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }
}
