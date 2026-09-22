package com.pets.trade.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.trade.adapter.out.CashbookMapper;
import com.pets.trade.adapter.out.PaymentMapper;
import com.pets.trade.application.dto.CashbookPageReq;
import com.pets.trade.application.dto.CashbookVO;
import com.pets.trade.application.dto.PaymentPageReq;
import com.pets.trade.application.dto.PaymentVO;
import com.pets.trade.domain.entity.Cashbook;
import com.pets.trade.domain.entity.Payment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 支付流水与现金流水服务。
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentMapper paymentMapper;
    private final CashbookMapper cashbookMapper;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Page<PaymentVO> paymentPage(PaymentPageReq req) {
        Long shopId = currentShopId();
        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<Payment>()
                .eq(Payment::getShopId, shopId)
                .orderByDesc(Payment::getCreateTime);

        if (req.getPayType() != null && !req.getPayType().isBlank()) {
            wrapper.eq(Payment::getPayType, req.getPayType());
        }
        if (req.getOrderId() != null) {
            wrapper.eq(Payment::getOrderId, req.getOrderId());
        }
        if (req.getStartTime() != null && !req.getStartTime().isBlank()) {
            wrapper.ge(Payment::getCreateTime, LocalDate.parse(req.getStartTime(), DATE_FMT).atStartOfDay());
        }
        if (req.getEndTime() != null && !req.getEndTime().isBlank()) {
            wrapper.le(Payment::getCreateTime, LocalDate.parse(req.getEndTime(), DATE_FMT).atTime(23, 59, 59));
        }

        Page<Payment> page = paymentMapper.selectPage(
                new Page<>(req.getPageNum(), req.getPageSize()), wrapper);

        Page<PaymentVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(p -> {
            PaymentVO vo = new PaymentVO();
            vo.setId(p.getId());
            vo.setOrderId(p.getOrderId());
            vo.setOrderNo(p.getOrderNo());
            vo.setPayType(p.getPayType());
            vo.setAmount(p.getAmount());
            vo.setStatus(p.getStatus());
            vo.setBizNo(p.getBizNo());
            vo.setIsRefund(p.getIsRefund());
            vo.setCreateTime(p.getCreateTime());
            return vo;
        }).toList());
        return result;
    }

    public Page<CashbookVO> cashbookPage(CashbookPageReq req) {
        Long shopId = currentShopId();
        LambdaQueryWrapper<Cashbook> wrapper = new LambdaQueryWrapper<Cashbook>()
                .eq(Cashbook::getShopId, shopId)
                .orderByDesc(Cashbook::getCreateTime);

        if (req.getType() != null) {
            wrapper.eq(Cashbook::getType, req.getType());
        }
        if (req.getStartTime() != null && !req.getStartTime().isBlank()) {
            wrapper.ge(Cashbook::getCreateTime, LocalDate.parse(req.getStartTime(), DATE_FMT).atStartOfDay());
        }
        if (req.getEndTime() != null && !req.getEndTime().isBlank()) {
            wrapper.le(Cashbook::getCreateTime, LocalDate.parse(req.getEndTime(), DATE_FMT).atTime(23, 59, 59));
        }

        Page<Cashbook> page = cashbookMapper.selectPage(
                new Page<>(req.getPageNum(), req.getPageSize()), wrapper);

        Page<CashbookVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(page.getRecords().stream().map(c -> {
            CashbookVO vo = new CashbookVO();
            vo.setId(c.getId());
            vo.setType(c.getType());
            vo.setAmount(c.getAmount());
            vo.setBizType(c.getBizType());
            vo.setOrderId(c.getOrderId());
            vo.setClerkId(c.getClerkId());
            vo.setMemo(c.getMemo());
            vo.setCreateTime(c.getCreateTime());
            return vo;
        }).toList());
        return result;
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) {
            throw new BizException("未登录或租户上下文缺失");
        }
        return ctx.getShopId();
    }
}
