package com.pets.trade.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.trade.adapter.out.WorkOrderMapper;
import com.pets.trade.application.dto.WorkOrderPageReq;
import com.pets.trade.application.dto.WorkOrderVO;
import com.pets.trade.domain.entity.WorkOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 服务单服务：分页/完成登记。
 */
@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final WorkOrderMapper workOrderMapper;

    public Page<WorkOrderVO> page(WorkOrderPageReq req) {
        Long shopId = currentShopId();
        LambdaQueryWrapper<WorkOrder> wrapper = new LambdaQueryWrapper<WorkOrder>()
                .eq(WorkOrder::getShopId, shopId)
                .orderByDesc(WorkOrder::getCreateTime);

        if (req.getStatus() != null) {
            wrapper.eq(WorkOrder::getStatus, req.getStatus());
        }
        if (req.getClerkId() != null) {
            wrapper.eq(WorkOrder::getClerkId, req.getClerkId());
        }

        Page<WorkOrder> page = workOrderMapper.selectPage(
                new Page<>(req.getPageNum(), req.getPageSize()), wrapper);

        Page<WorkOrderVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(w -> {
            WorkOrderVO vo = new WorkOrderVO();
            vo.setId(w.getId());
            vo.setOrderId(w.getOrderId());
            vo.setOrderItemId(w.getOrderItemId());
            vo.setServiceId(w.getServiceId());
            vo.setServiceName(w.getServiceName());
            vo.setClerkId(w.getClerkId());
            vo.setStatus(w.getStatus());
            vo.setRemark(w.getRemark());
            vo.setPics(w.getPics());
            vo.setStartTime(w.getStartTime());
            vo.setFinishTime(w.getFinishTime());
            vo.setCreateTime(w.getCreateTime());
            return vo;
        }).toList());
        return result;
    }

    public void complete(Long workOrderId, String remark) {
        WorkOrder wo = workOrderMapper.selectById(workOrderId);
        if (wo == null) {
            throw new BizException("服务单不存在");
        }
        if (!wo.getShopId().equals(currentShopId())) {
            throw new BizException("无权操作该服务单");
        }
        if (wo.getStatus() == 2 || wo.getStatus() == 3) {
            throw new BizException("服务单已结束，无法重复操作");
        }
        wo.setStatus(2);
        wo.setFinishTime(LocalDateTime.now());
        if (remark != null) {
            wo.setRemark(remark);
        }
        workOrderMapper.updateById(wo);
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) {
            throw new BizException("未登录或租户上下文缺失");
        }
        return ctx.getShopId();
    }
}
