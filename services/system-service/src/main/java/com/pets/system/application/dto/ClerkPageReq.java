package com.pets.system.application.dto;

import lombok.Data;

/**
 * 员工分页查询请求。
 */
@Data
public class ClerkPageReq {
    private Integer pageNum = 1;
    private Integer pageSize = 20;
    /** 关键字：姓名/手机 */
    private String keyword;
    private Integer status;
}
