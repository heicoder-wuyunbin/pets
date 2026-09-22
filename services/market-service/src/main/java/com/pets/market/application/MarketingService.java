package com.pets.market.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.market.adapter.out.MarketingActivityMapper;
import com.pets.market.application.dto.MarketingSaveReq;
import com.pets.market.application.dto.MarketingVO;
import com.pets.market.domain.entity.MarketingActivity;
import com.pets.id.service.IdService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarketingService {

    private final MarketingActivityMapper activityMapper;
    private final IdService idService;

    public Page<MarketingVO> page(Integer pageNum, Integer pageSize, Integer status) {
        Long shopId = currentShopId();
        LambdaQueryWrapper<MarketingActivity> wrapper = new LambdaQueryWrapper<MarketingActivity>()
                .eq(MarketingActivity::getShopId, shopId)
                .orderByDesc(MarketingActivity::getCreateTime);
        if (status != null) wrapper.eq(MarketingActivity::getStatus, status);

        Page<MarketingActivity> page = activityMapper.selectPage(
                new Page<>(pageNum != null ? pageNum : 1, pageSize != null ? pageSize : 20), wrapper);

        Page<MarketingVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(a -> {
            MarketingVO vo = new MarketingVO();
            vo.setId(a.getId());
            vo.setName(a.getName());
            vo.setType(a.getType());
            vo.setThreshold(a.getThreshold());
            vo.setDiscountValue(a.getDiscountValue());
            vo.setGoodsIds(a.getGoodsIds());
            vo.setStartTime(a.getStartTime());
            vo.setEndTime(a.getEndTime());
            vo.setStatus(a.getStatus());
            vo.setCreateTime(a.getCreateTime());
            return vo;
        }).toList());
        return result;
    }

    public List<MarketingVO> list() {
        Long shopId = currentShopId();
        return activityMapper.selectList(
                new LambdaQueryWrapper<MarketingActivity>()
                        .eq(MarketingActivity::getShopId, shopId)
                        .orderByDesc(MarketingActivity::getCreateTime)
        ).stream().map(a -> {
            MarketingVO vo = new MarketingVO();
            vo.setId(a.getId());
            vo.setName(a.getName());
            vo.setType(a.getType());
            vo.setThreshold(a.getThreshold());
            vo.setDiscountValue(a.getDiscountValue());
            vo.setStatus(a.getStatus());
            return vo;
        }).toList();
    }

    public void save(MarketingSaveReq req) {
        Long shopId = currentShopId();
        if (req.getId() == null) {
            MarketingActivity act = new MarketingActivity();
            act.setId(idService.next("marketing_activity"));
            act.setShopId(shopId);
            act.setName(req.getName());
            act.setType(req.getType());
            act.setThreshold(req.getThreshold());
            act.setDiscountValue(req.getDiscountValue());
            act.setGoodsIds(req.getGoodsIds());
            act.setStartTime(req.getStartTime());
            act.setEndTime(req.getEndTime());
            act.setStatus(req.getStatus() != null ? req.getStatus() : 1);
            activityMapper.insert(act);
        } else {
            MarketingActivity act = activityMapper.selectById(req.getId());
            if (act == null || !act.getShopId().equals(shopId)) throw new BizException("活动不存在");
            act.setName(req.getName());
            act.setType(req.getType());
            act.setThreshold(req.getThreshold());
            act.setDiscountValue(req.getDiscountValue());
            act.setGoodsIds(req.getGoodsIds());
            act.setStartTime(req.getStartTime());
            act.setEndTime(req.getEndTime());
            act.setStatus(req.getStatus());
            activityMapper.updateById(act);
        }
    }

    public void updateStatus(Long id, Integer status) {
        MarketingActivity act = activityMapper.selectById(id);
        if (act == null || !act.getShopId().equals(currentShopId())) throw new BizException("活动不存在");
        act.setStatus(status);
        activityMapper.updateById(act);
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) throw new BizException("未登录或租户上下文缺失");
        return ctx.getShopId();
    }
}
