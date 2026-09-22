package com.pets.goods.adapter.in;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.annotation.AutoLog;
import com.pets.common.web.PageResult;
import com.pets.common.web.R;
import com.pets.goods.application.GoodsService;
import com.pets.goods.application.dto.GoodsPageReq;
import com.pets.goods.application.dto.GoodsSaveReq;
import com.pets.goods.application.dto.GoodsVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 商品接口。
 */
@RestController
@RequestMapping("/api/v1/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsService goodsService;

    @PostMapping("/page")
    public R<List<GoodsVO>> page(@RequestBody GoodsPageReq req) {
        Page<GoodsVO> page = goodsService.page(req);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }

    @PostMapping("/detail")
    public R<GoodsVO> detail(@RequestBody Map<String, Long> body) {
        return R.ok(goodsService.detail(body.get("id")));
    }

    @AutoLog(module = "商品管理", action = "保存商品")
    @PostMapping("/save")
    public R<Void> save(@Valid @RequestBody GoodsSaveReq req) {
        goodsService.save(req);
        return R.ok();
    }

    @AutoLog(module = "商品管理", action = "上下架")
    @PostMapping("/status")
    public R<Void> status(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        goodsService.updateStatus(id, status);
        return R.ok();
    }
}
