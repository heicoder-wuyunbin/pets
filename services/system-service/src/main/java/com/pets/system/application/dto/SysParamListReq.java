package com.pets.system.application.dto;

import lombok.Data;

/**
 * 系统参数列表查询请求。
 */
@Data
public class SysParamListReq {
    /** 分组键，null=全部 */
    private String group;
}
