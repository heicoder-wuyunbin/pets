package com.pets.goods.adapter.in;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.annotation.AutoLog;
import com.pets.common.web.PageResult;
import com.pets.common.web.R;
import com.pets.goods.application.ServiceItemService;
import com.pets.goods.application.dto.CategorySaveReq;
import com.pets.goods.application.dto.CategoryVO;
import com.pets.goods.application.dto.ServicePageReq;
import com.pets.goods.application.dto.ServiceSaveReq;
import com.pets.goods.application.dto.ServiceVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 服务项目接口。
 */
@RestController
@RequestMapping("/api/v1/service")
@RequiredArgsConstructor
public class ServiceController {

    private final ServiceItemService serviceItemService;

    @PostMapping("/category/list")
    public R<List<CategoryVO>> categoryList() {
        return R.ok(serviceItemService.categoryList());
    }

    @PostMapping("/category/save")
    public R<Void> categorySave(@Valid @RequestBody CategorySaveReq req) {
        serviceItemService.categorySave(req);
        return R.ok();
    }

    @PostMapping("/page")
    public R<List<ServiceVO>> page(@RequestBody ServicePageReq req) {
        Page<ServiceVO> page = serviceItemService.page(req);
        return new R<>(0, "成功", page.getRecords(),
                PageResult.of((int) page.getCurrent(), (int) page.getSize(), page.getTotal()));
    }

    @AutoLog(module = "服务项目", action = "保存服务")
    @PostMapping("/save")
    public R<Void> save(@Valid @RequestBody ServiceSaveReq req) {
        serviceItemService.save(req);
        return R.ok();
    }

    @AutoLog(module = "服务项目", action = "上下架")
    @PostMapping("/status")
    public R<Void> status(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        serviceItemService.updateStatus(id, status);
        return R.ok();
    }
}
