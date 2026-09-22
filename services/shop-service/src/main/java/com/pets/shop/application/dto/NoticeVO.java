package com.pets.shop.application.dto;

import lombok.Data;

/**
 * 公告 VO。
 */
@Data
public class NoticeVO {
    private Long id;
    private Integer type;
    private String title;
    private String content;
    private String createTime;
}
