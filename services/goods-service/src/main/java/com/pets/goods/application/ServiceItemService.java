package com.pets.goods.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.goods.adapter.out.ServiceCategoryMapper;
import com.pets.goods.adapter.out.ServiceMapper;
import com.pets.goods.application.dto.CategorySaveReq;
import com.pets.goods.application.dto.CategoryVO;
import com.pets.goods.application.dto.ServicePageReq;
import com.pets.goods.application.dto.ServiceSaveReq;
import com.pets.goods.application.dto.ServiceVO;
import com.pets.goods.domain.entity.ServiceCategory;
import com.pets.goods.domain.entity.ServiceItem;
import com.pets.id.service.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 服务项目服务：分类 + 服务CRUD + 上下架。
 */
@Service
@RequiredArgsConstructor
public class ServiceItemService {

    private final ServiceMapper serviceMapper;
    private final ServiceCategoryMapper categoryMapper;
    private final IdService idService;

    // ==================== 服务分类 ====================

    public List<CategoryVO> categoryList() {
        Long shopId = currentShopId();
        return categoryMapper.selectList(
                new LambdaQueryWrapper<ServiceCategory>()
                        .eq(ServiceCategory::getShopId, shopId)
                        .orderByAsc(ServiceCategory::getSort)
        ).stream().map(c -> {
            CategoryVO vo = new CategoryVO();
            vo.setId(c.getId());
            vo.setParentId(0L);
            vo.setName(c.getName());
            vo.setSort(c.getSort());
            return vo;
        }).toList();
    }

    public void categorySave(CategorySaveReq req) {
        Long shopId = currentShopId();
        if (req.getId() == null) {
            ServiceCategory cat = new ServiceCategory();
            cat.setId(idService.next("service_category"));
            cat.setShopId(shopId);
            cat.setName(req.getName());
            cat.setSort(req.getSort() != null ? req.getSort() : 0);
            categoryMapper.insert(cat);
        } else {
            ServiceCategory cat = categoryMapper.selectById(req.getId());
            if (cat == null || !cat.getShopId().equals(shopId)) {
                throw new BizException("分类不存在");
            }
            cat.setName(req.getName());
            if (req.getSort() != null) cat.setSort(req.getSort());
            categoryMapper.updateById(cat);
        }
    }

    public void categoryDelete(Long id) {
        categoryMapper.deleteById(id);
    }

    // ==================== 服务项目 ====================

    public Page<ServiceVO> page(ServicePageReq req) {
        Long shopId = currentShopId();
        LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getShopId, shopId)
                .orderByDesc(ServiceItem::getCreateTime);

        if (req.getCategoryId() != null) {
            wrapper.eq(ServiceItem::getCategoryId, req.getCategoryId());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            wrapper.like(ServiceItem::getName, req.getKeyword());
        }
        if (req.getStatus() != null) {
            wrapper.eq(ServiceItem::getStatus, req.getStatus());
        }

        Page<ServiceItem> page = serviceMapper.selectPage(
                new Page<>(req.getPageNum(), req.getPageSize()), wrapper);

        Page<ServiceVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(s -> {
            ServiceVO vo = new ServiceVO();
            vo.setId(s.getId());
            vo.setCategoryId(s.getCategoryId());
            vo.setName(s.getName());
            vo.setPrice(s.getPrice());
            vo.setDuration(s.getDuration());
            vo.setDescription(s.getDescription());
            vo.setCanCard(s.getCanCard());
            vo.setStatus(s.getStatus());
            vo.setCreateTime(s.getCreateTime());
            return vo;
        }).toList());
        return result;
    }

    public void save(ServiceSaveReq req) {
        Long shopId = currentShopId();
        if (req.getId() == null) {
            ServiceItem item = new ServiceItem();
            item.setId(idService.next("service"));
            item.setShopId(shopId);
            item.setCategoryId(req.getCategoryId());
            item.setName(req.getName());
            item.setPrice(req.getPrice());
            item.setDuration(req.getDuration());
            item.setDescription(req.getDescription());
            item.setCanCard(req.getCanCard() != null ? req.getCanCard() : 0);
            item.setStatus(req.getStatus() != null ? req.getStatus() : 1);
            serviceMapper.insert(item);
        } else {
            ServiceItem item = getOwnedService(req.getId());
            item.setCategoryId(req.getCategoryId());
            item.setName(req.getName());
            item.setPrice(req.getPrice());
            item.setDuration(req.getDuration());
            item.setDescription(req.getDescription());
            item.setCanCard(req.getCanCard());
            item.setStatus(req.getStatus());
            serviceMapper.updateById(item);
        }
    }

    public void updateStatus(Long id, Integer status) {
        ServiceItem item = getOwnedService(id);
        item.setStatus(status);
        serviceMapper.updateById(item);
    }

    private ServiceItem getOwnedService(Long id) {
        ServiceItem item = serviceMapper.selectById(id);
        if (item == null) {
            throw new BizException("服务不存在");
        }
        if (!item.getShopId().equals(currentShopId())) {
            throw new BizException("无权操作该服务");
        }
        return item;
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) {
            throw new BizException("未登录或租户上下文缺失");
        }
        return ctx.getShopId();
    }
}
