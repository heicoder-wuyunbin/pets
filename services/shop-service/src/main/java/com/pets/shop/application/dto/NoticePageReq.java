package com.pets.shop.application.dto;

import lombok.Data;

/**
 * 公告分页查询请求。
 */
@Data
public class NoticePageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    /** 1公告 2资讯 null=全部 */
    private Integer type;
    private String keyword;
}
