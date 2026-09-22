package com.pets.system.application.dto;

import lombok.Data;

/**
 * 系统参数 VO。
 */
@Data
public class SysParamVO {
    private Long id;
    private String groupKey;
    private String paramKey;
    private String paramValue;
    private String remark;
}
