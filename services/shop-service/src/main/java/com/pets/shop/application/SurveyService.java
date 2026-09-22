package com.pets.shop.application;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pets.common.context.AuthContextHolder;
import com.pets.common.exception.BizException;
import com.pets.shop.adapter.out.MemberROMapper;
import com.pets.shop.adapter.out.NoticeMapper;
import com.pets.shop.adapter.out.SaleOrderROMapper;
import com.pets.shop.adapter.out.ShopMapper;
import com.pets.shop.adapter.out.StockROMapper;
import com.pets.shop.application.dto.SurveyVO;
import com.pets.shop.domain.entity.MemberRO;
import com.pets.shop.domain.entity.Notice;
import com.pets.shop.domain.entity.SaleOrderRO;
import com.pets.shop.domain.entity.Shop;
import com.pets.shop.domain.entity.StockRO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 店铺概况看板服务：聚合统计卡/待办/店铺信息/公告。
 */
@Service
@RequiredArgsConstructor
public class SurveyService {

    private final ShopMapper shopMapper;
    private final NoticeMapper noticeMapper;
    private final SaleOrderROMapper saleOrderROMapper;
    private final MemberROMapper memberROMapper;
    private final StockROMapper stockROMapper;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public SurveyVO survey() {
        Long shopId = currentShopId();
        Shop shop = shopMapper.selectById(shopId);
        if (shop == null) {
            throw new BizException("店铺不存在");
        }

        SurveyVO vo = new SurveyVO();

        // 统计卡
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime yesterdayStart = todayStart.minusDays(1);

        long todayOrder = saleOrderROMapper.selectCount(
                new LambdaQueryWrapper<SaleOrderRO>()
                        .eq(SaleOrderRO::getShopId, shopId)
                        .ge(SaleOrderRO::getCreateTime, todayStart));
        long yesterdayOrder = saleOrderROMapper.selectCount(
                new LambdaQueryWrapper<SaleOrderRO>()
                        .eq(SaleOrderRO::getShopId, shopId)
                        .ge(SaleOrderRO::getCreateTime, yesterdayStart)
                        .lt(SaleOrderRO::getCreateTime, todayStart));

        long todayMember = memberROMapper.selectCount(
                new LambdaQueryWrapper<MemberRO>()
                        .eq(MemberRO::getShopId, shopId)
                        .ge(MemberRO::getCreateTime, todayStart));
        long yesterdayMember = memberROMapper.selectCount(
                new LambdaQueryWrapper<MemberRO>()
                        .eq(MemberRO::getShopId, shopId)
                        .ge(MemberRO::getCreateTime, yesterdayStart)
                        .lt(MemberRO::getCreateTime, todayStart));

        // 待退款订单数
        long waitRefund = saleOrderROMapper.selectCount(
                new LambdaQueryWrapper<SaleOrderRO>()
                        .eq(SaleOrderRO::getShopId, shopId)
                        .eq(SaleOrderRO::getStatus, 3));

        // 库存不足预警（数量 < 10）
        long stockShortage = stockROMapper.selectCount(
                new LambdaQueryWrapper<StockRO>()
                        .eq(StockRO::getShopId, shopId)
                        .lt(StockRO::getQuantity, 10));

        SurveyVO.StatisticsVO stat = new SurveyVO.StatisticsVO();
        stat.setTodayOrder((int) todayOrder);
        stat.setOrderTrend(calcTrend(todayOrder, yesterdayOrder));
        stat.setTodayMember((int) todayMember);
        stat.setMemberTrend(calcTrend(todayMember, yesterdayMember));
        stat.setTodayReminder(0);
        stat.setReminderTrend("0");
        stat.setTodayAppointment(0);
        stat.setAppointmentTrend("0");
        vo.setStatistics(stat);

        // 待办区
        SurveyVO.TodoVO todo = new SurveyVO.TodoVO();
        todo.setWaitPay(0);
        todo.setWaitDeliver(0);
        todo.setWaitRefund((int) waitRefund);
        todo.setWaitAppointment(0);
        todo.setStockShortage((int) stockShortage);
        vo.setTodo(todo);

        // 店铺信息
        SurveyVO.ShopInfoVO shopInfo = new SurveyVO.ShopInfoVO();
        shopInfo.setId(shop.getId());
        shopInfo.setShopName(shop.getShopName());
        shopInfo.setBindPhone(shop.getBindPhone());
        shopInfo.setExpireTime(shop.getExpireTime() != null ? shop.getExpireTime().format(DATE_FMT) : null);
        shopInfo.setSmsRemain(shop.getSmsRemain());
        vo.setShop(shopInfo);

        // 公告：取最近 5 条上架记录
        List<Notice> notices = noticeMapper.selectList(
                new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getShopId, 0)
                        .eq(Notice::getStatus, 1)
                        .orderByDesc(Notice::getCreateTime)
                        .last("LIMIT 5"));
        vo.setNews(notices.stream().map(n -> {
            SurveyVO.NewsItemVO item = new SurveyVO.NewsItemVO();
            item.setId(n.getId());
            item.setTitle(n.getTitle());
            item.setType(n.getType() != null && n.getType() == 1 ? "announcement" : "news");
            item.setTime(n.getCreateTime() != null ? n.getCreateTime().format(DATE_FMT) : null);
            return item;
        }).toList());

        return vo;
    }

    private String calcTrend(long today, long yesterday) {
        if (yesterday == 0) return today > 0 ? "+100%" : "0";
        long diff = today - yesterday;
        double pct = (double) diff / yesterday * 100;
        return (pct >= 0 ? "+" : "") + String.format("%.0f%%", pct);
    }

    private Long currentShopId() {
        AuthContextHolder.AuthContext ctx = AuthContextHolder.get();
        if (ctx == null || ctx.getShopId() == null) {
            throw new BizException("未登录或租户上下文缺失");
        }
        return ctx.getShopId();
    }
}
