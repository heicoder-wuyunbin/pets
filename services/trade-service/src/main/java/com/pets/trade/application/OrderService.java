package com.pets.trade.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.id.service.IdService;
import com.pets.trade.adapter.out.CashbookMapper;
import com.pets.trade.adapter.out.OrderItemMapper;
import com.pets.trade.adapter.out.PaymentMapper;
import com.pets.trade.adapter.out.SaleOrderMapper;
import com.pets.trade.adapter.out.WorkOrderMapper;
import com.pets.trade.application.dto.OrderDetailVO;
import com.pets.trade.application.dto.OrderItemVO;
import com.pets.trade.application.dto.OrderListVO;
import com.pets.trade.application.dto.OrderPageReq;
import com.pets.trade.application.dto.PaymentVO;
import com.pets.trade.domain.entity.Cashbook;
import com.pets.trade.domain.entity.OrderItem;
import com.pets.trade.domain.entity.Payment;
import com.pets.trade.domain.entity.SaleOrder;
import com.pets.trade.domain.entity.WorkOrder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 订单管理服务：分页/详情/取消/退款。
 */
@Service
@RequiredArgsConstructor
public class OrderService {

    private final SaleOrderMapper saleOrderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentMapper paymentMapper;
    private final CashbookMapper cashbookMapper;
    private final WorkOrderMapper workOrderMapper;
    private final IdService idService;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Page<OrderListVO> page(OrderPageReq req) {
        Long shopId = currentShopId();
        LambdaQueryWrapper<SaleOrder> wrapper = new LambdaQueryWrapper<SaleOrder>()
                .eq(SaleOrder::getShopId, shopId)
                .orderByDesc(SaleOrder::getCreateTime);

        if (req.getStatus() != null) {
            wrapper.eq(SaleOrder::getStatus, req.getStatus());
        }
        if (req.getClerkId() != null) {
            wrapper.eq(SaleOrder::getClerkId, req.getClerkId());
        }
        if (req.getKeyword() != null && !req.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(SaleOrder::getOrderNo, req.getKeyword())
                    .or().like(SaleOrder::getMemberName, req.getKeyword())
                    .or().like(SaleOrder::getRemark, req.getKeyword()));
        }
        if (req.getStartTime() != null && !req.getStartTime().isBlank()) {
            wrapper.ge(SaleOrder::getCreateTime, LocalDate.parse(req.getStartTime(), DATE_FMT).atStartOfDay());
        }
        if (req.getEndTime() != null && !req.getEndTime().isBlank()) {
            wrapper.le(SaleOrder::getCreateTime, LocalDate.parse(req.getEndTime(), DATE_FMT).atTime(23, 59, 59));
        }

        Page<SaleOrder> page = saleOrderMapper.selectPage(
                new Page<>(req.getPageNum(), req.getPageSize()), wrapper);

        Page<OrderListVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(this::toListVO).toList());
        return result;
    }

    public OrderDetailVO detail(Long orderId) {
        SaleOrder order = getOwnedOrder(orderId);
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setMemberId(order.getMemberId());
        vo.setMemberName(order.getMemberName());
        vo.setClerkId(order.getClerkId());
        vo.setClerkName(order.getClerkName());
        vo.setTotal(order.getTotal());
        vo.setDiscount(order.getDiscount());
        vo.setPayable(order.getPayable());
        vo.setActual(order.getActual());
        vo.setPayType(order.getPayType());
        vo.setStatus(order.getStatus());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());

        // 明细
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orderId));
        vo.setItems(items.stream().map(i -> {
            OrderItemVO ivo = new OrderItemVO();
            ivo.setId(i.getId());
            ivo.setItemType(i.getItemType());
            ivo.setItemId(i.getItemId());
            ivo.setItemName(i.getItemName());
            ivo.setPrice(i.getPrice());
            ivo.setQty(i.getQty());
            ivo.setDiscount(i.getDiscount());
            ivo.setAmount(i.getAmount());
            return ivo;
        }).toList());

        // 支付流水
        List<Payment> payments = paymentMapper.selectList(
                new LambdaQueryWrapper<Payment>().eq(Payment::getOrderId, orderId));
        vo.setPayments(payments.stream().map(p -> {
            PaymentVO pvo = new PaymentVO();
            pvo.setId(p.getId());
            pvo.setOrderId(p.getOrderId());
            pvo.setOrderNo(p.getOrderNo());
            pvo.setPayType(p.getPayType());
            pvo.setAmount(p.getAmount());
            pvo.setStatus(p.getStatus());
            pvo.setBizNo(p.getBizNo());
            pvo.setIsRefund(p.getIsRefund());
            pvo.setCreateTime(p.getCreateTime());
            return pvo;
        }).toList());

        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long orderId) {
        SaleOrder order = getOwnedOrder(orderId);
        if (order.getStatus() != 0) {
            throw new BizException("仅待支付订单可取消");
        }
        order.setStatus(2);
        saleOrderMapper.updateById(order);
        // TODO(M4): 回滚库存预占
    }

    @Transactional(rollbackFor = Exception.class)
    public void refund(Long orderId) {
        SaleOrder order = getOwnedOrder(orderId);
        if (order.getStatus() != 1) {
            throw new BizException("仅已完成订单可退款");
        }
        order.setStatus(4); // 已退款
        saleOrderMapper.updateById(order);

        // 写退款支付流水
        Payment refund = new Payment();
        refund.setId(idService.next("payment"));
        refund.setShopId(order.getShopId());
        refund.setOrderId(order.getId());
        refund.setOrderNo(order.getOrderNo());
        refund.setPayType(order.getPayType());
        refund.setAmount(order.getActual().negate());
        refund.setStatus(1);
        refund.setIsRefund(1);
        paymentMapper.insert(refund);

        // 现金退款写现金流水
        if ("cash".equalsIgnoreCase(order.getPayType())) {
            Cashbook cb = new Cashbook();
            cb.setId(idService.next("cashbook"));
            cb.setShopId(order.getShopId());
            cb.setType(2); // 支出
            cb.setAmount(order.getActual());
            cb.setBizType("退款");
            cb.setOrderId(order.getId());
            cb.setClerkId(order.getClerkId());
            cashbookMapper.insert(cb);
        }

        // 取消关联服务单
        workOrderMapper.update(null,
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<WorkOrder>()
                        .eq(WorkOrder::getOrderId, orderId)
                        .set(WorkOrder::getStatus, 3));
    }

    private SaleOrder getOwnedOrder(Long orderId) {
        SaleOrder order = saleOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (!order.getShopId().equals(currentShopId())) {
            throw new BizException("无权操作该订单");
        }
        return order;
    }

    private OrderListVO toListVO(SaleOrder o) {
        OrderListVO vo = new OrderListVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setMemberName(o.getMemberName());
        vo.setTotal(o.getTotal());
        vo.setPayable(o.getPayable());
        vo.setActual(o.getActual());
        vo.setPayType(o.getPayType());
        vo.setStatus(o.getStatus());
        vo.setRemark(o.getRemark());
        vo.setCreateTime(o.getCreateTime());
        return vo;
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) {
            throw new BizException("未登录或租户上下文缺失");
        }
        return ctx.getShopId();
    }
}
