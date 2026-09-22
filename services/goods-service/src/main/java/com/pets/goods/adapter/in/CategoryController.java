package com.pets.goods.adapter.in;

import com.pets.common.annotation.AutoLog;
import com.pets.common.web.R;
import com.pets.goods.application.CategoryService;
import com.pets.goods.application.dto.CategorySaveReq;
import com.pets.goods.application.dto.CategoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 商品分类接口。
 */
@RestController
@RequestMapping("/api/v1/goods/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/list")
    public R<List<CategoryVO>> list() {
        return R.ok(categoryService.list());
    }

    @AutoLog(module = "商品分类", action = "保存分类")
    @PostMapping("/save")
    public R<Void> save(@Valid @RequestBody CategorySaveReq req) {
        categoryService.save(req);
        return R.ok();
    }

    @AutoLog(module = "商品分类", action = "删除分类")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody Map<String, Long> body) {
        categoryService.delete(body.get("id"));
        return R.ok();
    }
}
