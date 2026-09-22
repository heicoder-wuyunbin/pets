package com.pets.shop.application.dto;

import lombok.Data;

import java.util.List;

/**
 * 店铺概况看板 VO。
 */
@Data
public class SurveyVO {

    private StatisticsVO statistics;
    private TodoVO todo;
    private ShopInfoVO shop;
    private List<NewsItemVO> news;

    @Data
    public static class StatisticsVO {
        private Integer todayOrder;
        private String orderTrend;
        private Integer todayMember;
        private String memberTrend;
        private Integer todayReminder;
        private String reminderTrend;
        private Integer todayAppointment;
        private String appointmentTrend;
    }

    @Data
    public static class TodoVO {
        private Integer waitPay;
        private Integer waitDeliver;
        private Integer waitRefund;
        private Integer waitAppointment;
        private Integer stockShortage;
    }

    @Data
    public static class ShopInfoVO {
        private Long id;
        private String shopName;
        private String bindPhone;
        private String expireTime;
        private Integer smsRemain;
    }

    @Data
    public static class NewsItemVO {
        private Long id;
        private String title;
        private String type;
        private String time;
    }
}
