package com.pets.goods.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.goods.adapter.out.GoodsCategoryMapper;
import com.pets.goods.application.dto.CategorySaveReq;
import com.pets.goods.application.dto.CategoryVO;
import com.pets.goods.domain.entity.GoodsCategory;
import com.pets.id.service.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分类服务。
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final GoodsCategoryMapper categoryMapper;
    private final IdService idService;

    public List<CategoryVO> list() {
        Long shopId = currentShopId();
        List<GoodsCategory> list = categoryMapper.selectList(
                new LambdaQueryWrapper<GoodsCategory>()
                        .eq(GoodsCategory::getShopId, shopId)
                        .orderByAsc(GoodsCategory::getSort));
        return list.stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(c.getId());
            vo.setParentId(c.getParentId());
            vo.setName(c.getName());
            vo.setSort(c.getSort());
            return vo;
        }).toList();
    }

    public void save(CategorySaveReq req) {
        Long shopId = currentShopId();
        if (req.getId() == null) {
            GoodsCategory cat = new GoodsCategory();
            cat.setId(idService.next("goods_category"));
            cat.setShopId(shopId);
            cat.setParentId(req.getParentId() != null ? req.getParentId() : 0L);
            cat.setName(req.getName());
            cat.setSort(req.getSort() != null ? req.getSort() : 0);
            categoryMapper.insert(cat);
        } else {
            GoodsCategory cat = categoryMapper.selectById(req.getId());
            if (cat == null || !cat.getShopId().equals(shopId)) {
                throw new BizException("分类不存在");
            }
            cat.setName(req.getName());
            if (req.getParentId() != null) cat.setParentId(req.getParentId());
            if (req.getSort() != null) cat.setSort(req.getSort());
            categoryMapper.updateById(cat);
        }
    }

    public void delete(Long id) {
        GoodsCategory cat = categoryMapper.selectById(id);
        if (cat == null || !cat.getShopId().equals(currentShopId())) {
            throw new BizException("分类不存在");
        }
        categoryMapper.deleteById(id);
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) {
            throw new BizException("未登录或租户上下文缺失");
        }
        return ctx.getShopId();
    }
}
