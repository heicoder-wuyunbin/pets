package com.pets.trade.application;

import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.id.service.IdService;
import com.pets.trade.adapter.out.CashbookMapper;
import com.pets.trade.adapter.out.OrderItemMapper;
import com.pets.trade.adapter.out.PaymentMapper;
import com.pets.trade.adapter.out.SaleOrderMapper;
import com.pets.trade.adapter.out.WorkOrderMapper;
import com.pets.trade.adapter.out.client.MarketClient;
import com.pets.trade.application.dto.CheckoutReq;
import com.pets.trade.application.dto.CheckoutResult;
import com.pets.trade.application.dto.MarketingCalcReq;
import com.pets.trade.application.dto.MarketingCalcResult;
import com.pets.trade.domain.entity.Cashbook;
import com.pets.trade.domain.entity.OrderItem;
import com.pets.trade.domain.entity.Payment;
import com.pets.trade.domain.entity.SaleOrder;
import com.pets.trade.domain.entity.WorkOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 收银台结算服务。
 * 核心流程：校验购物清单 -> 计算金额 -> 营销优惠计算 -> 生成订单+明细 -> 写支付流水 -> 现金流水 -> 派生服务单。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final SaleOrderMapper saleOrderMapper;
    private final OrderItemMapper orderItemMapper;
    private final PaymentMapper paymentMapper;
    private final CashbookMapper cashbookMapper;
    private final WorkOrderMapper workOrderMapper;
    private final MarketClient marketClient;
    private final IdService idService;

    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Transactional(rollbackFor = Exception.class)
    public CheckoutResult checkout(CheckoutReq req) {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) {
            throw new BizException("未登录或租户上下文缺失");
        }
        Long shopId = ctx.getShopId();
        Long clerkId = ctx.getAccountId();

        // 1. 计算金额
        BigDecimal total = BigDecimal.ZERO;
        for (CheckoutReq.CheckoutItem item : req.getItems()) {
            BigDecimal line = item.getPrice()
                    .multiply(BigDecimal.valueOf(item.getQty()))
                    .multiply(item.getDiscount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            total = total.add(line);
        }
        BigDecimal orderDiscount = req.getDiscount() != null ? req.getDiscount() : new BigDecimal("100");
        BigDecimal payable = total.multiply(orderDiscount).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // 2. 营销优惠计算（失败降级为无优惠，不阻断收银）
        BigDecimal marketingDiscount = BigDecimal.ZERO;
        String activityName = null;
        try {
            List<Long> goodsIds = req.getItems().stream()
                    .filter(i -> i.getItemType() != null && i.getItemType() == 1)
                    .map(CheckoutReq.CheckoutItem::getItemId)
                    .toList();
            MarketingCalcReq calcReq = new MarketingCalcReq();
            calcReq.setShopId(shopId);
            calcReq.setGoodsIds(goodsIds);
            calcReq.setTotalAmount(payable);
            com.pets.common.web.R<MarketingCalcResult> resp = marketClient.calculate(calcReq);
            if (resp != null && resp.code() == 0 && resp.data() != null) {
                MarketingCalcResult calc = resp.data();
                marketingDiscount = calc.getDiscountAmount() != null ? calc.getDiscountAmount() : BigDecimal.ZERO;
                activityName = calc.getActivityName();
                payable = calc.getPayableAmount() != null ? calc.getPayableAmount() : payable;
            }
        } catch (Exception e) {
            log.warn("营销优惠计算失败，降级为无优惠: {}", e.getMessage());
        }

        BigDecimal actual = payable; // 实收 = 应付(首期无抹零)

        // 2. 生成订单号 (P + yyyyMMdd + 6位序号)
        String orderNo = generateOrderNo();

        // 3. 创建订单
        SaleOrder order = new SaleOrder();
        order.setId(idService.next("sale_order"));
        order.setShopId(shopId);
        order.setOrderNo(orderNo);
        order.setMemberId(req.getMemberId());
        order.setMemberName(req.getMemberName());
        order.setClerkId(clerkId);
        order.setTotal(total);
        order.setDiscount(orderDiscount);
        order.setPayable(payable);
        order.setActual(actual);
        order.setPayType(req.getPayType());
        order.setStatus(1); // 直接标记已完成(收银台即时收款)
        order.setRemark(req.getRemark());
        saleOrderMapper.insert(order);

        // 4. 创建订单明细 + 服务项派生服务单
        for (CheckoutReq.CheckoutItem reqItem : req.getItems()) {
            BigDecimal lineAmount = reqItem.getPrice()
                    .multiply(BigDecimal.valueOf(reqItem.getQty()))
                    .multiply(reqItem.getDiscount())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            OrderItem item = new OrderItem();
            item.setId(idService.next("order_item"));
            item.setOrderId(order.getId());
            item.setItemType(reqItem.getItemType());
            item.setItemId(reqItem.getItemId());
            item.setItemName(reqItem.getItemName());
            item.setSkuId(reqItem.getSkuId());
            item.setPrice(reqItem.getPrice());
            item.setQty(reqItem.getQty());
            item.setDiscount(reqItem.getDiscount());
            item.setAmount(lineAmount);
            orderItemMapper.insert(item);

            // 服务项自动生成服务单
            if (reqItem.getItemType() != null && reqItem.getItemType() == 2) {
                WorkOrder wo = new WorkOrder();
                wo.setId(idService.next("work_order"));
                wo.setShopId(shopId);
                wo.setOrderId(order.getId());
                wo.setOrderItemId(item.getId());
                wo.setServiceId(reqItem.getItemId());
                wo.setServiceName(reqItem.getItemName());
                wo.setStatus(0); // 待服务
                workOrderMapper.insert(wo);
            }
        }

        // 5. 写支付流水
        Payment payment = new Payment();
        payment.setId(idService.next("payment"));
        payment.setShopId(shopId);
        payment.setOrderId(order.getId());
        payment.setOrderNo(orderNo);
        payment.setPayType(req.getPayType());
        payment.setAmount(actual);
        payment.setStatus(1);
        payment.setIsRefund(0);
        paymentMapper.insert(payment);

        // 6. 现金支付同步写现金流水
        if ("cash".equalsIgnoreCase(req.getPayType())) {
            Cashbook cashbook = new Cashbook();
            cashbook.setId(idService.next("cashbook"));
            cashbook.setShopId(shopId);
            cashbook.setType(1); // 收入
            cashbook.setAmount(actual);
            cashbook.setBizType("收款");
            cashbook.setOrderId(order.getId());
            cashbook.setClerkId(clerkId);
            cashbookMapper.insert(cashbook);
        }

        // TODO(M4/M5): 库存扣减、会员积分/卡扣款
        CheckoutResult result = new CheckoutResult();
        result.setOrderId(order.getId());
        result.setOrderNo(orderNo);
        result.setTotal(total);
        result.setPayable(payable);
        result.setActualReceived(actual);
        result.setMarketingDiscount(marketingDiscount);
        result.setActivityName(activityName);
        return result;
    }

    private String generateOrderNo() {
        String dateStr = LocalDate.now().format(ORDER_NO_FMT);
        // 简化版: P + 日期 + 时间戳后6位
        long seq = System.currentTimeMillis() % 1000000;
        return "P" + dateStr + String.format("%06d", seq);
    }
}
